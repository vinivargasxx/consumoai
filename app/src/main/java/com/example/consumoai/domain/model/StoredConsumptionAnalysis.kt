package com.example.consumoai.domain.model

data class StoredConsumptionAnalysis(
    val receipts: List<Receipt>,
    val metrics: ConsumptionMetrics,
    val modelInput: ConsumptionModelInput,
    val behaviorResult: ConsumptionBehaviorResult,
    val behaviorAnalysis: ConsumptionBehaviorAnalysis? = null,
    val profileExplanation: String? = null,
    val anonymizedExport: AnonymizedConsumptionExport? = null
)

