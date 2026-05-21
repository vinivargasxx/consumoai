package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.BehaviorCompositionItem
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ConsumptionProfileSummary
import com.example.consumoai.domain.model.ProfileInterpretationType

class BuildConsumptionProfileSummaryUseCase {

    operator fun invoke(result: ConsumptionBehaviorResult): ConsumptionProfileSummary {
        val composition = result.profileScores
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { (profile, score) ->
                BehaviorCompositionItem(
                    profile = profile,
                    percentage = (score * 100).coerceIn(0.0, 100.0)
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
        val secondScore = composition.getOrNull(1)?.percentage?.div(100.0) ?: 0.0
        return when {
            result.confidence < 0.30 -> ProfileInterpretationType.LOW_CONFIDENCE_PROFILE
            result.confidence < 0.45 && secondScore >= 0.18 -> ProfileInterpretationType.HYBRID_PROFILE
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

