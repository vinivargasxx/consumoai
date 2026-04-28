package com.example.consumoai.core.di

import com.example.consumoai.domain.usecase.AnalyzeConsumptionByPeriodUseCase
import com.example.consumoai.domain.usecase.CalculateConsumptionMetricsUseCase
import com.example.consumoai.domain.usecase.ClassifyConsumptionProfileUseCase
import com.example.consumoai.domain.usecase.GetPeriodDateRangeUseCase
import org.koin.dsl.module

val domainModule = module {

    factory {
        GetPeriodDateRangeUseCase()
    }

    factory {
        CalculateConsumptionMetricsUseCase()
    }

    factory {
        ClassifyConsumptionProfileUseCase()
    }

    factory {
        AnalyzeConsumptionByPeriodUseCase(
            receiptRepository = get(),
            getPeriodDateRangeUseCase = get(),
            calculateConsumptionMetricsUseCase = get(),
            classifyConsumptionProfileUseCase = get()
        )
    }
}

