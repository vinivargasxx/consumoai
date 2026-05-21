package com.example.consumoai.domain.insights

import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource
import com.example.consumoai.data.classifier.KeywordProductSemanticTagger
import com.example.consumoai.domain.usecase.CalculateConsumptionMetricsUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultConsumptionInsightsEngineTest {

    @Test
    fun generate_ordersInsightsBySeverityAndProducesNeutralUtf8Text() {
        val receipts = listOf(
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "REFRIGERANTE", price = 10.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "BISCOITO", price = 9.0, category = ProductCategory.INDUSTRIALIZED)
                )
            ),
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "SUCO", price = 8.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "SALGADINHO", price = 7.0, category = ProductCategory.INDUSTRIALIZED)
                )
            ),
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "ENERGETICO", price = 12.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "BARRA", price = 6.0, category = ProductCategory.INDUSTRIALIZED)
                )
            )
        )

        val metrics = CalculateConsumptionMetricsUseCase(
            semanticTagger = KeywordProductSemanticTagger()
        )(receipts)
        val result = ConsumptionBehaviorResult(
            mainProfile = ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT,
            confidence = 0.45,
            profileScores = mapOf(
                ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT to 0.46,
                ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED to 0.29,
                ConsumptionBehaviorProfile.LOW_FRESH_FOOD to 0.13
            ),
            source = BehaviorClassificationSource.TRAINED_MODEL
        )

        val analysis = DefaultConsumptionInsightsEngine().generate(metrics, result)

        assertFalse(analysis.insights.isEmpty())
        assertEquals("Bebidas aparecem com alta recorrência", analysis.insights.first().title)
        assertTrue(analysis.summary.contains("padrão"))

        val allText = buildString {
            analysis.insights.forEach {
                append(it.title)
                append(it.description)
            }
            append(analysis.summary)
        }
        assertFalse(allText.contains("Ã"))
    }

    @Test
    fun generate_addsCompositeInsightWhenTopProfilesAndMetricsSupportHybridNarrative() {
        val receipts = listOf(
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "REFRIGERANTE", price = 14.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "ARROZ", price = 12.0, category = ProductCategory.BASIC_FOOD),
                    ProductItem(name = "ALFACE", price = 6.0, category = ProductCategory.PRODUCE)
                )
            ),
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "SUCO", price = 10.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "FEIJAO", price = 11.0, category = ProductCategory.BASIC_FOOD),
                    ProductItem(name = "SABONETE", price = 8.0, category = ProductCategory.HYGIENE)
                )
            ),
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "ENERGETICO", price = 15.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "PAO", price = 9.0, category = ProductCategory.BASIC_FOOD),
                    ProductItem(name = "TOMATE", price = 7.0, category = ProductCategory.PRODUCE)
                )
            )
        )

        val metrics = CalculateConsumptionMetricsUseCase(
            semanticTagger = KeywordProductSemanticTagger()
        )(receipts)
        val result = ConsumptionBehaviorResult(
            mainProfile = ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT,
            confidence = 0.41,
            profileScores = mapOf(
                ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT to 0.41,
                ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED to 0.27,
                ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED to 0.22
            ),
            source = BehaviorClassificationSource.TRAINED_MODEL
        )

        val analysis = DefaultConsumptionInsightsEngine().generate(metrics, result)

        assertTrue(
            analysis.insights.any {
                it.title.contains("Equilíbrio entre itens essenciais e bebidas")
            }
        )
    }
}
