package com.example.consumoai.core.di

import com.example.consumoai.domain.insights.ConsumptionInsightsEngine
import com.example.consumoai.domain.insights.DefaultConsumptionInsightsEngine
import com.example.consumoai.domain.usecase.AnalyzeReceiptFromQrCodeUrlUseCase
import com.example.consumoai.domain.usecase.AnalyzeStoredReceiptsUseCase
import com.example.consumoai.domain.usecase.BuildConsumptionModelInputUseCase
import com.example.consumoai.domain.usecase.BuildConsumptionProfileSummaryUseCase
import com.example.consumoai.domain.usecase.CalculateConsumptionMetricsUseCase
import com.example.consumoai.domain.usecase.ClearReceiptsUseCase
import com.example.consumoai.domain.usecase.ClassifyConsumptionProfileUseCase
import com.example.consumoai.domain.usecase.ClassifyProductsUseCase
import com.example.consumoai.domain.usecase.ConsumptionFeatureSanitizer
import com.example.consumoai.domain.usecase.GetStoredReceiptsSummaryUseCase
import com.example.consumoai.domain.usecase.ImportSampleNfceReceiptsUseCase
import com.example.consumoai.domain.usecase.SaveReceiptUseCase
import org.koin.dsl.module

val domainModule = module {

    factory {
        AnalyzeReceiptFromQrCodeUrlUseCase(
            nfceQrCodeDataSource = get(),
            classifyProductsUseCase = get()
        )
    }

    factory {
        ClassifyProductsUseCase(
            productClassifier = get()
        )
    }

    factory {
        CalculateConsumptionMetricsUseCase(
            semanticTagger = get()
        )
    }

    factory { BuildConsumptionModelInputUseCase() }

    factory { ConsumptionFeatureSanitizer() }

    factory { BuildConsumptionProfileSummaryUseCase() }


    factory {
        ClassifyConsumptionProfileUseCase(
            consumptionBehaviorClassifier = get()
        )
    }


    factory {
        SaveReceiptUseCase(
            receiptRepository = get()
        )
    }

    factory {
        ImportSampleNfceReceiptsUseCase(
            analyzeReceiptFromQrCodeUrlUseCase = get(),
            saveReceiptUseCase = get(),
            receiptRepository = get()
        )
    }

    single<ConsumptionInsightsEngine> {
        DefaultConsumptionInsightsEngine()
    }

    factory {
        AnalyzeStoredReceiptsUseCase(
            receiptRepository = get(),
            calculateConsumptionMetricsUseCase = get(),
            buildConsumptionModelInputUseCase = get(),
            classifyConsumptionProfileUseCase = get(),
            insightsEngine = get(),
            consumptionFeatureSanitizer = get(),
            buildConsumptionProfileSummaryUseCase = get()
        )
    }

    factory {
        GetStoredReceiptsSummaryUseCase(
            receiptRepository = get()
        )
    }

    factory {
        ClearReceiptsUseCase(
            receiptRepository = get()
        )
    }

}
