package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ProfileInterpretationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildConsumptionProfileSummaryUseCaseTest {

    private val useCase = BuildConsumptionProfileSummaryUseCase()

    @Test
    fun invoke_marksPureProfileWhenConfidenceIsHigh() {
        val summary = useCase(
            ConsumptionBehaviorResult(
                mainProfile = ConsumptionBehaviorProfile.BEVERAGE_RECURRENT,
                confidence = 0.62,
                profileScores = mapOf(
                    ConsumptionBehaviorProfile.BEVERAGE_RECURRENT to 0.62,
                    ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED to 0.20
                ),
                source = BehaviorClassificationSource.TRAINED_MODEL
            )
        )

        assertEquals(ProfileInterpretationType.PURE_PROFILE, summary.interpretationType)
        assertEquals(ConsumptionBehaviorProfile.BEVERAGE_RECURRENT, summary.primaryProfile)
    }

    @Test
    fun invoke_marksHybridProfileWhenConfidenceIsLowButSignalsAreMixed() {
        val summary = useCase(
            ConsumptionBehaviorResult(
                mainProfile = ConsumptionBehaviorProfile.BEVERAGE_RECURRENT,
                confidence = 0.40,
                profileScores = mapOf(
                    ConsumptionBehaviorProfile.BEVERAGE_RECURRENT to 0.40,
                    ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED to 0.28,
                    ConsumptionBehaviorProfile.HOUSEHOLD_MAINTENANCE to 0.20
                ),
                source = BehaviorClassificationSource.TRAINED_MODEL
            )
        )

        assertEquals(ProfileInterpretationType.HYBRID_PROFILE, summary.interpretationType)
        assertTrue(summary.humanReadableDescription.contains("híbrido"))
        assertEquals(2, summary.secondaryProfiles.size)
    }

    @Test
    fun invoke_marksLowConfidenceProfileWhenConfidenceIsVeryLow() {
        val summary = useCase(
            ConsumptionBehaviorResult(
                mainProfile = ConsumptionBehaviorProfile.UNDEFINED,
                confidence = 0.20,
                profileScores = mapOf(
                    ConsumptionBehaviorProfile.UNDEFINED to 0.20,
                    ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED to 0.18
                ),
                source = BehaviorClassificationSource.TRAINED_MODEL
            )
        )

        assertEquals(ProfileInterpretationType.LOW_CONFIDENCE_PROFILE, summary.interpretationType)
        assertTrue(summary.humanReadableDescription.contains("baixa confiança"))
    }
}

