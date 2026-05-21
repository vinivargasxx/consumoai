package com.example.consumoai.domain.usecase

import com.example.consumoai.data.classifier.RuleBasedConsumptionBehaviorClassifier
import com.example.consumoai.data.classifier.KeywordProductSemanticTagger
import com.example.consumoai.domain.insights.DefaultConsumptionInsightsEngine
import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.MODEL_FEATURE_COUNT
import com.example.consumoai.domain.model.MODEL_FINAL_FEATURES
import com.example.consumoai.domain.model.MODEL_INPUT_VERSION
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource
import com.example.consumoai.domain.repository.ReceiptRepository
import com.example.consumoai.domain.classifier.ConsumptionBehaviorClassifier
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
            calculateConsumptionMetricsUseCase = CalculateConsumptionMetricsUseCase(
                semanticTagger = KeywordProductSemanticTagger()
            ),
            buildConsumptionModelInputUseCase = BuildConsumptionModelInputUseCase(),
            classifyConsumptionProfileUseCase = ClassifyConsumptionProfileUseCase(
                consumptionBehaviorClassifier = RuleBasedConsumptionBehaviorClassifier()
            ),
            insightsEngine = DefaultConsumptionInsightsEngine(),
            consumptionFeatureSanitizer = ConsumptionFeatureSanitizer(),
            buildConsumptionProfileSummaryUseCase = BuildConsumptionProfileSummaryUseCase()
        )()

        assertEquals(2, analysis.receipts.size)
        assertEquals(2, analysis.metrics.totalReceipts)
        assertEquals(3, analysis.metrics.totalItems)
        assertEquals(22.5, analysis.metrics.totalValue, 0.0001)
        assertEquals(MODEL_INPUT_VERSION, analysis.modelInput.version)
        assertEquals(MODEL_FEATURE_COUNT, analysis.modelInput.features.size)
        assertEquals(MODEL_FINAL_FEATURES, analysis.modelInput.features.keys.toList())
        assertEquals(BehaviorClassificationSource.RULE_BASED_FALLBACK, analysis.behaviorResult.source)
        assertEquals(true, analysis.behaviorResult.profileSummary != null)
        assertEquals(analysis.behaviorResult.mainProfile, analysis.behaviorAnalysis.behaviorResult.mainProfile)
        assertEquals(true, analysis.behaviorAnalysis.insights.isNotEmpty())
        assertEquals(true, analysis.behaviorAnalysis.behavioralComposition.isNotEmpty())
        assertEquals(true, analysis.behaviorAnalysis.summary.isNotEmpty())
    }

    @Test
    fun invoke_sendsFinalInputToClassifierWithTop15Features() = runBlocking {
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
                )
            )

            override suspend fun clearReceipts() = Unit

            override suspend fun existsByAccessKeyOrUrl(accessKeyOrUrl: String): Boolean = false
        }

        var capturedVersion: String? = null
        var capturedFeatureCount: Int? = null

        val classifier = object : ConsumptionBehaviorClassifier {
            override suspend fun classify(input: com.example.consumoai.domain.model.ConsumptionModelInput): ConsumptionBehaviorResult {
                capturedVersion = input.version
                capturedFeatureCount = input.features.size
                return ConsumptionBehaviorResult(
                    mainProfile = ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT,
                    confidence = 0.8,
                    profileScores = mapOf(ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT to 0.8),
                    source = BehaviorClassificationSource.RULE_BASED_FALLBACK
                )
            }
        }

        val analysis = AnalyzeStoredReceiptsUseCase(
            receiptRepository = repository,
            calculateConsumptionMetricsUseCase = CalculateConsumptionMetricsUseCase(
                semanticTagger = KeywordProductSemanticTagger()
            ),
            buildConsumptionModelInputUseCase = BuildConsumptionModelInputUseCase(),
            classifyConsumptionProfileUseCase = ClassifyConsumptionProfileUseCase(
                consumptionBehaviorClassifier = classifier
            ),
            insightsEngine = DefaultConsumptionInsightsEngine(),
            consumptionFeatureSanitizer = ConsumptionFeatureSanitizer(),
            buildConsumptionProfileSummaryUseCase = BuildConsumptionProfileSummaryUseCase()
        )()

        assertEquals(MODEL_INPUT_VERSION, capturedVersion)
        assertEquals(MODEL_FEATURE_COUNT, capturedFeatureCount)
        assertEquals(MODEL_INPUT_VERSION, analysis.modelInput.version)
        assertEquals(MODEL_FEATURE_COUNT, analysis.modelInput.features.size)
    }
}

