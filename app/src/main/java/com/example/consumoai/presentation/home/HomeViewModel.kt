package com.example.consumoai.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.consumoai.domain.usecase.AnalyzeStoredReceiptsUseCase
import com.example.consumoai.domain.usecase.ClearReceiptsUseCase
import com.example.consumoai.domain.usecase.GetStoredReceiptsSummaryUseCase
import com.example.consumoai.domain.usecase.ImportSampleNfceReceiptsUseCase
import com.example.consumoai.presentation.home.model.toHomeAnalysisPresentation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val importSampleNfceReceiptsUseCase: ImportSampleNfceReceiptsUseCase,
    private val analyzeStoredReceiptsUseCase: AnalyzeStoredReceiptsUseCase,
    private val getStoredReceiptsSummaryUseCase: GetStoredReceiptsSummaryUseCase,
    private val clearReceiptsUseCase: ClearReceiptsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onAction(action: HomeScreenAction) {
        when (action) {
            HomeScreenAction.OnImportSampleNfceUrlsClick -> importSampleReceipts()
            HomeScreenAction.OnAnalyzeStoredReceiptsClick -> analyzeStoredReceipts()
            HomeScreenAction.OnClearReceiptsClick -> clearReceipts()
        }
    }

    private fun importSampleReceipts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true, errorMessage = null)

            runCatching {
                val result = importSampleNfceReceiptsUseCase()
                val summary = getStoredReceiptsSummaryUseCase()
                result to summary
            }.onSuccess { (result, summary) ->
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    importResult = result,
                    localSummary = summary,
                    storedAnalysis = null,
                    analysisPresentation = null
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    errorMessage = error.message ?: "Erro ao importar notas NFC-e"
                )
            }
        }
    }

    private fun analyzeStoredReceipts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzing = true, errorMessage = null)

            runCatching {
                val analysis = analyzeStoredReceiptsUseCase()
                val summary = getStoredReceiptsSummaryUseCase()
                analysis to summary
            }.onSuccess { (analysis, summary) ->

                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    localSummary = summary,
                    storedAnalysis = analysis,
                    analysisPresentation = analysis.toHomeAnalysisPresentation()
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    errorMessage = error.message ?: "Erro ao analisar notas armazenadas"
                )
            }
        }
    }

    private fun clearReceipts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isImporting = true,
                isAnalyzing = true,
                errorMessage = null
            )

            runCatching {
                clearReceiptsUseCase()
            }.onSuccess {
                _uiState.value = HomeUiState()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    isAnalyzing = false,
                    errorMessage = error.message ?: "Erro ao limpar notas locais"
                )
            }
        }
    }
}
