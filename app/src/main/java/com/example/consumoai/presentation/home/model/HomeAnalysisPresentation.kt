package com.example.consumoai.presentation.home.model

import com.example.consumoai.domain.model.ProfileInterpretationType

data class HomeAnalysisPresentation(
    val profileTitle: String,
    val profileDescription: String,
    val consumptionReading: String,
    val confidenceLabel: String,
    val sourceLabel: String,
    val sourceWarning: String?,
    val primarySignals: List<String>,
    val technicalItems: List<Pair<String, String>>,
    val interpretationType: ProfileInterpretationType = ProfileInterpretationType.PURE_PROFILE,
    val secondaryProfileTitle: String? = null,
    val secondaryProfileDescription: String? = null,
    val explanationSignals: List<String> = emptyList(),
    val technicalExplanation: List<String> = emptyList()
)

