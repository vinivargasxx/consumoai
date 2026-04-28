package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.ConsumptionMetrics
import com.example.consumoai.domain.model.ConsumptionProfile
import com.example.consumoai.domain.model.ProductCategory

class ClassifyConsumptionProfileUseCase {

    operator fun invoke(metrics: ConsumptionMetrics): ConsumptionProfile {
        if (metrics.totalReceipts == 0) {
            return ConsumptionProfile.UNDEFINED
        }

        val basicFood = metrics.valuePercentageByCategory[ProductCategory.BASIC_FOOD] ?: 0.0
        val industrialized = metrics.valuePercentageByCategory[ProductCategory.INDUSTRIALIZED] ?: 0.0
        val beverages = metrics.valuePercentageByCategory[ProductCategory.BEVERAGES] ?: 0.0
        val hygiene = metrics.valuePercentageByCategory[ProductCategory.HYGIENE] ?: 0.0
        val cleaning = metrics.valuePercentageByCategory[ProductCategory.CLEANING] ?: 0.0

        val maxCategoryPercentage = listOf(
            basicFood,
            industrialized,
            beverages,
            hygiene,
            cleaning
        ).max()

        return when {
            maxCategoryPercentage >= 0.70 -> ConsumptionProfile.CONCENTRATED
            beverages >= 0.35 -> ConsumptionProfile.HIGH_BEVERAGES
            industrialized >= 0.35 -> ConsumptionProfile.HIGH_INDUSTRIALIZED
            basicFood >= 0.55 -> ConsumptionProfile.BASIC_FOOD_FOCUSED
            hygiene >= 0.30 -> ConsumptionProfile.PERSONAL_CARE_FOCUSED
            cleaning + hygiene >= 0.40 -> ConsumptionProfile.DOMESTIC_FOCUSED
            else -> ConsumptionProfile.BALANCED
        }
    }
}

