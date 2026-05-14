package com.example.consumoai.domain.model

data class ConsumptionInsight(
    val title: String,
    val description: String,
    val type: InsightType,
    val severity: InsightSeverity,
    val relatedProfiles: List<ConsumptionBehaviorProfile> = emptyList(),
    val relatedFeatures: List<String> = emptyList()
)

enum class InsightType {
    BEHAVIORAL_PATTERN,
    CATEGORY_DOMINANCE,
    RECURRENCE,
    DIVERSITY,
    CONCENTRATION,
    CONSUMPTION_BALANCE,
    PURCHASE_PATTERN,
    MODEL_INTERPRETATION
}

enum class InsightSeverity {
    LOW,
    MEDIUM,
    HIGH
}

