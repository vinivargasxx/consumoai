package com.example.consumoai.domain.model

data class ConsumptionBehaviorResult(
    val mainProfile: ConsumptionBehaviorProfile,
    val confidence: Double,
    val profileScores: Map<ConsumptionBehaviorProfile, Double>,
    val source: BehaviorClassificationSource,
    val profileSummary: ConsumptionProfileSummary? = null,
    val fallbackReason: FallbackReason? = null,
    val inferenceDurationMs: Long = 0L,
    val usedSanitizedInput: Boolean = false,
    val sanitizationNotes: List<FeatureSanitizationNote> = emptyList()
)

enum class BehaviorClassificationSource {
    TRAINED_MODEL,
    RULE_BASED_FALLBACK
}

