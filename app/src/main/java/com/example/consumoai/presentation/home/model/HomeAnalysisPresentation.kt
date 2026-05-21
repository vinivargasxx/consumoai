package com.example.consumoai.presentation.home.model

data class HomeAnalysisPresentation(
    val profileTitle: String,
    val profileDescription: String,
    val consumptionReading: String,
    val confidenceLabel: String,
    val sourceLabel: String,
    val sourceWarning: String?,
    val primarySignals: List<String>,
    val technicalItems: List<Pair<String, String>>
)

