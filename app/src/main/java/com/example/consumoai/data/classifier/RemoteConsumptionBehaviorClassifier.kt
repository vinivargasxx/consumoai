package com.example.consumoai.data.classifier

import android.util.Log
import com.example.consumoai.domain.classifier.ConsumptionBehaviorClassifier
import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ConsumptionModelInput
import com.example.consumoai.domain.model.FallbackReason

class RemoteConsumptionBehaviorClassifier(
    private val api: ConsumptionModelApi,
    private val fallbackClassifier: RuleBasedConsumptionBehaviorClassifier
) : ConsumptionBehaviorClassifier {

    private companion object {
        const val REQUEST_TAG = "MODEL_REQUEST"
        const val RESPONSE_TAG = "MODEL_RESPONSE"
        const val ERROR_TAG = "MODEL_ERROR"
        const val FALLBACK_TAG = "MODEL_FALLBACK"
    }

    override suspend fun classify(input: ConsumptionModelInput): ConsumptionBehaviorResult {
        val startNanos = System.nanoTime()

        if (input.features.isEmpty()) {
            return fallback(
                input = input,
                reason = FallbackReason.EMPTY_FEATURES,
                durationMs = elapsedMillis(startNanos),
                details = "Nenhuma feature disponível para enviar ao modelo."
            )
        }

        return try {
            safeLogDebug(
                REQUEST_TAG,
                "version=${input.version} features=${input.features.size} payload=${input.features.toSortedMap()}"
            )
            val response = api.predict(
                ModelPredictionRequestDto(
                    version = input.version,
                    features = input.features
                )
            )
            val durationMs = elapsedMillis(startNanos)
            safeLogDebug(
                RESPONSE_TAG,
                "durationMs=$durationMs main=${response.main_profile} confidence=${response.confidence} scores=${response.profile_scores}"
            )

            val mainProfile = response.main_profile.toBehaviorProfile()
            val mappedScores = response.profile_scores
                .mapKeys { (profile, _) -> profile.toBehaviorProfile() }
                .toMutableMap()
                .apply {
                    putIfAbsent(mainProfile, response.confidence)
                }
                .toMap()

            ConsumptionBehaviorResult(
                mainProfile = mainProfile,
                confidence = response.confidence.coerceIn(0.0, 1.0),
                profileScores = mappedScores,
                source = BehaviorClassificationSource.TRAINED_MODEL,
                inferenceDurationMs = durationMs
            )
        } catch (error: Exception) {
            val durationMs = elapsedMillis(startNanos)
            val reason = when {
                error is IllegalArgumentException -> FallbackReason.INVALID_INPUT
                error.message?.contains("load", ignoreCase = true) == true -> FallbackReason.MODEL_LOAD_ERROR
                else -> FallbackReason.INFERENCE_ERROR
            }
            safeLogError("durationMs=$durationMs reason=$reason message=${error.message}\n${error.stackTraceToString()}")
            fallback(
                input = input,
                reason = reason,
                durationMs = durationMs,
                details = error.message ?: "Erro desconhecido na inferência remota"
            )
        }
    }

    private suspend fun fallback(
        input: ConsumptionModelInput,
        reason: FallbackReason,
        durationMs: Long,
        details: String
    ): ConsumptionBehaviorResult {
        safeLogDebug(
            FALLBACK_TAG,
            "reason=$reason durationMs=$durationMs details=$details features=${input.features.toSortedMap()}"
        )
        return fallbackClassifier.classify(input).copy(
            fallbackReason = reason,
            inferenceDurationMs = durationMs
        )
    }

    private fun elapsedMillis(startNanos: Long): Long {
        return ((System.nanoTime() - startNanos) / 1_000_000L).coerceAtLeast(0L)
    }

    private fun safeLogDebug(tag: String, message: String) {
        runCatching { Log.d(tag, message) }
    }

    private fun safeLogError(message: String) {
        runCatching { Log.e(ERROR_TAG, message) }
    }

    private fun String.toBehaviorProfile(): ConsumptionBehaviorProfile {
        return runCatching { ConsumptionBehaviorProfile.valueOf(this) }
            .getOrDefault(ConsumptionBehaviorProfile.UNDEFINED)
    }
}
