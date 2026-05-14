package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.CategoryMetrics
import com.example.consumoai.domain.model.ConsumptionMetrics
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.Receipt

class CalculateConsumptionMetricsUseCase {

    operator fun invoke(receipts: List<Receipt>): ConsumptionMetrics {
        if (receipts.isEmpty()) {
            return ConsumptionMetrics(
                valuePercentageByCategory = emptyCategoryMap(),
                itemPercentageByCategory = emptyCategoryMap(),
                frequencyByCategory = emptyCategoryMap(),
                categoryMetrics = emptyCategoryMetricsMap(),
                categoryValueTotals = emptyDoubleMap(),
                categoryItemTotals = emptyIntMap(),
                totalReceipts = 0,
                totalItems = 0,
                totalValue = 0.0,
                averageTicket = 0.0,
                averageItemsPerReceipt = 0.0,
                maxCategoryByValue = null,
                maxCategoryByItems = null,
                receiptAverageValueByCategory = emptyCategoryMap(),
                categoryConcentrationIndex = 0.0,
                topThreeCategoriesByValue = emptyList(),
                averageValuePerItem = 0.0,
                highestReceiptValue = 0.0,
                lowestReceiptValue = 0.0,
                receiptValueAmplitude = 0.0,
                highValueReceiptsPercentage = 0.0,
                lowValueReceiptsPercentage = 0.0,
                categoryDominanceGap = 0.0,
                topThreeCategoriesValuePercentage = 0.0,
                otherPercentageByValue = 0.0,
                otherPercentageByItems = 0.0,
                classifiedItemsPercentage = 0.0,
                averageCategoriesPerReceipt = 0.0,
                categoryDiversityIndex = 0.0,
                essentialCategoriesPercentage = 0.0,
                nonEssentialCategoriesPercentage = 0.0,
                industrializedToBasicFoodRatio = 0.0,
                beveragesToBasicFoodRatio = 0.0,
                beveragesToTotalRatio = 0.0,
                produceToTotalRatio = 0.0,
                receiptsWithIndustrializedPercentage = 0.0,
                receiptsWithBeveragesPercentage = 0.0,
                receiptsWithBasicFoodPercentage = 0.0,
                receiptsWithProducePercentage = 0.0,
                receiptsWithHygienePercentage = 0.0,
                receiptsWithCleaningPercentage = 0.0,
                averageIndustrializedItemsPerReceipt = 0.0,
                averageBeveragesItemsPerReceipt = 0.0,
                averageBasicFoodItemsPerReceipt = 0.0,
                averageProduceItemsPerReceipt = 0.0,
                convenienceScore = 0.0,
                essentialScore = 0.0,
                diversityScore = 0.0
            )
        }

        val allItems = receipts.flatMap { it.items }
        val totalValue = receipts.sumOf { it.totalValue }
        val totalItems = allItems.size
        val totalReceipts = receipts.size

        val categoryValueTotals = ProductCategory.entries.associateWith { category ->
            allItems.filter { it.category == category }.sumOf { it.price }
        }

        val categoryItemTotals = ProductCategory.entries.associateWith { category ->
            allItems.count { it.category == category }
        }

        val valuePercentageByCategory = ProductCategory.entries.associateWith { category ->
            safeDivide(categoryValueTotals.valueOf(category), totalValue)
        }

        val itemPercentageByCategory = ProductCategory.entries.associateWith { category ->
            safeDivide(categoryItemTotals.countOf(category).toDouble(), totalItems.toDouble())
        }

        val frequencyByCategory = ProductCategory.entries.associateWith { category ->
            val receiptsWithCategory = receipts.count { receipt -> receipt.items.any { it.category == category } }
            safeDivide(receiptsWithCategory.toDouble(), totalReceipts.toDouble())
        }

        val categoryMetrics = ProductCategory.entries.associateWith { category ->
            CategoryMetrics(
                category = category,
                totalValue = categoryValueTotals.valueOf(category),
                totalItems = categoryItemTotals.countOf(category),
                valuePercentage = valuePercentageByCategory.valueOf(category),
                itemPercentage = itemPercentageByCategory.valueOf(category),
                frequency = frequencyByCategory.valueOf(category),
                averageValuePerReceipt = safeDivide(categoryValueTotals.valueOf(category), totalReceipts.toDouble()),
                averageItemsPerReceipt = safeDivide(categoryItemTotals.countOf(category).toDouble(), totalReceipts.toDouble())
            )
        }

        val averageTicket = safeDivide(totalValue, totalReceipts.toDouble())
        val averageItemsPerReceipt = safeDivide(totalItems.toDouble(), totalReceipts.toDouble())
        val averageValuePerItem = safeDivide(totalValue, totalItems.toDouble())
        val receiptAverageValueByCategory = ProductCategory.entries.associateWith { category ->
            safeDivide(categoryValueTotals.valueOf(category), totalReceipts.toDouble())
        }
        val orderedValueCategories = orderedCategoriesByValue(valuePercentageByCategory)
        val maxCategoryByValue = orderedValueCategories.firstOrNull()?.takeIf { valuePercentageByCategory.valueOf(it) > 0.0 }
        val maxCategoryByItems = orderedCategoriesByItems(categoryItemTotals).firstOrNull()?.takeIf { categoryItemTotals.countOf(it) > 0 }
        val categoryConcentrationIndex = valuePercentageByCategory.values.maxOrNull() ?: 0.0
        val topThreeCategoriesByValue = orderedValueCategories.take(3)
        val topThreeCategoriesValuePercentage = orderedValueCategories.take(3).sumOf { valuePercentageByCategory.valueOf(it) }.coerceIn(0.0, 1.0)
        val categoryDominanceGap = if (orderedValueCategories.isNotEmpty()) {
            val first = valuePercentageByCategory.valueOf(orderedValueCategories.first())
            val second = orderedValueCategories.getOrNull(1)?.let { valuePercentageByCategory.valueOf(it) } ?: 0.0
            (first - second).coerceAtLeast(0.0)
        } else {
            0.0
        }
        val highestReceiptValue = receipts.maxOf { it.totalValue }
        val lowestReceiptValue = receipts.minOf { it.totalValue }
        val receiptValueAmplitude = highestReceiptValue - lowestReceiptValue
        val averageCategoriesPerReceipt = safeDivide(
            receipts.sumOf { receipt -> receipt.items.map { it.category }.distinct().size.toDouble() },
            totalReceipts.toDouble()
        )
        val categoryDiversityIndex = safeDivide(
            valuePercentageByCategory.count { it.value > 0.0 }.toDouble(),
            ProductCategory.entries.size.toDouble()
        )
        val essentialCategoriesPercentage = (
            valuePercentageByCategory.valueOf(ProductCategory.BASIC_FOOD) +
                valuePercentageByCategory.valueOf(ProductCategory.PRODUCE) +
                valuePercentageByCategory.valueOf(ProductCategory.HYGIENE) +
                valuePercentageByCategory.valueOf(ProductCategory.CLEANING)
            ).coerceIn(0.0, 1.0)
        val nonEssentialCategoriesPercentage = (
            valuePercentageByCategory.valueOf(ProductCategory.INDUSTRIALIZED) +
                valuePercentageByCategory.valueOf(ProductCategory.BEVERAGES) +
                valuePercentageByCategory.valueOf(ProductCategory.OTHER)
            ).coerceIn(0.0, 1.0)
        val industrializedValue = categoryValueTotals.valueOf(ProductCategory.INDUSTRIALIZED)
        val basicFoodValue = categoryValueTotals.valueOf(ProductCategory.BASIC_FOOD)
        val beveragesValue = categoryValueTotals.valueOf(ProductCategory.BEVERAGES)
        val produceValue = categoryValueTotals.valueOf(ProductCategory.PRODUCE)
        val industrializedToBasicFoodRatio = safeDivide(industrializedValue, basicFoodValue)
        val beveragesToBasicFoodRatio = safeDivide(beveragesValue, basicFoodValue)
        val beveragesToTotalRatio = valuePercentageByCategory.valueOf(ProductCategory.BEVERAGES)
        val produceToTotalRatio = valuePercentageByCategory.valueOf(ProductCategory.PRODUCE)
        val receiptsWithIndustrializedPercentage = frequencyByCategory.valueOf(ProductCategory.INDUSTRIALIZED)
        val receiptsWithBeveragesPercentage = frequencyByCategory.valueOf(ProductCategory.BEVERAGES)
        val receiptsWithBasicFoodPercentage = frequencyByCategory.valueOf(ProductCategory.BASIC_FOOD)
        val receiptsWithProducePercentage = frequencyByCategory.valueOf(ProductCategory.PRODUCE)
        val receiptsWithHygienePercentage = frequencyByCategory.valueOf(ProductCategory.HYGIENE)
        val receiptsWithCleaningPercentage = frequencyByCategory.valueOf(ProductCategory.CLEANING)
        val averageIndustrializedItemsPerReceipt = safeDivide(
            categoryItemTotals.countOf(ProductCategory.INDUSTRIALIZED).toDouble(),
            totalReceipts.toDouble()
        )
        val averageBeveragesItemsPerReceipt = safeDivide(
            categoryItemTotals.countOf(ProductCategory.BEVERAGES).toDouble(),
            totalReceipts.toDouble()
        )
        val averageBasicFoodItemsPerReceipt = safeDivide(
            categoryItemTotals.countOf(ProductCategory.BASIC_FOOD).toDouble(),
            totalReceipts.toDouble()
        )
        val averageProduceItemsPerReceipt = safeDivide(
            categoryItemTotals.countOf(ProductCategory.PRODUCE).toDouble(),
            totalReceipts.toDouble()
        )
        val highValueReceiptsPercentage = safeDivide(
            receipts.count { it.totalValue > averageTicket }.toDouble(),
            totalReceipts.toDouble()
        )
        val lowValueReceiptsPercentage = safeDivide(
            receipts.count { it.totalValue < averageTicket }.toDouble(),
            totalReceipts.toDouble()
        )
        val classifiedItemsPercentage = (1.0 - (itemPercentageByCategory.valueOf(ProductCategory.OTHER))).coerceIn(0.0, 1.0)
        val otherPercentageByValue = valuePercentageByCategory.valueOf(ProductCategory.OTHER)
        val otherPercentageByItems = itemPercentageByCategory.valueOf(ProductCategory.OTHER)
        val convenienceScore = ((valuePercentageByCategory.valueOf(ProductCategory.INDUSTRIALIZED) + beveragesToTotalRatio + receiptsWithIndustrializedPercentage) / 3.0).coerceIn(0.0, 1.0)
        val essentialScore = ((essentialCategoriesPercentage + receiptsWithBasicFoodPercentage + receiptsWithProducePercentage) / 3.0).coerceIn(0.0, 1.0)
        val diversityScore = ((categoryDiversityIndex + safeDivide(averageCategoriesPerReceipt, ProductCategory.entries.size.toDouble())) / 2.0).coerceIn(0.0, 1.0)

        val metrics = ConsumptionMetrics(
            valuePercentageByCategory = valuePercentageByCategory,
            itemPercentageByCategory = itemPercentageByCategory,
            frequencyByCategory = frequencyByCategory,
            categoryMetrics = categoryMetrics,
            categoryValueTotals = categoryValueTotals,
            categoryItemTotals = categoryItemTotals,
            totalReceipts = totalReceipts,
            totalItems = totalItems,
            totalValue = totalValue,
            averageTicket = averageTicket,
            averageItemsPerReceipt = averageItemsPerReceipt,
            maxCategoryByValue = maxCategoryByValue,
            maxCategoryByItems = maxCategoryByItems,
            receiptAverageValueByCategory = receiptAverageValueByCategory,
            categoryConcentrationIndex = categoryConcentrationIndex,
            topThreeCategoriesByValue = topThreeCategoriesByValue,
            averageValuePerItem = averageValuePerItem,
            highestReceiptValue = highestReceiptValue,
            lowestReceiptValue = lowestReceiptValue,
            receiptValueAmplitude = receiptValueAmplitude,
            highValueReceiptsPercentage = highValueReceiptsPercentage,
            lowValueReceiptsPercentage = lowValueReceiptsPercentage,
            categoryDominanceGap = categoryDominanceGap,
            topThreeCategoriesValuePercentage = topThreeCategoriesValuePercentage,
            otherPercentageByValue = otherPercentageByValue,
            otherPercentageByItems = otherPercentageByItems,
            classifiedItemsPercentage = classifiedItemsPercentage,
            averageCategoriesPerReceipt = averageCategoriesPerReceipt,
            categoryDiversityIndex = categoryDiversityIndex,
            essentialCategoriesPercentage = essentialCategoriesPercentage,
            nonEssentialCategoriesPercentage = nonEssentialCategoriesPercentage,
            industrializedToBasicFoodRatio = industrializedToBasicFoodRatio,
            beveragesToBasicFoodRatio = beveragesToBasicFoodRatio,
            beveragesToTotalRatio = beveragesToTotalRatio,
            produceToTotalRatio = produceToTotalRatio,
            receiptsWithIndustrializedPercentage = receiptsWithIndustrializedPercentage,
            receiptsWithBeveragesPercentage = receiptsWithBeveragesPercentage,
            receiptsWithBasicFoodPercentage = receiptsWithBasicFoodPercentage,
            receiptsWithProducePercentage = receiptsWithProducePercentage,
            receiptsWithHygienePercentage = receiptsWithHygienePercentage,
            receiptsWithCleaningPercentage = receiptsWithCleaningPercentage,
            averageIndustrializedItemsPerReceipt = averageIndustrializedItemsPerReceipt,
            averageBeveragesItemsPerReceipt = averageBeveragesItemsPerReceipt,
            averageBasicFoodItemsPerReceipt = averageBasicFoodItemsPerReceipt,
            averageProduceItemsPerReceipt = averageProduceItemsPerReceipt,
            convenienceScore = convenienceScore,
            essentialScore = essentialScore,
            diversityScore = diversityScore
        )

        return metrics
    }

    private fun emptyCategoryMap(): Map<ProductCategory, Double> {
        return ProductCategory.entries.associateWith { 0.0 }
    }

    private fun emptyDoubleMap(): Map<ProductCategory, Double> = emptyCategoryMap()

    private fun emptyIntMap(): Map<ProductCategory, Int> {
        return ProductCategory.entries.associateWith { 0 }
    }

    private fun emptyCategoryMetricsMap(): Map<ProductCategory, CategoryMetrics> {
        return ProductCategory.entries.associateWith { category ->
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
    }

    private fun safeDivide(numerator: Double, denominator: Double): Double {
        return if (denominator == 0.0) 0.0 else numerator / denominator
    }

    private fun Map<ProductCategory, Double>.valueOf(category: ProductCategory): Double {
        return this[category] ?: 0.0
    }

    private fun Map<ProductCategory, Int>.countOf(category: ProductCategory): Int {
        return this[category] ?: 0
    }

    private fun orderedCategoriesByValue(values: Map<ProductCategory, Double>): List<ProductCategory> {
        return ProductCategory.entries
            .sortedWith(compareByDescending<ProductCategory> { values.valueOf(it) }.thenBy { it.ordinal })
            .filter { values.valueOf(it) > 0.0 }
    }

    private fun orderedCategoriesByItems(values: Map<ProductCategory, Int>): List<ProductCategory> {
        return ProductCategory.entries
            .sortedWith(compareByDescending<ProductCategory> { values.countOf(it) }.thenBy { it.ordinal })
            .filter { values.countOf(it) > 0 }
    }
}

