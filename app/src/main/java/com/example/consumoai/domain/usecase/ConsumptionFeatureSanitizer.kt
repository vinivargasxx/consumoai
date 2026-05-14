package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.ConsumptionModelInput
import com.example.consumoai.domain.model.FeatureSanitizationNote
import com.example.consumoai.domain.model.SanitizedConsumptionModelInput

class ConsumptionFeatureSanitizer {

    operator fun invoke(input: ConsumptionModelInput): SanitizedConsumptionModelInput {
        val notes = mutableListOf<FeatureSanitizationNote>()
        val sanitizedFeatures = input.features.mapValues { (name, value) ->
            sanitizeFeature(name, value, notes)
        }

        return SanitizedConsumptionModelInput(
            input = input.copy(features = sanitizedFeatures),
            notes = notes
        )
    }

    private fun sanitizeFeature(
        featureName: String,
        value: Double,
        notes: MutableList<FeatureSanitizationNote>
    ): Double {
        val sanitized = when {
            value.isNaN() || value.isInfinite() -> 0.0
            isClampedZeroToOneFeature(featureName) -> value.coerceIn(0.0, 1.0)
            isNonNegativeFeature(featureName) -> value.coerceAtLeast(0.0)
            else -> value
        }

        if (sanitized != value) {
            notes += FeatureSanitizationNote(
                featureName = featureName,
                originalValue = value,
                sanitizedValue = sanitized,
                reason = buildReason(featureName, value, sanitized)
            )
        }

        return sanitized
    }

    private fun isClampedZeroToOneFeature(featureName: String): Boolean {
        return featureName.endsWith("_pct") ||
            featureName.endsWith("_percentage") ||
            featureName.endsWith("_frequency") ||
            featureName.endsWith("_index") ||
            featureName.endsWith("_score") ||
            featureName.endsWith("_ratio") ||
            featureName == "classified_items_percentage"
    }

    private fun isNonNegativeFeature(featureName: String): Boolean {
        return featureName.startsWith("total_") ||
            featureName.startsWith("average_") ||
            featureName.contains("value") ||
            featureName.contains("items")
    }

    private fun buildReason(featureName: String, originalValue: Double, sanitizedValue: Double): String {
        return when {
            originalValue.isNaN() -> "Valor NaN substituído por 0.0"
            originalValue.isInfinite() -> "Valor infinito substituído por 0.0"
            isClampedZeroToOneFeature(featureName) && originalValue < 0.0 -> "Feature limitada ao intervalo 0..1"
            isClampedZeroToOneFeature(featureName) && originalValue > 1.0 -> "Feature limitada ao intervalo 0..1"
            sanitizedValue == 0.0 && originalValue < 0.0 -> "Valor negativo inválido ajustado para 0.0"
            else -> "Valor sanitizado para manter consistência do modelo"
        }
    }
}

