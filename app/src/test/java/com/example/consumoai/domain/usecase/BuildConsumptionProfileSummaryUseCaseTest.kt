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
    fun invoke_marksPureProfileWhenSecondScoreIsLow() {
        val summary = useCase(
            ConsumptionBehaviorResult(
                mainProfile = ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT,
                confidence = 0.90,
                profileScores = mapOf(
                    ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT to 0.90,
                    ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED to 0.05
                ),
                source = BehaviorClassificationSource.TRAINED_MODEL
            )
        )

        assertEquals(ProfileInterpretationType.PURE_PROFILE, summary.interpretationType)
        assertEquals(ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT, summary.primaryProfile)
    }

    @Test
    fun invoke_marksHybridProfileWhenTopTwoScoresAreClose() {
        val summary = useCase(
            ConsumptionBehaviorResult(
                mainProfile = ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT,
                confidence = 0.65,
                profileScores = mapOf(
                    ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT to 0.65,
                    ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED to 0.35,
                    ConsumptionBehaviorProfile.HOUSEHOLD_MAINTENANCE to 0.05
                ),
                source = BehaviorClassificationSource.TRAINED_MODEL
            )
        )

        assertEquals(ProfileInterpretationType.HYBRID_PROFILE, summary.interpretationType)
        assertTrue(summary.humanReadableDescription.contains("híbrido"))
        assertEquals(2, summary.secondaryProfiles.size)
    }

    @Test
    fun invoke_marksLowConfidenceProfileWhenConfidenceIsBelowThreshold() {
        val summary = useCase(
            ConsumptionBehaviorResult(
                mainProfile = ConsumptionBehaviorProfile.UNDEFINED,
                confidence = 0.49,
                profileScores = mapOf(
                    ConsumptionBehaviorProfile.UNDEFINED to 0.49,
                    ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED to 0.40
                ),
                source = BehaviorClassificationSource.TRAINED_MODEL
            )
        )

        assertEquals(ProfileInterpretationType.LOW_CONFIDENCE_PROFILE, summary.interpretationType)
        assertTrue(summary.humanReadableDescription.contains("baixa confiança"))
    }
}

