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
import com.example.consumoai.domain.model.ProfileInterpretationType
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

        assertEquals("Leitura híbrida: Rotina de consumo com bebidas e conveniência", presentation.profileTitle)
        assertEquals("Padrão de consumo consistente", presentation.confidenceLabel)
        assertEquals(ProfileInterpretationType.HYBRID_PROFILE, presentation.interpretationType)
        assertEquals("Consumo diversificado e equilibrado", presentation.secondaryProfileTitle)
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

        assertEquals("Padrão social de bebidas", presentation.profileTitle)
        assertTrue(presentation.profileDescription.contains("snacks"))
        assertTrue(presentation.consumptionReading.contains("Bebidas alcoólicas aparecem"))
        assertTrue(presentation.primarySignals.any { it.contains("Presente em") })
        assertTrue(presentation.primarySignals.any { it.contains("Representa") })
        assertTrue(presentation.primarySignals.any { it.contains("Álcool + snacks") })
        assertTrue(presentation.explanationSignals.any { it.contains("Bebidas alcoólicas aparecem") })
        assertTrue(presentation.technicalExplanation.any { it.contains("non_alcoholic_beverage_frequency") })
        assertTrue(presentation.technicalExplanation.any { it.contains("essential_score") })
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

        assertEquals("Rotina de consumo com bebidas e conveniência", presentation.profileTitle)
        assertTrue(presentation.profileDescription.lowercase().contains("bebidas não alcoólicas"))
        assertTrue(presentation.consumptionReading.lowercase().contains("bebidas"))
        assertTrue(presentation.consumptionReading.contains("Financeiramente"))
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

    @Test
    fun toHomeAnalysisPresentation_marksLowConfidenceWhenConfidenceIsBelowThreshold() {
        val receipts = listOf(
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "AGUA", price = 4.0, category = ProductCategory.BEVERAGES)
                )
            )
        )
        val metrics = CalculateConsumptionMetricsUseCase(
            semanticTagger = KeywordProductSemanticTagger()
        )(receipts)
        val modelInput = BuildConsumptionModelInputUseCase()(metrics)
        val behaviorResult = ConsumptionBehaviorResult(
            mainProfile = ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT,
            confidence = 0.49,
            profileScores = mapOf(
                ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT to 0.49,
                ConsumptionBehaviorProfile.ALCOHOLIC_BEVERAGE_RECURRENT to 0.30
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

        assertEquals(ProfileInterpretationType.LOW_CONFIDENCE_PROFILE, presentation.interpretationType)
    }

    @Test
    fun toHomeAnalysisPresentation_hybridNarrativeMentionsSecondaryProfile() {
        val receipts = listOf(
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "CERVEJA", price = 15.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "AMENDOIM", price = 9.0, category = ProductCategory.INDUSTRIALIZED),
                    ProductItem(name = "REFRIGERANTE", price = 10.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "ARROZ", price = 12.0, category = ProductCategory.BASIC_FOOD)
                )
            )
        )
        val metrics = CalculateConsumptionMetricsUseCase(
            semanticTagger = KeywordProductSemanticTagger()
        )(receipts)
        val modelInput = BuildConsumptionModelInputUseCase()(metrics)
        val behaviorResult = ConsumptionBehaviorResult(
            mainProfile = ConsumptionBehaviorProfile.ALCOHOLIC_BEVERAGE_RECURRENT,
            confidence = 0.649,
            profileScores = mapOf(
                ConsumptionBehaviorProfile.ALCOHOLIC_BEVERAGE_RECURRENT to 0.649,
                ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT to 0.349,
                ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED to 0.002
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

        assertEquals(ProfileInterpretationType.HYBRID_PROFILE, presentation.interpretationType)
        assertTrue(presentation.consumptionReading.contains("também há sinais de"))
        assertEquals("Rotina de consumo com bebidas e conveniência", presentation.secondaryProfileTitle)
    }

    @Test
    fun toHomeAnalysisPresentation_nonAlcoholicNarrativeShowsFrequencyValueAndDominantCategory() {
        val analysis = buildAnalysis(
            mainProfile = ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT,
            confidence = 0.79,
            profileScores = mapOf(
                ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT to 0.79,
                ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED to 0.15,
                ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED to 0.06
            )
        ) { base ->
            base.copy(
                totalValue = 1000.0,
                nonAlcoholicBeverageFrequency = 0.794,
                nonAlcoholicBeverageValuePct = 0.087,
                maxCategoryByValue = ProductCategory.BASIC_FOOD,
                valuePercentageByCategory = base.valuePercentageByCategory + mapOf(
                    ProductCategory.BASIC_FOOD to 0.359,
                    ProductCategory.BEVERAGES to 0.087
                )
            )
        }

        val presentation = analysis.toHomeAnalysisPresentation()

        assertTrue(presentation.consumptionReading.contains("Bebidas não alcoólicas aparecem"))
        assertTrue(presentation.consumptionReading.contains("79,4%"))
        assertTrue(presentation.consumptionReading.contains("8,7%"))
        assertTrue(presentation.consumptionReading.contains("R$"))
        assertTrue(presentation.consumptionReading.contains("A maior concentração de valor está em Alimentação básica"))
    }

    @Test
    fun toHomeAnalysisPresentation_alcoholicNarrativeIsNeutralAndExplainsRepetition() {
        val analysis = buildAnalysis(
            mainProfile = ConsumptionBehaviorProfile.ALCOHOLIC_BEVERAGE_RECURRENT,
            confidence = 0.81,
            profileScores = mapOf(
                ConsumptionBehaviorProfile.ALCOHOLIC_BEVERAGE_RECURRENT to 0.81,
                ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT to 0.14,
                ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED to 0.05
            )
        ) { base ->
            base.copy(
                totalValue = 1000.0,
                alcoholicBeverageFrequency = 0.412,
                alcoholicBeverageValuePct = 0.064,
                alcoholSnackCoOccurrenceFrequency = 0.324
            )
        }

        val text = analysis.toHomeAnalysisPresentation().consumptionReading
        val lower = text.lowercase()

        assertTrue(text.contains("Bebidas alcoólicas aparecem"))
        assertTrue(text.contains("O sistema interpreta esse comportamento pela repetição nas notas"))
        assertFalse(lower.contains("alcoolismo"))
        assertFalse(lower.contains("vício"))
        assertFalse(lower.contains("excesso"))
        assertFalse(lower.contains("consumo inadequado"))
    }

    @Test
    fun toHomeAnalysisPresentation_nonAlcoholicHighFrequencyLowValueMentionsFrequencyWeight() {
        val analysis = buildAnalysis(
            mainProfile = ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT,
            confidence = 0.72,
            profileScores = mapOf(
                ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT to 0.72,
                ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED to 0.20,
                ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED to 0.08
            )
        ) { base ->
            base.copy(
                totalValue = 900.0,
                nonAlcoholicBeverageFrequency = 0.80,
                nonAlcoholicBeverageValuePct = 0.05,
                maxCategoryByValue = ProductCategory.BASIC_FOOD,
                valuePercentageByCategory = base.valuePercentageByCategory + (ProductCategory.BASIC_FOOD to 0.40)
            )
        }

        val text = analysis.toHomeAnalysisPresentation().consumptionReading
        assertTrue(text.contains("mais um padrão de frequência do que o maior peso financeiro"))
    }

    @Test
    fun toHomeAnalysisPresentation_nonAlcoholicHighValueMentionsRelevantFinancialParticipation() {
        val analysis = buildAnalysis(
            mainProfile = ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT,
            confidence = 0.77,
            profileScores = mapOf(
                ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT to 0.77,
                ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED to 0.15,
                ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED to 0.08
            )
        ) { base ->
            base.copy(
                totalValue = 1100.0,
                nonAlcoholicBeverageFrequency = 0.76,
                nonAlcoholicBeverageValuePct = 0.20,
                maxCategoryByValue = ProductCategory.BEVERAGES,
                valuePercentageByCategory = base.valuePercentageByCategory + (ProductCategory.BEVERAGES to 0.20)
            )
        }

        val text = analysis.toHomeAnalysisPresentation().consumptionReading
        assertTrue(text.contains("participação financeira relevante"))
    }

    private fun buildAnalysis(
        mainProfile: ConsumptionBehaviorProfile,
        confidence: Double,
        profileScores: Map<ConsumptionBehaviorProfile, Double>,
        metricsTransform: (com.example.consumoai.domain.model.ConsumptionMetrics) -> com.example.consumoai.domain.model.ConsumptionMetrics = { it }
    ): StoredConsumptionAnalysis {
        val receipts = listOf(
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "REFRIGERANTE", price = 8.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "ARROZ", price = 20.0, category = ProductCategory.BASIC_FOOD),
                    ProductItem(name = "SABAO", price = 12.0, category = ProductCategory.CLEANING)
                )
            )
        )

        val metrics = metricsTransform(
            CalculateConsumptionMetricsUseCase(semanticTagger = KeywordProductSemanticTagger())(receipts)
        )
        val modelInput = BuildConsumptionModelInputUseCase()(metrics)
        val behaviorResult = ConsumptionBehaviorResult(
            mainProfile = mainProfile,
            confidence = confidence,
            profileScores = profileScores,
            source = BehaviorClassificationSource.TRAINED_MODEL
        )
        val behaviorAnalysis = DefaultConsumptionInsightsEngine().generate(metrics, behaviorResult)

        return StoredConsumptionAnalysis(
            receipts = receipts,
            metrics = metrics,
            modelInput = modelInput,
            behaviorResult = behaviorResult,
            behaviorAnalysis = behaviorAnalysis
        )
    }
}

