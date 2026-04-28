package com.example.consumoai.core.di

import com.example.consumoai.presentation.home.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel {
        HomeViewModel(
            analyzeConsumptionByPeriodUseCase = get()
        )
    }
}

