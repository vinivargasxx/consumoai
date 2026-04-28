package com.example.consumoai.domain.model

data class ReceiptAnalysis(
    val receipts: List<Receipt>,
    val metrics: ConsumptionMetrics,
    val profile: ConsumptionProfile
)