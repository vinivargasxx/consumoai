package com.example.consumoai.domain.model

data class AnonymizedConsumptionExport(
    val inputVersion: String,
    val finalProfile: String,
    val confidence: Double,
    val source: String,
    val fallbackReason: String?,
    val featureSnapshot: Map<String, Double>,
    val profileScores: Map<String, Double>,
    val aggregatedMetrics: Map<String, Double>
)

