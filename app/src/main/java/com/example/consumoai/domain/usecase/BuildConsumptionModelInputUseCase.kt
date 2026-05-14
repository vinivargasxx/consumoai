package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.ConsumptionMetrics
import com.example.consumoai.domain.model.ConsumptionModelInput
import com.example.consumoai.domain.model.ProductCategory

class BuildConsumptionModelInputUseCase {

    operator fun invoke(metrics: ConsumptionMetrics): ConsumptionModelInput {
        val valuePercentages = metrics.valuePercentageByCategory
        val frequencies = metrics.frequencyByCategory

        return ConsumptionModelInput(
            features = linkedMapOf(
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

                "category_concentration_index" to metrics.categoryConcentrationIndex,
                "category_dominance_gap" to metrics.categoryDominanceGap,
                "category_diversity_index" to metrics.categoryDiversityIndex,
                "essential_categories_percentage" to metrics.essentialCategoriesPercentage,
                "non_essential_categories_percentage" to metrics.nonEssentialCategoriesPercentage,
                "convenience_score" to metrics.convenienceScore,
                "essential_score" to metrics.essentialScore,
                "diversity_score" to metrics.diversityScore,
                "classified_items_percentage" to metrics.classifiedItemsPercentage
            )
        )
    }
}

private fun Double?.orZero(): Double = this ?: 0.0

