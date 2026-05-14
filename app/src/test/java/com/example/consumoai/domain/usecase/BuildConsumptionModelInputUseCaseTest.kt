package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.ConsumptionMetrics
import com.example.consumoai.domain.model.CategoryMetrics
import com.example.consumoai.domain.model.MODEL_INPUT_VERSION
import com.example.consumoai.domain.model.ProductCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildConsumptionModelInputUseCaseTest {

    private val useCase = BuildConsumptionModelInputUseCase()

    @Test
    fun invoke_buildsStableFeatureMapWithoutNaNOrInfinity() {
        val expectedKeys = setOf(
            "total_receipts",
            "total_items",
            "total_value",
            "average_ticket",
            "average_items_per_receipt",
            "basic_food_value_pct",
            "industrialized_value_pct",
            "beverages_value_pct",
            "hygiene_value_pct",
            "cleaning_value_pct",
            "produce_value_pct",
            "other_value_pct",
            "basic_food_frequency",
            "industrialized_frequency",
            "beverages_frequency",
            "produce_frequency",
            "hygiene_frequency",
            "cleaning_frequency",
            "category_concentration_index",
            "category_dominance_gap",
            "category_diversity_index",
            "essential_categories_percentage",
            "non_essential_categories_percentage",
            "convenience_score",
            "essential_score",
            "diversity_score",
            "classified_items_percentage"
        )

        val zeroDoubleMap = ProductCategory.entries.associateWith { 0.0 }
        val zeroIntMap = ProductCategory.entries.associateWith { 0 }
        val categoryMetrics = ProductCategory.entries.associateWith { category ->
            CategoryMetrics(
                category = category,
                totalValue = 0.0,
                totalItems = 0,
                valuePercentage = 0.0,
                itemPercentage = 0.0,
                frequency = 0.0,
                averageValuePerReceipt = 0.0,
                averageItemsPerReceipt = 0.0
            )
        }

        val metrics = ConsumptionMetrics(
            valuePercentageByCategory = zeroDoubleMap + (ProductCategory.BASIC_FOOD to 0.4) + (ProductCategory.INDUSTRIALIZED to 0.2),
            itemPercentageByCategory = zeroDoubleMap + (ProductCategory.BASIC_FOOD to 0.3) + (ProductCategory.OTHER to 0.1),
            frequencyByCategory = zeroDoubleMap + (ProductCategory.BASIC_FOOD to 0.8),
            categoryMetrics = categoryMetrics,
            categoryValueTotals = zeroDoubleMap + (ProductCategory.BASIC_FOOD to 120.0),
            categoryItemTotals = zeroIntMap + (ProductCategory.BASIC_FOOD to 12),
            totalReceipts = 10,
            totalItems = 30,
            totalValue = 300.0,
            averageTicket = 30.0,
            averageItemsPerReceipt = 3.0,
            maxCategoryByValue = ProductCategory.BASIC_FOOD,
            maxCategoryByItems = ProductCategory.BASIC_FOOD,
            receiptAverageValueByCategory = zeroDoubleMap + (ProductCategory.BASIC_FOOD to 12.0),
            categoryConcentrationIndex = 0.4,
            topThreeCategoriesByValue = listOf(ProductCategory.BASIC_FOOD, ProductCategory.INDUSTRIALIZED, ProductCategory.BEVERAGES),
            averageValuePerItem = 10.0,
            highestReceiptValue = 80.0,
            lowestReceiptValue = 10.0,
            receiptValueAmplitude = 70.0,
            highValueReceiptsPercentage = 0.4,
            lowValueReceiptsPercentage = 0.6,
            categoryDominanceGap = 0.2,
            topThreeCategoriesValuePercentage = 0.85,
            otherPercentageByValue = 0.05,
            otherPercentageByItems = 0.1,
            classifiedItemsPercentage = 0.9,
            averageCategoriesPerReceipt = 3.2,
            categoryDiversityIndex = 0.7,
            essentialCategoriesPercentage = 0.6,
            nonEssentialCategoriesPercentage = 0.4,
            industrializedToBasicFoodRatio = 0.5,
            beveragesToBasicFoodRatio = 0.3,
            beveragesToTotalRatio = 0.12,
            produceToTotalRatio = 0.15,
            receiptsWithIndustrializedPercentage = 0.6,
            receiptsWithBeveragesPercentage = 0.5,
            receiptsWithBasicFoodPercentage = 0.9,
            receiptsWithProducePercentage = 0.4,
            receiptsWithHygienePercentage = 0.2,
            receiptsWithCleaningPercentage = 0.3,
            averageIndustrializedItemsPerReceipt = 1.0,
            averageBeveragesItemsPerReceipt = 0.7,
            averageBasicFoodItemsPerReceipt = 1.8,
            averageProduceItemsPerReceipt = 0.5,
            convenienceScore = 0.45,
            essentialScore = 0.63,
            diversityScore = 0.72
        )

        val result = useCase(metrics)

        assertEquals(MODEL_INPUT_VERSION, result.version)
        assertEquals(expectedKeys, result.features.keys)
        assertEquals(10.0, result.features["total_receipts"] ?: -1.0, 0.0001)
        assertEquals(0.4, result.features["basic_food_value_pct"] ?: -1.0, 0.0001)
        assertEquals(0.8, result.features["basic_food_frequency"] ?: -1.0, 0.0001)
        assertEquals(27, result.features.size)
        assertFalse(result.features.containsKey("highest_receipt_value"))
        assertFalse(result.features.containsKey("basic_food_item_pct"))
        assertFalse(result.features.containsKey("top_three_categories_value_percentage"))
        assertFalse(result.features.values.any { it.isNaN() })
        assertFalse(result.features.values.any { it.isInfinite() })
        assertTrue(result.features.values.all { it in Double.NEGATIVE_INFINITY..Double.POSITIVE_INFINITY })
    }
}


