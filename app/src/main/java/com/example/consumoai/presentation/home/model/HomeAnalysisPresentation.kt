package com.example.consumoai.presentation.home.model

data class HomeAnalysisPresentation(
    val profileTitle: String,
    val profileDescription: String,
    val confidenceLabel: String,
    val sourceLabel: String,
    val sourceWarning: String?,
    val mainCharacteristics: List<String>,
    val consumptionSummaryItems: List<Pair<String, String>>,
    val technicalItems: List<Pair<String, String>>
)

