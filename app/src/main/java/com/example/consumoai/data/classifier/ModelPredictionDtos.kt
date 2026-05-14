package com.example.consumoai.data.classifier
data class ModelPredictionRequestDto(
    val version: String,
    val features: Map<String, Double>
)
data class ModelPredictionResponseDto(
    val main_profile: String,
    val confidence: Double,
    val profile_scores: Map<String, Double>
)
