package com.example.consumoai.data.classifier

import com.example.consumoai.domain.classifier.ConsumptionBehaviorClassifier
import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ConsumptionModelInput

/**
 * Fallback local usado apenas quando o backend treinado não está disponível.
 * O fluxo principal permanece no classificador remoto (XGBoost Beverage Split Top 15).
 */
@Suppress("unused")
class RuleBasedConsumptionBehaviorClassifier : ConsumptionBehaviorClassifier {

    override suspend fun classify(input: ConsumptionModelInput): ConsumptionBehaviorResult {
        val profile = classifyProfile(input)
        return ConsumptionBehaviorResult(
            mainProfile = profile,
            confidence = 1.0,
            profileScores = mapOf(profile to 1.0),
            source = BehaviorClassificationSource.RULE_BASED_FALLBACK
        )
    }

    internal fun classifyProfile(input: ConsumptionModelInput): ConsumptionBehaviorProfile {
        val classifiedItemsPercentage = input.feature("classified_items_percentage")
        val nonAlcoholicBeverageSnackCoOccurrence = input.feature("non_alcoholic_beverage_snack_cooccurrence_frequency")
        val categoryConcentrationIndex = input.feature("category_concentration_index")
        val essentialRoutineScore = input.feature("essential_routine_score")
        val produceFrequency = input.feature("produce_frequency")
        val householdRoutineScore = input.feature("household_routine_score")
        val softDrinkFrequency = input.feature("soft_drink_frequency")
        val alcoholicBeverageFrequency = input.feature("alcoholic_beverage_frequency")
        val categoryDominanceGap = input.feature("category_dominance_gap")
        val categoryStabilityScore = input.feature("category_stability_score")
        val otherValuePct = input.feature("other_value_pct")
        val hygieneCleaningCoOccurrence = input.feature("hygiene_cleaning_cooccurrence_frequency")
        val essentialScore = input.feature("essential_score")
        val basicProduceCoOccurrence = input.feature("basic_produce_cooccurrence_frequency")
        val alcoholSnackCoOccurrence = input.feature("alcohol_snack_cooccurrence_frequency")

        return when {
            classifiedItemsPercentage < 0.50 -> ConsumptionBehaviorProfile.UNDEFINED
            categoryConcentrationIndex >= 0.70 && categoryDominanceGap >= 0.30 -> ConsumptionBehaviorProfile.HIGHLY_CONCENTRATED
            alcoholicBeverageFrequency >= 0.30 || alcoholSnackCoOccurrence >= 0.25 -> {
                ConsumptionBehaviorProfile.ALCOHOLIC_BEVERAGE_RECURRENT
            }
            nonAlcoholicBeverageSnackCoOccurrence >= 0.35 || softDrinkFrequency >= 0.30 -> {
                ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT
            }
            essentialRoutineScore >= 0.55 && essentialScore >= 0.55 -> ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED
            householdRoutineScore >= 0.40 || hygieneCleaningCoOccurrence >= 0.35 -> ConsumptionBehaviorProfile.HOUSEHOLD_MAINTENANCE
            produceFrequency <= 0.15 && basicProduceCoOccurrence <= 0.20 -> ConsumptionBehaviorProfile.LOW_FRESH_FOOD
            otherValuePct >= 0.30 && categoryStabilityScore <= 0.35 -> ConsumptionBehaviorProfile.IMPULSIVE_CONSUMPTION
            categoryStabilityScore >= 0.55 && categoryConcentrationIndex < 0.45 -> ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED
            otherValuePct >= 0.20 -> ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED
            else -> ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED
        }
    }

    private fun ConsumptionModelInput.feature(name: String): Double {
        return features[name] ?: 0.0
    }
}

