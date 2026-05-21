package com.example.consumoai.domain.usecase

import android.util.Log
import com.example.consumoai.domain.model.ConsumptionMetrics
import com.example.consumoai.domain.model.ConsumptionModelInput
import com.example.consumoai.domain.model.MODEL_FEATURE_COUNT
import com.example.consumoai.domain.model.MODEL_FINAL_FEATURES
import com.example.consumoai.domain.model.MODEL_INPUT_VERSION
import com.example.consumoai.domain.model.ProductCategory

class BuildConsumptionModelInputUseCase {

    private companion object {
        const val TAG = "MODEL_INPUT"
    }

    operator fun invoke(metrics: ConsumptionMetrics): ConsumptionModelInput {
        val valuePercentages = metrics.valuePercentageByCategory
        val frequencies = metrics.frequencyByCategory

        val allFeatures = linkedMapOf(
            "total_receipts" to metrics.totalReceipts.toDouble(),
            "total_items" to metrics.totalItems.toDouble(),
            "total_value" to metrics.totalValue,
            "average_ticket" to metrics.averageTicket,
            "average_items_per_receipt" to metrics.averageItemsPerReceipt,
            "basic_food_value_pct" to valuePercentages[ProductCategory.BASIC_FOOD].orZero(),
            "industrialized_value_pct" to valuePercentages[ProductCategory.INDUSTRIALIZED].orZero(),
            "beverages_value_pct" to valuePercentages[ProductCategory.BEVERAGES].orZero(),
            "hygiene_value_pct" to valuePercentages[ProductCategory.HYGIENE].orZero(),
            "cleaning_value_pct" to valuePercentages[ProductCategory.CLEANING].orZero(),
            "produce_value_pct" to valuePercentages[ProductCategory.PRODUCE].orZero(),
            "other_value_pct" to valuePercentages[ProductCategory.OTHER].orZero(),
            "basic_food_frequency" to frequencies[ProductCategory.BASIC_FOOD].orZero(),
            "industrialized_frequency" to frequencies[ProductCategory.INDUSTRIALIZED].orZero(),
            "beverages_frequency" to frequencies[ProductCategory.BEVERAGES].orZero(),
            "produce_frequency" to frequencies[ProductCategory.PRODUCE].orZero(),
            "hygiene_frequency" to frequencies[ProductCategory.HYGIENE].orZero(),
            "cleaning_frequency" to frequencies[ProductCategory.CLEANING].orZero(),
            "classified_items_percentage" to metrics.classifiedItemsPercentage,
            "category_concentration_index" to metrics.categoryConcentrationIndex,
            "category_dominance_gap" to metrics.categoryDominanceGap,
            "category_diversity_index" to metrics.categoryDiversityIndex,
            "essential_categories_percentage" to metrics.essentialCategoriesPercentage,
            "non_essential_categories_percentage" to metrics.nonEssentialCategoriesPercentage,
            "essential_score" to metrics.essentialScore,
            "convenience_score" to metrics.convenienceScore,
            "diversity_score" to metrics.diversityScore,
            "time_span_days" to metrics.timeSpanDays,
            "receipts_per_week" to metrics.receiptsPerWeek,
            "average_days_between_receipts" to metrics.averageDaysBetweenReceipts,
            "purchase_regularity_score" to metrics.purchaseRegularityScore,
            "ticket_standard_deviation" to metrics.ticketStandardDeviation,
            "ticket_variation_coefficient" to metrics.ticketVariationCoefficient,
            "item_count_variation_coefficient" to metrics.itemCountVariationCoefficient,
            "high_ticket_receipts_percentage" to metrics.highTicketReceiptsPercentage,
            "low_ticket_receipts_percentage" to metrics.lowTicketReceiptsPercentage,
            "category_stability_score" to metrics.categoryStabilityScore,
            "average_category_overlap_between_receipts" to metrics.averageCategoryOverlapBetweenReceipts,
            "recurring_item_ratio" to metrics.recurringItemRatio,
            "top_item_repetition_rate" to metrics.topItemRepetitionRate,
            "beverage_snack_cooccurrence_frequency" to metrics.beverageSnackCoOccurrenceFrequency,
            "alcohol_snack_cooccurrence_frequency" to metrics.alcoholSnackCoOccurrenceFrequency,
            "hygiene_cleaning_cooccurrence_frequency" to metrics.hygieneCleaningCoOccurrenceFrequency,
            "basic_produce_cooccurrence_frequency" to metrics.basicProduceCoOccurrenceFrequency,
            "alcoholic_beverage_value_pct" to metrics.alcoholicBeverageValuePct,
            "alcoholic_beverage_frequency" to metrics.alcoholicBeverageFrequency,
            "non_alcoholic_beverage_value_pct" to metrics.nonAlcoholicBeverageValuePct,
            "non_alcoholic_beverage_frequency" to metrics.nonAlcoholicBeverageFrequency,
            "soft_drink_value_pct" to metrics.softDrinkValuePct,
            "soft_drink_frequency" to metrics.softDrinkFrequency,
            "energy_drink_value_pct" to metrics.energyDrinkValuePct,
            "energy_drink_frequency" to metrics.energyDrinkFrequency,
            "snack_sweet_value_pct" to metrics.snackSweetValuePct,
            "snack_sweet_frequency" to metrics.snackSweetFrequency,
            "frozen_convenience_value_pct" to metrics.frozenConvenienceValuePct,
            "frozen_convenience_frequency" to metrics.frozenConvenienceFrequency,
            "dairy_value_pct" to metrics.dairyValuePct,
            "meat_protein_value_pct" to metrics.meatProteinValuePct,
            "fresh_produce_value_pct" to metrics.freshProduceValuePct,
            "convenience_meal_value_pct" to metrics.convenienceMealValuePct,
            "convenience_meal_frequency" to metrics.convenienceMealFrequency,
            "essential_routine_score" to metrics.essentialRoutineScore,
            "convenience_routine_score" to metrics.convenienceRoutineScore,
            "non_alcoholic_beverage_snack_cooccurrence_frequency" to metrics.nonAlcoholicBeverageSnackCoOccurrenceFrequency,
            "household_routine_score" to metrics.householdRoutineScore,
            "fresh_food_presence_score" to metrics.freshFoodPresenceScore
        )

        val selectedFeatures = linkedMapOf<String, Double>()
        MODEL_FINAL_FEATURES.forEach { feature ->
            val value = allFeatures[feature]
                ?: error("Feature obrigatoria ausente: $feature")
            selectedFeatures[feature] = if (value.isNaN() || value.isInfinite()) 0.0 else value
        }

        check(selectedFeatures.size == MODEL_FEATURE_COUNT) {
            "Quantidade de features invalida para o modelo final: ${selectedFeatures.size}"
        }

        // Log detalhado do input do modelo
        runCatching {
            Log.d(
                TAG,
                "version=$MODEL_INPUT_VERSION feature_count=${selectedFeatures.size} features=${MODEL_FINAL_FEATURES.joinToString(",")}"
            )
        }

        return ConsumptionModelInput(
            version = MODEL_INPUT_VERSION,
            features = selectedFeatures
        )
    }
}

private fun Double?.orZero(): Double = this ?: 0.0
