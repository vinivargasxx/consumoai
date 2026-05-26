package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.BehaviorCompositionItem
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ConsumptionProfileSummary
import com.example.consumoai.domain.model.ProfileInterpretationType
import com.example.consumoai.domain.model.topProfiles

class BuildConsumptionProfileSummaryUseCase {

    operator fun invoke(result: ConsumptionBehaviorResult): ConsumptionProfileSummary {
        val topProfiles = result.topProfiles(limit = 3)
        val composition = topProfiles
            .map { summary ->
                BehaviorCompositionItem(
                    profile = summary.profile,
                    percentage = (summary.score * 100).coerceIn(0.0, 100.0)
                )
            }

        val primaryProfile = composition.firstOrNull()?.profile ?: result.mainProfile
        val secondaryProfiles = composition.drop(1).map { it.profile }
        val interpretationType = resolveInterpretationType(result, composition)

        return ConsumptionProfileSummary(
            primaryProfile = primaryProfile,
            secondaryProfiles = secondaryProfiles,
            confidence = result.confidence,
            interpretationType = interpretationType,
            humanReadableDescription = buildHumanReadableSummary(
                primaryProfile = primaryProfile,
                secondaryProfiles = secondaryProfiles,
                confidence = result.confidence,
                interpretationType = interpretationType
            ),
            profileComposition = composition
        )
    }

    fun buildHumanReadableSummary(
        primaryProfile: ConsumptionBehaviorProfile,
        secondaryProfiles: List<ConsumptionBehaviorProfile>,
        confidence: Double,
        interpretationType: ProfileInterpretationType
    ): String {
        return when (interpretationType) {
            ProfileInterpretationType.PURE_PROFILE -> {
                "Padrão predominante de ${primaryProfile.toReadableFragment()} com confiança de ${(confidence * 100).toInt()}%."
            }
            ProfileInterpretationType.HYBRID_PROFILE -> {
                val secondaryText = secondaryProfiles
                    .take(2)
                    .joinToString(" e ") { it.toReadableFragment() }
                    .ifBlank { "outros sinais complementares" }
                "Padrão híbrido com predominância de ${primaryProfile.toReadableFragment()} e influência de $secondaryText."
            }
            ProfileInterpretationType.LOW_CONFIDENCE_PROFILE -> {
                "Os sinais atuais indicam ${primaryProfile.toReadableFragment()}, mas com baixa confiança para definir um único padrão dominante."
            }
        }
    }

    private fun resolveInterpretationType(
        result: ConsumptionBehaviorResult,
        composition: List<BehaviorCompositionItem>
    ): ProfileInterpretationType {
        val firstScore = composition.getOrNull(0)?.percentage?.div(100.0) ?: result.confidence
        val secondScore = composition.getOrNull(1)?.percentage?.div(100.0) ?: 0.0
        val scoreGap = firstScore - secondScore
        return when {
            result.confidence < 0.50 -> ProfileInterpretationType.LOW_CONFIDENCE_PROFILE
            secondScore >= 0.25 && scoreGap <= 0.35 -> ProfileInterpretationType.HYBRID_PROFILE
            else -> ProfileInterpretationType.PURE_PROFILE
        }
    }

    private fun ConsumptionBehaviorProfile.toReadableFragment(): String {
        return when (this) {
            ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED -> "consumo orientado à conveniência"
            ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED -> "foco em itens essenciais"
            ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED -> "consumo diversificado e equilibrado"
            ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT -> "recorrência de bebidas não alcoólicas"
            ConsumptionBehaviorProfile.ALCOHOLIC_BEVERAGE_RECURRENT -> "recorrência de bebidas alcoólicas"
            ConsumptionBehaviorProfile.LOW_FRESH_FOOD -> "baixa presença de alimentos frescos"
            ConsumptionBehaviorProfile.HOUSEHOLD_MAINTENANCE -> "manutenção doméstica"
            ConsumptionBehaviorProfile.HIGHLY_CONCENTRATED -> "consumo altamente concentrado"
            ConsumptionBehaviorProfile.IMPULSIVE_CONSUMPTION -> "consumo impulsivo"
            ConsumptionBehaviorProfile.UNDEFINED -> "um padrão ainda indefinido"
        }
    }
}

