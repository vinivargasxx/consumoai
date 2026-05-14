package com.example.consumoai.domain.model

enum class ProfileInterpretationType {
    PURE_PROFILE,
    HYBRID_PROFILE,
    LOW_CONFIDENCE_PROFILE
}

data class ConsumptionProfileSummary(
    val primaryProfile: ConsumptionBehaviorProfile,
    val secondaryProfiles: List<ConsumptionBehaviorProfile>,
    val confidence: Double,
    val interpretationType: ProfileInterpretationType,
    val humanReadableDescription: String,
    val profileComposition: List<BehaviorCompositionItem>
)

