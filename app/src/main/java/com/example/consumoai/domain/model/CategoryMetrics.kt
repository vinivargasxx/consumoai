package com.example.consumoai.domain.model

data class CategoryMetrics(
    val category: ProductCategory,
    val totalValue: Double,
    val totalItems: Int,
    val valuePercentage: Double,
    val itemPercentage: Double,
    val frequency: Double,
    val averageValuePerReceipt: Double,
    val averageItemsPerReceipt: Double
)

