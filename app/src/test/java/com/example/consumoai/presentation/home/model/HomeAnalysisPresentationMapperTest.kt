package com.example.consumoai.presentation.home.model

import com.example.consumoai.domain.insights.DefaultConsumptionInsightsEngine
import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ConsumptionModelInput
import com.example.consumoai.domain.model.MODEL_CLASS_COUNT
import com.example.consumoai.domain.model.MODEL_FEATURE_COUNT
import com.example.consumoai.domain.model.MODEL_INPUT_VERSION
import com.example.consumoai.domain.model.MODEL_NAME
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource
import com.example.consumoai.domain.model.StoredConsumptionAnalysis
import com.example.consumoai.data.classifier.KeywordProductSemanticTagger
import com.example.consumoai.domain.usecase.BuildConsumptionModelInputUseCase
import com.example.consumoai.domain.usecase.CalculateConsumptionMetricsUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
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

        val metrics = CalculateConsumptionMetricsUseCase(
            semanticTagger = KeywordProductSemanticTagger()
        )(receipts)
        val modelInput = BuildConsumptionModelInputUseCase()(metrics)
        val behaviorResult = ConsumptionBehaviorResult(
            mainProfile = ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT,
            confidence = 0.78,
            profileScores = mapOf(
                ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT to 0.46,
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

        assertEquals("Recorrência de bebidas não alcoólicas", presentation.profileTitle)
        assertEquals("Padrão de consumo consistente", presentation.confidenceLabel)
        assertEquals("Modelo treinado", presentation.sourceLabel)
        assertNull(presentation.sourceWarning)
        assertTrue(presentation.technicalItems.any { it.first == "Modelo" && it.second == MODEL_NAME })
        assertTrue(presentation.technicalItems.any { it.first == "Features enviadas" && it.second == MODEL_FEATURE_COUNT.toString() })
        assertTrue(presentation.technicalItems.any { it.first == "Versão" && it.second == MODEL_INPUT_VERSION })
        assertTrue(presentation.technicalItems.any { it.first == "Classes" && it.second == MODEL_CLASS_COUNT.toString() })
        assertTrue(presentation.technicalItems.any { it.first == "Métricas internas" && it.second.contains("64") })
        assertTrue(presentation.technicalItems.any { it.first == "Tipo de interpretação" })
        assertTrue(presentation.technicalItems.any { it.first == "Inferência (ms)" })
        assertTrue(presentation.technicalItems.any { it.first == "Itens classificados" })
        assertTrue(presentation.technicalItems.any { it.first == "OTHER por valor" })
        assertTrue(presentation.technicalItems.none { it.first == "Aviso técnico" })
        assertTrue(presentation.technicalItems.any { it.first.startsWith("Feature ") })
    }

    @Test
    fun toHomeAnalysisPresentation_showsAlcoholicBeverageProfileWithAlcoholSpecificNarrative() {
        val receipts = listOf(
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "CERVEJA", price = 18.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "SALGADINHO", price = 9.0, category = ProductCategory.INDUSTRIALIZED)
                )
            ),
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "VINHO", price = 35.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "AMENDOIM", price = 7.0, category = ProductCategory.INDUSTRIALIZED)
                )
            )
        )

        val metrics = CalculateConsumptionMetricsUseCase(
            semanticTagger = KeywordProductSemanticTagger()
        )(receipts)
        val modelInput = BuildConsumptionModelInputUseCase()(metrics)
        val behaviorResult = ConsumptionBehaviorResult(
            mainProfile = ConsumptionBehaviorProfile.ALCOHOLIC_BEVERAGE_RECURRENT,
            confidence = 0.81,
            profileScores = mapOf(
                ConsumptionBehaviorProfile.ALCOHOLIC_BEVERAGE_RECURRENT to 0.81,
                ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT to 0.12,
                ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED to 0.07
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

        assertEquals("Recorrência de bebidas alcoólicas", presentation.profileTitle)
        assertTrue(presentation.profileDescription.contains("Bebidas alcoólicas"))
        assertTrue(presentation.consumptionReading.contains("bebidas alcoólicas"))
        assertTrue(presentation.primarySignals.any { it.contains("Bebidas alcoólicas presentes") })
        assertTrue(presentation.primarySignals.any { it.contains("Bebidas alcoólicas + snacks") })
    }

    @Test
    fun toHomeAnalysisPresentation_showsNonAlcoholicBeverageProfileWithNonAlcoholicSpecificNarrative() {
        val receipts = listOf(
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "REFRIGERANTE", price = 8.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "BISCOITO", price = 5.0, category = ProductCategory.INDUSTRIALIZED)
                )
            ),
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "RED BULL", price = 12.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "BATATA FRITA", price = 6.0, category = ProductCategory.INDUSTRIALIZED)
                )
            )
        )

        val metrics = CalculateConsumptionMetricsUseCase(
            semanticTagger = KeywordProductSemanticTagger()
        )(receipts)
        val modelInput = BuildConsumptionModelInputUseCase()(metrics)
        val behaviorResult = ConsumptionBehaviorResult(
            mainProfile = ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT,
            confidence = 0.75,
            profileScores = mapOf(
                ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT to 0.75,
                ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED to 0.18,
                ConsumptionBehaviorProfile.ALCOHOLIC_BEVERAGE_RECURRENT to 0.07
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

        assertEquals("Recorrência de bebidas não alcoólicas", presentation.profileTitle)
        assertTrue(presentation.profileDescription.lowercase().contains("bebidas não alcoólicas"))
        assertTrue(presentation.consumptionReading.lowercase().contains("bebidas"))
        assertFalse(presentation.consumptionReading.lowercase().contains("álcool"))
        assertTrue(presentation.primarySignals.any { it.lowercase().contains("bebidas") })
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
        val metrics = CalculateConsumptionMetricsUseCase(
            semanticTagger = KeywordProductSemanticTagger()
        )(receipts)
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

        val metrics = CalculateConsumptionMetricsUseCase(
            semanticTagger = KeywordProductSemanticTagger()
        )(receipts)
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

