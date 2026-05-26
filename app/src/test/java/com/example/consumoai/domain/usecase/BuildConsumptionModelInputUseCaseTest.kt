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

        // Validar presença das 15 features oficiais calibradas (ordem exata)
        val expectedFeatures = listOf(
            "classified_items_percentage",
            "category_concentration_index",
            "essential_routine_score",
            "household_routine_score",
            "produce_frequency",
            "basic_produce_cooccurrence_frequency",
            "alcoholic_beverage_frequency",
            "essential_score",
            "category_dominance_gap",
            "non_alcoholic_beverage_frequency",
            "category_stability_score",
            "other_value_pct",
            "hygiene_cleaning_cooccurrence_frequency",
            "soft_drink_frequency",
            "ticket_variation_coefficient"
        )
        assertEquals(expectedFeatures, result.features.keys.toList())
        expectedFeatures.forEach { feature ->
            assertTrue("Feature ausente: $feature", result.features.containsKey(feature))
        }

        // Validar que features removidas NÃO estão nas 15 oficiais
        assertFalse(result.features.containsKey("alcohol_snack_cooccurrence_frequency"))
        assertFalse(result.features.containsKey("non_alcoholic_beverage_snack_cooccurrence_frequency"))
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


