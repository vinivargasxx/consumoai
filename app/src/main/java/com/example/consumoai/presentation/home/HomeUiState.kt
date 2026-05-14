package com.example.consumoai.presentation.home

import com.example.consumoai.domain.model.ImportReceiptsResult
import com.example.consumoai.domain.model.StoredReceiptsSummary
import com.example.consumoai.domain.model.StoredConsumptionAnalysis
import com.example.consumoai.presentation.home.model.HomeAnalysisPresentation

data class HomeUiState(
    val isImporting: Boolean = false,
    val isAnalyzing: Boolean = false,
    val importResult: ImportReceiptsResult? = null,
    val localSummary: StoredReceiptsSummary? = null,
    val storedAnalysis: StoredConsumptionAnalysis? = null,
    val analysisPresentation: HomeAnalysisPresentation? = null,
    val errorMessage: String? = null
)
