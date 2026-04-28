package com.example.consumoai.presentation.home
import com.example.consumoai.domain.model.ConsumptionPeriod
sealed interface HomeScreenAction {
    data class OnPeriodSelected(
        val period: ConsumptionPeriod
    ) : HomeScreenAction
    data object OnClearError : HomeScreenAction
}
