package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.ConsumptionModelInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConsumptionFeatureSanitizerTest {

    private val sanitizer = ConsumptionFeatureSanitizer()

    @Test
    fun invoke_clampsProbabilityLikeValuesAndInvalidNumbers() {
        val input = ConsumptionModelInput(
            features = linkedMapOf(
                "beverages_value_pct" to 1.4,
                "classified_items_percentage" to -0.3,
                "diversity_score" to Double.NaN,
                "total_value" to Double.POSITIVE_INFINITY,
                "total_items" to -5.0
            )
        )

        val result = sanitizer(input)

        assertEquals(1.0, result.input.features.getValue("beverages_value_pct"), 0.0001)
        assertEquals(0.0, result.input.features.getValue("classified_items_percentage"), 0.0001)
        assertEquals(0.0, result.input.features.getValue("diversity_score"), 0.0001)
        assertEquals(0.0, result.input.features.getValue("total_value"), 0.0001)
        assertEquals(0.0, result.input.features.getValue("total_items"), 0.0001)
        assertEquals(5, result.notes.size)
    }

    @Test
    fun invoke_keepsValidValuesUntouched() {
        val input = ConsumptionModelInput(
            features = linkedMapOf(
                "beverages_value_pct" to 0.35,
                "total_value" to 120.0,
                "total_items" to 8.0
            )
        )

        val result = sanitizer(input)

        assertEquals(0.35, result.input.features.getValue("beverages_value_pct"), 0.0001)
        assertEquals(120.0, result.input.features.getValue("total_value"), 0.0001)
        assertTrue(result.notes.isEmpty())
    }
}

