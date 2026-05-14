package com.example.consumoai.domain.model

data class ConsumptionBehaviorAnalysis(
    val behaviorResult: ConsumptionBehaviorResult,
    val insights: List<ConsumptionInsight>,
    val summary: String,
    val behavioralComposition: List<BehaviorCompositionItem>
)

data class BehaviorCompositionItem(
    val profile: ConsumptionBehaviorProfile,
    val percentage: Double
)

