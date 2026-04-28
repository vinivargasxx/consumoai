package com.example.consumoai.presentation.home
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.consumoai.domain.model.ConsumptionPeriod
import com.example.consumoai.domain.usecase.AnalyzeConsumptionByPeriodUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
class HomeViewModel(
    private val analyzeConsumptionByPeriodUseCase: AnalyzeConsumptionByPeriodUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    init {
        analyzePeriod(ConsumptionPeriod.LAST_30_DAYS)
    }
    fun onAction(action: HomeScreenAction) {
        when (action) {
            is HomeScreenAction.OnPeriodSelected -> analyzePeriod(action.period)
            HomeScreenAction.OnClearError -> clearError()
        }
    }
    private fun analyzePeriod(period: ConsumptionPeriod) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                selectedPeriod = period,
                errorMessage = null
            )
            runCatching {
                analyzeConsumptionByPeriodUseCase(period)
            }.onSuccess { analysis ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    analysis = analysis
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Erro ao analisar consumo"
                )
            }
        }
    }
    private fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
