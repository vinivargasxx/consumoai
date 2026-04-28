package com.example.consumoai.presentation.home
import com.example.consumoai.domain.model.ConsumptionPeriod
import com.example.consumoai.domain.model.ReceiptAnalysis
data class HomeUiState(
    val isLoading: Boolean = false,
    val selectedPeriod: ConsumptionPeriod = ConsumptionPeriod.LAST_30_DAYS,
    val analysis: ReceiptAnalysis? = null,
    val errorMessage: String? = null
)
