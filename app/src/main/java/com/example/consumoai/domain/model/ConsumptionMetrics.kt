package com.example.consumoai.domain.model

/**
 * Full analytics payload used by the app UI, debugging, and future experiments.
 * Nem todas as métricas calculadas fazem parte da entrada oficial do modelo.
 */
data class ConsumptionMetrics(
    // Métricas por categoria
    val valuePercentageByCategory: Map<ProductCategory, Double>,
    val itemPercentageByCategory: Map<ProductCategory, Double>,
    val frequencyByCategory: Map<ProductCategory, Double>,
    val categoryMetrics: Map<ProductCategory, CategoryMetrics>,

    val categoryValueTotals: Map<ProductCategory, Double>,
    val categoryItemTotals: Map<ProductCategory, Int>,

    // Métricas gerais
    val totalReceipts: Int,
    val totalItems: Int,
    val totalValue: Double,
    val averageTicket: Double,
    val averageItemsPerReceipt: Double,
    val maxCategoryByValue: ProductCategory?,
    val maxCategoryByItems: ProductCategory?,
    val receiptAverageValueByCategory: Map<ProductCategory, Double>,
    val categoryConcentrationIndex: Double,
    val topThreeCategoriesByValue: List<ProductCategory>,
    val averageValuePerItem: Double,
    val highestReceiptValue: Double,
    val lowestReceiptValue: Double,
    val receiptValueAmplitude: Double,
    val highValueReceiptsPercentage: Double,
    val lowValueReceiptsPercentage: Double,
    val categoryDominanceGap: Double,
    val topThreeCategoriesValuePercentage: Double,
    val otherPercentageByValue: Double,
    val otherPercentageByItems: Double,
    val classifiedItemsPercentage: Double,
    val averageCategoriesPerReceipt: Double,
    val categoryDiversityIndex: Double,

    // Métricas comportamentais
    val essentialCategoriesPercentage: Double,
    val nonEssentialCategoriesPercentage: Double,
    val industrializedToBasicFoodRatio: Double,
    val beveragesToBasicFoodRatio: Double,
    val beveragesToTotalRatio: Double,
    val produceToTotalRatio: Double,
    val receiptsWithIndustrializedPercentage: Double,
    val receiptsWithBeveragesPercentage: Double,
    val receiptsWithBasicFoodPercentage: Double,
    val receiptsWithProducePercentage: Double,
    val receiptsWithHygienePercentage: Double,
    val receiptsWithCleaningPercentage: Double,
    val averageIndustrializedItemsPerReceipt: Double,
    val averageBeveragesItemsPerReceipt: Double,
    val averageBasicFoodItemsPerReceipt: Double,
    val averageProduceItemsPerReceipt: Double,

    // Features oficiais aproveitadas pela IA V1
    val convenienceScore: Double,
    val essentialScore: Double,
    val diversityScore: Double
)