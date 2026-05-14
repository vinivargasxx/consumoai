package com.example.consumoai.domain.usecase

import com.example.consumoai.data.classifier.RuleBasedConsumptionBehaviorClassifier
import com.example.consumoai.domain.insights.DefaultConsumptionInsightsEngine
import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.MODEL_INPUT_VERSION
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource
import com.example.consumoai.domain.repository.ReceiptRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyzeStoredReceiptsUseCaseTest {

    @Test
    fun invoke_returnsStoredAnalysisFromRepository() = runBlocking {
        val repository = object : ReceiptRepository {
            override suspend fun saveReceipt(receipt: Receipt) = Unit

            override suspend fun getAllReceipts(): List<Receipt> = listOf(
                Receipt(
                    id = 1L,
                    accessKeyOrUrl = "u1",
                    source = ReceiptSource.QR_CODE,
                    items = listOf(
                        ProductItem(name = "SUCO", price = 10.0, category = ProductCategory.BEVERAGES),
                        ProductItem(name = "PAO", price = 5.0, category = ProductCategory.BASIC_FOOD)
                    )
                ),
                Receipt(
                    id = 2L,
                    accessKeyOrUrl = "u2",
                    source = ReceiptSource.QR_CODE,
                    items = listOf(
                        ProductItem(name = "LEITE", price = 7.5, category = ProductCategory.BASIC_FOOD)
                    )
                )
            )

            override suspend fun clearReceipts() = Unit

            override suspend fun existsByAccessKeyOrUrl(accessKeyOrUrl: String): Boolean = false
        }

        val analysis = AnalyzeStoredReceiptsUseCase(
            receiptRepository = repository,
            calculateConsumptionMetricsUseCase = CalculateConsumptionMetricsUseCase(),
            buildConsumptionModelInputUseCase = BuildConsumptionModelInputUseCase(),
            classifyConsumptionProfileUseCase = ClassifyConsumptionProfileUseCase(
                consumptionBehaviorClassifier = RuleBasedConsumptionBehaviorClassifier()
            ),
            insightsEngine = DefaultConsumptionInsightsEngine(),
            consumptionFeatureSanitizer = ConsumptionFeatureSanitizer(),
            buildConsumptionProfileSummaryUseCase = BuildConsumptionProfileSummaryUseCase(),
            profileExplanationBuilder = ProfileExplanationBuilder(),
            buildAnonymizedConsumptionExportUseCase = BuildAnonymizedConsumptionExportUseCase()
        )()

        assertEquals(2, analysis.receipts.size)
        assertEquals(2, analysis.metrics.totalReceipts)
        assertEquals(3, analysis.metrics.totalItems)
        assertEquals(22.5, analysis.metrics.totalValue, 0.0001)
        assertEquals(MODEL_INPUT_VERSION, analysis.modelInput.version)
        assertEquals(analysis.metrics.totalReceipts.toDouble(), analysis.modelInput.features["total_receipts"] ?: -1.0, 0.0001)
        assertEquals(analysis.metrics.averageTicket, analysis.modelInput.features["average_ticket"] ?: -1.0, 0.0001)
        assertEquals(27, analysis.modelInput.features.size)
        assertEquals(ConsumptionBehaviorProfile.BEVERAGE_RECURRENT, analysis.behaviorResult.mainProfile)
        assertEquals(BehaviorClassificationSource.RULE_BASED_FALLBACK, analysis.behaviorResult.source)
        assertEquals(true, analysis.behaviorResult.profileSummary != null)
        assertEquals(ConsumptionBehaviorProfile.BEVERAGE_RECURRENT, analysis.behaviorAnalysis?.behaviorResult?.mainProfile)
        assertEquals(true, analysis.behaviorAnalysis?.insights?.isNotEmpty() ?: false)
        assertEquals(true, analysis.behaviorAnalysis?.behavioralComposition?.isNotEmpty() ?: false)
        assertEquals(true, analysis.behaviorAnalysis?.summary?.isNotEmpty() ?: false)
        assertEquals(true, analysis.profileExplanation?.isNotBlank() ?: false)
        assertEquals(true, analysis.anonymizedExport != null)
    }
}

