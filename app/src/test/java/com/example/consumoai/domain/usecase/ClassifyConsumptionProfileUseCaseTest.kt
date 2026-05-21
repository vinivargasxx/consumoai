package com.example.consumoai.domain.usecase

import com.example.consumoai.data.classifier.RuleBasedConsumptionBehaviorClassifier
import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionModelInput
import com.example.consumoai.domain.model.MODEL_FINAL_FEATURES
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
    fun invoke_returnsBeverageRecurrentWhenV3BeverageSignalsAreHigh() = runBlocking {
        val input = input(
            "classified_items_percentage" to 0.9,
            "category_concentration_index" to 0.3,
            "beverage_snack_cooccurrence_frequency" to 0.42,
            "soft_drink_frequency" to 0.58
        )

        assertEquals(ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT, useCase(input).mainProfile)
    }

    @Test
    fun invoke_returnsAlcoholicBeverageRecurrentWhenAlcoholSignalsAreHigh() = runBlocking {
        val input = input(
            "classified_items_percentage" to 0.9,
            "alcoholic_beverage_frequency" to 0.34,
            "alcohol_snack_cooccurrence_frequency" to 0.28
        )

        assertEquals(ConsumptionBehaviorProfile.ALCOHOLIC_BEVERAGE_RECURRENT, useCase(input).mainProfile)
    }

    @Test
    fun invoke_returnsConvenienceOrientedWhenOtherValueIsElevated() = runBlocking {
        val input = input(
            "classified_items_percentage" to 0.9,
            "category_concentration_index" to 0.4,
            "category_stability_score" to 0.30,
            "other_value_pct" to 0.25
        )

        assertEquals(ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED, useCase(input).mainProfile)
    }

    @Test
    fun invoke_returnsHighlyConcentratedWhenConcentrationIsVeryHigh() = runBlocking {
        val input = input(
            "classified_items_percentage" to 0.9,
            "category_concentration_index" to 0.72,
            "category_dominance_gap" to 0.35
        )

        assertEquals(ConsumptionBehaviorProfile.HIGHLY_CONCENTRATED, useCase(input).mainProfile)
    }

    private fun input(vararg overrides: Pair<String, Double>): ConsumptionModelInput {
        val defaults = MODEL_FINAL_FEATURES
            .associateWith { 0.1 }
            .toMutableMap()
            .apply {
                this["beverage_snack_cooccurrence_frequency"] = 0.20
                this["category_concentration_index"] = 0.30
                this["classified_items_percentage"] = 0.90
                this["essential_routine_score"] = 0.40
                this["produce_frequency"] = 0.30
                this["household_routine_score"] = 0.15
                this["soft_drink_frequency"] = 0.18
                this["alcoholic_beverage_frequency"] = 0.10
                this["category_dominance_gap"] = 0.10
                this["category_stability_score"] = 0.60
                this["other_value_pct"] = 0.12
                this["hygiene_cleaning_cooccurrence_frequency"] = 0.10
                this["essential_score"] = 0.40
                this["basic_produce_cooccurrence_frequency"] = 0.25
                this["alcohol_snack_cooccurrence_frequency"] = 0.10
            }

        overrides.forEach { (key, value) ->
            defaults[key] = value
        }

        return ConsumptionModelInput(features = defaults)
    }
}

