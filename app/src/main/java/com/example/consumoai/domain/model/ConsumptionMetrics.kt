package com.example.consumoai.domain.model

data class ConsumptionMetrics(
    val valuePercentageByCategory: Map<ProductCategory, Double>,
    val itemPercentageByCategory: Map<ProductCategory, Double>,
    val frequencyByCategory: Map<ProductCategory, Double>,
    val totalReceipts: Int,
    val periodDays: Int,
    val totalValue: Double,
    val averageTicket: Double
)