package com.example.consumoai.data.classifier
import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionModelInput
import com.example.consumoai.domain.model.FallbackReason
import com.example.consumoai.domain.model.MODEL_FEATURE_COUNT
import com.example.consumoai.domain.model.MODEL_FINAL_FEATURES
import com.example.consumoai.domain.model.MODEL_INPUT_VERSION
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
class RemoteConsumptionBehaviorClassifierTest {
    @Test
    fun classify_returnsRemotePredictionWhenApiSucceeds() = runBlocking {
        val classifier = RemoteConsumptionBehaviorClassifier(
            api = object : ConsumptionModelApi {
                override suspend fun predict(request: ModelPredictionRequestDto): ModelPredictionResponseDto {
                    assertEquals(MODEL_INPUT_VERSION, request.version)
                    assertEquals(MODEL_FEATURE_COUNT, request.features.size)
                    return ModelPredictionResponseDto(
                        main_profile = "ALCOHOLIC_BEVERAGE_RECURRENT",
                        confidence = 0.665,
                        profile_scores = mapOf(
                            "ALCOHOLIC_BEVERAGE_RECURRENT" to 0.665,
                            "NON_ALCOHOLIC_BEVERAGE_RECURRENT" to 0.225,
                            "DIVERSIFIED_BALANCED" to 0.295,
                            "LOW_FRESH_FOOD" to 0.10
                        ),
                        version = request.version,
                        feature_count = request.features.size,
                        model = "xgboost_beverage_split_top15"
                    )
                }
            },
            fallbackClassifier = RuleBasedConsumptionBehaviorClassifier()
        )
        val result = classifier.classify(input())
        assertEquals(BehaviorClassificationSource.TRAINED_MODEL, result.source)
        assertEquals(ConsumptionBehaviorProfile.ALCOHOLIC_BEVERAGE_RECURRENT, result.mainProfile)
        assertEquals(0.665, result.confidence, 0.0001)
        assertEquals(0.295, result.profileScores[ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED] ?: -1.0, 0.0001)
        assertEquals("final", result.requestedInputVersion)
        assertEquals(MODEL_FEATURE_COUNT, result.requestedFeatureCount)
        assertEquals("final", result.responseVersion)
        assertEquals(MODEL_FEATURE_COUNT, result.responseFeatureCount)
        assertEquals("xgboost_beverage_split_top15", result.backendModelUsed)
        assertEquals(true, result.inferenceDurationMs >= 0L)
    }
     @Test
    fun classify_usesFallbackWhenApiFails() = runBlocking {
        val classifier = RemoteConsumptionBehaviorClassifier(
            api = object : ConsumptionModelApi {
                override suspend fun predict(request: ModelPredictionRequestDto): ModelPredictionResponseDto {
                    error("backend offline")
                }
            },
            fallbackClassifier = RuleBasedConsumptionBehaviorClassifier()
        )
        val result = classifier.classify(
            input(
                "non_alcoholic_beverage_frequency" to 0.75
            )
        )
        assertEquals(BehaviorClassificationSource.RULE_BASED_FALLBACK, result.source)
        assertEquals(1.0, result.confidence, 0.0001)
        assertEquals(FallbackReason.INFERENCE_ERROR, result.fallbackReason)
        assertEquals("final", result.requestedInputVersion)
        assertEquals(MODEL_FEATURE_COUNT, result.requestedFeatureCount)
    }

    @Test
    fun classify_usesFallbackWhenBackendRejectsInputWith400() = runBlocking {
        val classifier = RemoteConsumptionBehaviorClassifier(
            api = object : ConsumptionModelApi {
                override suspend fun predict(request: ModelPredictionRequestDto): ModelPredictionResponseDto {
                    throw HttpException(Response.error<Any>(400, "bad request".toResponseBody(null)))
                }
            },
            fallbackClassifier = RuleBasedConsumptionBehaviorClassifier()
        )

        val result = classifier.classify(input())

        assertEquals(BehaviorClassificationSource.RULE_BASED_FALLBACK, result.source)
        assertEquals(FallbackReason.BACKEND_REJECTED_INPUT, result.fallbackReason)
        assertEquals("final", result.requestedInputVersion)
        assertEquals(MODEL_FEATURE_COUNT, result.requestedFeatureCount)
    }
    @Test
    fun classify_usesFallbackWhenInputHasNoFeatures() = runBlocking {
        val classifier = RemoteConsumptionBehaviorClassifier(
            api = object : ConsumptionModelApi {
                override suspend fun predict(request: ModelPredictionRequestDto): ModelPredictionResponseDto {
                    error("should not call API")
                }
            },
            fallbackClassifier = RuleBasedConsumptionBehaviorClassifier()
        )
        val result = classifier.classify(ConsumptionModelInput(features = emptyMap()))
        assertEquals(BehaviorClassificationSource.RULE_BASED_FALLBACK, result.source)
        assertEquals(FallbackReason.EMPTY_FEATURES, result.fallbackReason)
    }
    private fun input(vararg overrides: Pair<String, Double>): ConsumptionModelInput {
        val defaults = MODEL_FINAL_FEATURES
            .associateWith { 0.1 }
            .toMutableMap()
            .apply {
                // Valores específicos para as 15 features oficiais do modelo calibrado.
                this["classified_items_percentage"] = 0.90
                this["category_concentration_index"] = 0.30
                this["essential_routine_score"] = 0.55
                this["household_routine_score"] = 0.20
                this["produce_frequency"] = 0.35
                this["basic_produce_cooccurrence_frequency"] = 0.22
                this["alcoholic_beverage_frequency"] = 0.15
                this["essential_score"] = 0.65
                this["category_dominance_gap"] = 0.25
                this["non_alcoholic_beverage_frequency"] = 0.45
                this["category_stability_score"] = 0.60
                this["other_value_pct"] = 0.05
                this["hygiene_cleaning_cooccurrence_frequency"] = 0.12
                this["soft_drink_frequency"] = 0.30
                this["ticket_variation_coefficient"] = 0.18
            }
        assertEquals(MODEL_FINAL_FEATURES, defaults.keys.toList())
        overrides.forEach { (key, value) ->
            defaults[key] = value
        }
        return ConsumptionModelInput(features = defaults)
    }
}
