package com.example.consumoai.data.classifier

import com.example.consumoai.domain.classifier.ConsumptionBehaviorClassifier
import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ConsumptionModelInput

/**
 * Temporary rule-based implementation until a trained model replaces it.
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
        val totalReceipts = input.feature("total_receipts")
        val classifiedItemsPercentage = input.feature("classified_items_percentage")
        val categoryConcentrationIndex = input.feature("category_concentration_index")
        val convenienceScore = input.feature("convenience_score")
        val essentialScore = input.feature("essential_score")
        val diversityScore = input.feature("diversity_score")
        val nonEssentialPercentage = input.feature("non_essential_categories_percentage")
        val beveragesValuePercentage = input.feature("beverages_value_pct")
        val beveragesFrequency = input.feature("beverages_frequency")
        val produceValuePercentage = input.feature("produce_value_pct")
        val produceFrequency = input.feature("produce_frequency")
        val householdMaintenanceValue = input.feature("hygiene_value_pct") + input.feature("cleaning_value_pct")

        return when {
            totalReceipts <= 0.0 -> ConsumptionBehaviorProfile.UNDEFINED
            classifiedItemsPercentage < 0.50 -> ConsumptionBehaviorProfile.UNDEFINED
            categoryConcentrationIndex >= 0.70 -> ConsumptionBehaviorProfile.HIGHLY_CONCENTRATED
            nonEssentialPercentage >= 0.75 && convenienceScore >= 0.55 -> ConsumptionBehaviorProfile.IMPULSIVE_CONSUMPTION
            convenienceScore >= 0.60 -> ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED
            beveragesValuePercentage >= 0.25 && beveragesFrequency >= 0.50 -> ConsumptionBehaviorProfile.BEVERAGE_RECURRENT
            essentialScore >= 0.60 -> ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED
            householdMaintenanceValue >= 0.25 -> ConsumptionBehaviorProfile.HOUSEHOLD_MAINTENANCE
            produceValuePercentage <= 0.05 && produceFrequency <= 0.20 -> ConsumptionBehaviorProfile.LOW_FRESH_FOOD
            diversityScore >= 0.55 && categoryConcentrationIndex < 0.45 -> ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED
            else -> ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED
        }
    }

    private fun ConsumptionModelInput.feature(name: String): Double {
        return features[name] ?: 0.0
    }
}

