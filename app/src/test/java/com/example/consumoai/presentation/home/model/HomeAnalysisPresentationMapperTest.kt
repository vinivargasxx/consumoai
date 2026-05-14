package com.example.consumoai.presentation.home.model

import com.example.consumoai.domain.insights.DefaultConsumptionInsightsEngine
import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ConsumptionModelInput
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource
import com.example.consumoai.domain.model.StoredConsumptionAnalysis
import com.example.consumoai.domain.usecase.BuildConsumptionModelInputUseCase
import com.example.consumoai.domain.usecase.CalculateConsumptionMetricsUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeAnalysisPresentationMapperTest {

    @Test
    fun toHomeAnalysisPresentation_mapsFriendlyLabelsAndTechnicalData() {
        val receipts = listOf(
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "SUCO", price = 12.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "ARROZ", price = 9.0, category = ProductCategory.BASIC_FOOD)
                )
            ),
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "REFRIGERANTE", price = 8.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "BISCOITO", price = 7.0, category = ProductCategory.INDUSTRIALIZED)
                )
            )
        )

        val metrics = CalculateConsumptionMetricsUseCase()(receipts)
        val modelInput = BuildConsumptionModelInputUseCase()(metrics)
        val behaviorResult = ConsumptionBehaviorResult(
            mainProfile = ConsumptionBehaviorProfile.BEVERAGE_RECURRENT,
            confidence = 0.78,
            profileScores = mapOf(
                ConsumptionBehaviorProfile.BEVERAGE_RECURRENT to 0.46,
                ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED to 0.29,
                ConsumptionBehaviorProfile.LOW_FRESH_FOOD to 0.13
            ),
            source = BehaviorClassificationSource.TRAINED_MODEL
        )
        val behaviorAnalysis = DefaultConsumptionInsightsEngine().generate(metrics, behaviorResult)

        val presentation = StoredConsumptionAnalysis(
            receipts = receipts,
            metrics = metrics,
            modelInput = modelInput,
            behaviorResult = behaviorResult,
            behaviorAnalysis = behaviorAnalysis
        ).toHomeAnalysisPresentation()

        assertEquals("Recorrente em bebidas", presentation.profileTitle)
        assertEquals("Padrão de consumo consistente", presentation.confidenceLabel)
        assertEquals("Modelo treinado", presentation.sourceLabel)
        assertNull(presentation.sourceWarning)
        assertTrue(presentation.mainCharacteristics.isNotEmpty())
        assertTrue(presentation.mainCharacteristics.any { it.contains("Recorrente em bebidas") })
        assertEquals(8, presentation.consumptionSummaryItems.size)
        assertTrue(presentation.technicalItems.any { it.first == "Versão do input" && it.second == "v1" })
        assertTrue(presentation.technicalItems.any { it.first == "Tipo de interpretação" })
        assertTrue(presentation.technicalItems.any { it.first == "Inferência (ms)" })
        assertTrue(presentation.technicalItems.any { it.first == "Itens classificados" })
        assertTrue(presentation.technicalItems.any { it.first == "OTHER por valor" })
        assertTrue(presentation.technicalItems.any { it.first == "OTHER por quantidade" })
        assertTrue(presentation.technicalItems.none { it.first == "Aviso técnico" })
        assertTrue(presentation.technicalItems.any { it.first.startsWith("Feature ") })
    }

    @Test
    fun toHomeAnalysisPresentation_showsFallbackWarningWhenSourceIsLocalFallback() {
        val receipts = listOf(
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "SABAO", price = 5.0, category = ProductCategory.CLEANING)
                )
            )
        )
        val metrics = CalculateConsumptionMetricsUseCase()(receipts)
        val modelInput = ConsumptionModelInput(features = mapOf("total_receipts" to 1.0))
        val behaviorResult = ConsumptionBehaviorResult(
            mainProfile = ConsumptionBehaviorProfile.UNDEFINED,
            confidence = 0.4,
            profileScores = mapOf(ConsumptionBehaviorProfile.UNDEFINED to 1.0),
            source = BehaviorClassificationSource.RULE_BASED_FALLBACK
        )
        val behaviorAnalysis = DefaultConsumptionInsightsEngine().generate(metrics, behaviorResult)

        val presentation = StoredConsumptionAnalysis(
            receipts = receipts,
            metrics = metrics,
            modelInput = modelInput,
            behaviorResult = behaviorResult,
            behaviorAnalysis = behaviorAnalysis
        ).toHomeAnalysisPresentation()

        assertNotNull(presentation.sourceWarning)
        assertTrue(presentation.sourceWarning!!.contains("Backend indisponível"))
        assertEquals("Padrão de consumo variado", presentation.confidenceLabel)
    }

    @Test
    fun toHomeAnalysisPresentation_addsTechnicalWarningWhenClassifiedCoverageIsLow() {
        val receipts = listOf(
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "ITEM DESCONHECIDO", price = 20.0, category = ProductCategory.OTHER),
                    ProductItem(name = "ARROZ", price = 10.0, category = ProductCategory.BASIC_FOOD)
                )
            )
        )

        val metrics = CalculateConsumptionMetricsUseCase()(receipts)
        val modelInput = BuildConsumptionModelInputUseCase()(metrics)
        val behaviorResult = ConsumptionBehaviorResult(
            mainProfile = ConsumptionBehaviorProfile.UNDEFINED,
            confidence = 0.55,
            profileScores = mapOf(ConsumptionBehaviorProfile.UNDEFINED to 1.0),
            source = BehaviorClassificationSource.TRAINED_MODEL
        )
        val behaviorAnalysis = DefaultConsumptionInsightsEngine().generate(metrics, behaviorResult)

        val presentation = StoredConsumptionAnalysis(
            receipts = receipts,
            metrics = metrics,
            modelInput = modelInput,
            behaviorResult = behaviorResult,
            behaviorAnalysis = behaviorAnalysis
        ).toHomeAnalysisPresentation()

        assertTrue(presentation.technicalItems.any {
            it.first == "Aviso técnico" &&
                it.second.contains("muitos itens não classificados")
        })
    }
}

