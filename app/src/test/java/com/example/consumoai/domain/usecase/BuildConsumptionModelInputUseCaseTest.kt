package com.example.consumoai.domain.usecase

import com.example.consumoai.data.classifier.KeywordProductSemanticTagger
import com.example.consumoai.domain.model.MODEL_FEATURE_COUNT
import com.example.consumoai.domain.model.MODEL_FINAL_FEATURES
import com.example.consumoai.domain.model.MODEL_INPUT_VERSION
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BuildConsumptionModelInputUseCaseTest {

    private val useCase = BuildConsumptionModelInputUseCase()

    @Test
    fun invoke_buildsFinalModelFeatureMapWithoutNaNOrInfinity() {
        val receipts = listOf(
            Receipt(
                date = LocalDate.of(2026, 5, 1),
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "COCA", price = 10.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "ARROZ", price = 8.0, category = ProductCategory.BASIC_FOOD)
                )
            ),
            Receipt(
                date = LocalDate.of(2026, 5, 10),
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "SABAO", price = 9.0, category = ProductCategory.CLEANING),
                    ProductItem(name = "MELANCIA", price = 12.0, category = ProductCategory.PRODUCE)
                )
            )
        )

        val metrics = CalculateConsumptionMetricsUseCase(
            semanticTagger = KeywordProductSemanticTagger()
        )(receipts)

        val result = useCase(metrics)

        assertEquals(MODEL_INPUT_VERSION, result.version)
        assertEquals(MODEL_FEATURE_COUNT, result.features.size)
        assertEquals(15, result.features.size)
        assertEquals(MODEL_FINAL_FEATURES, result.features.keys.toList())

        // Validar presença das 15 features oficiais atuais
        assertTrue(result.features.containsKey("non_alcoholic_beverage_frequency"))
        assertTrue(result.features.containsKey("category_concentration_index"))
        assertTrue(result.features.containsKey("classified_items_percentage"))
        assertTrue(result.features.containsKey("non_alcoholic_beverage_snack_cooccurrence_frequency"))
        assertTrue(result.features.containsKey("household_routine_score"))
        assertTrue(result.features.containsKey("alcoholic_beverage_frequency"))
        assertTrue(result.features.containsKey("produce_frequency"))
        assertTrue(result.features.containsKey("essential_routine_score"))
        assertTrue(result.features.containsKey("category_dominance_gap"))
        assertTrue(result.features.containsKey("category_stability_score"))
        assertTrue(result.features.containsKey("essential_score"))
        assertTrue(result.features.containsKey("other_value_pct"))
        assertTrue(result.features.containsKey("hygiene_cleaning_cooccurrence_frequency"))
        assertTrue(result.features.containsKey("basic_produce_cooccurrence_frequency"))
        assertTrue(result.features.containsKey("alcohol_snack_cooccurrence_frequency"))

        // Validar que features removidas NÃO estão nas 15 oficiais
        assertFalse(result.features.containsKey("soft_drink_frequency"))
        assertFalse(result.features.containsKey("soft_drink_value_pct"))
        assertFalse(result.features.containsKey("alcoholic_beverage_value_pct"))
        assertFalse(result.features.containsKey("non_alcoholic_beverage_value_pct"))
        assertFalse(result.features.containsKey("energy_drink_frequency"))
        assertFalse(result.features.containsKey("energy_drink_value_pct"))
        assertFalse(result.features.containsKey("beverages_frequency"))

        // Validar integridade
        assertFalse(result.features.values.any { it.isNaN() })
        assertFalse(result.features.values.any { it.isInfinite() })
    }
}


