package com.example.consumoai.domain.model

data class FeatureSanitizationNote(
    val featureName: String,
    val originalValue: Double?,
    val sanitizedValue: Double,
    val reason: String
)

data class SanitizedConsumptionModelInput(
    val input: ConsumptionModelInput,
    val notes: List<FeatureSanitizationNote>
) {
    val hasChanges: Boolean
        get() = notes.isNotEmpty()
}

