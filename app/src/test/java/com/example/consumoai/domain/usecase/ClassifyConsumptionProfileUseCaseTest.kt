package com.example.consumoai.domain.usecase

import com.example.consumoai.data.classifier.RuleBasedConsumptionBehaviorClassifier
import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionModelInput
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ClassifyConsumptionProfileUseCaseTest {

    private val useCase = ClassifyConsumptionProfileUseCase(
        consumptionBehaviorClassifier = RuleBasedConsumptionBehaviorClassifier()
    )

    @Test
    fun invoke_returnsUndefinedWhenFewItemsAreClassified() = runBlocking {
        val input = input(
            "classified_items_percentage" to 0.4,
            "category_concentration_index" to 0.8
        )

        val result = useCase(input)

        assertEquals(ConsumptionBehaviorProfile.UNDEFINED, result.mainProfile)
        assertEquals(BehaviorClassificationSource.RULE_BASED_FALLBACK, result.source)
        assertEquals(1.0, result.confidence, 0.0001)
    }

    @Test
    fun invoke_returnsBeverageRecurrentWhenValueAndFrequencyThresholdsMatch() = runBlocking {
        val input = input(
            "classified_items_percentage" to 0.9,
            "category_concentration_index" to 0.3,
            "beverages_value_pct" to 0.3,
            "beverages_frequency" to 0.75
        )

        assertEquals(ConsumptionBehaviorProfile.BEVERAGE_RECURRENT, useCase(input).mainProfile)
    }

    @Test
    fun invoke_returnsConvenienceOrientedWhenConvenienceScoreIsHigh() = runBlocking {
        val input = input(
            "classified_items_percentage" to 0.9,
            "category_concentration_index" to 0.4,
            "convenience_score" to 0.6
        )

        assertEquals(ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED, useCase(input).mainProfile)
    }

    @Test
    fun invoke_returnsHighlyConcentratedWhenConcentrationIsVeryHigh() = runBlocking {
        val input = input(
            "classified_items_percentage" to 0.9,
            "category_concentration_index" to 0.72
        )

        assertEquals(ConsumptionBehaviorProfile.HIGHLY_CONCENTRATED, useCase(input).mainProfile)
    }

    private fun input(vararg overrides: Pair<String, Double>): ConsumptionModelInput {
        val defaults = mutableMapOf(
            "total_receipts" to 5.0,
            "total_items" to 20.0,
            "total_value" to 200.0,
            "average_ticket" to 40.0,
            "average_items_per_receipt" to 4.0,
            "basic_food_value_pct" to 0.20,
            "industrialized_value_pct" to 0.20,
            "beverages_value_pct" to 0.10,
            "hygiene_value_pct" to 0.05,
            "cleaning_value_pct" to 0.05,
            "produce_value_pct" to 0.10,
            "other_value_pct" to 0.30,
            "basic_food_frequency" to 0.60,
            "industrialized_frequency" to 0.60,
            "beverages_frequency" to 0.30,
            "produce_frequency" to 0.30,
            "hygiene_frequency" to 0.20,
            "cleaning_frequency" to 0.20,
            "category_concentration_index" to 0.30,
            "category_dominance_gap" to 0.10,
            "category_diversity_index" to 0.60,
            "essential_categories_percentage" to 0.40,
            "non_essential_categories_percentage" to 0.60,
            "convenience_score" to 0.30,
            "essential_score" to 0.40,
            "diversity_score" to 0.50,
            "classified_items_percentage" to 0.90
        )

        overrides.forEach { (key, value) ->
            defaults[key] = value
        }

        return ConsumptionModelInput(features = defaults)
    }
}

