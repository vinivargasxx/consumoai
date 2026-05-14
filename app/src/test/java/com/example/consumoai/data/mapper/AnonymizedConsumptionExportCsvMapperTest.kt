package com.example.consumoai.data.mapper

import com.example.consumoai.domain.model.AnonymizedConsumptionExport
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnonymizedConsumptionExportCsvMapperTest {

    @Test
    fun toCsvRow_exportsOnlyStructuredAnonymousFields() {
        val export = AnonymizedConsumptionExport(
            inputVersion = "v1",
            finalProfile = "BEVERAGE_RECURRENT",
            confidence = 0.46,
            source = "TRAINED_MODEL",
            fallbackReason = null,
            featureSnapshot = mapOf("total_receipts" to 4.0, "beverages_value_pct" to 0.33),
            profileScores = mapOf("BEVERAGE_RECURRENT" to 0.46),
            aggregatedMetrics = mapOf("total_value" to 120.0)
        )

        val header = export.toCsvHeader()
        val row = export.toCsvRow()

        assertTrue(header.contains("input_version"))
        assertTrue(row.contains("BEVERAGE_RECURRENT"))
        assertFalse(row.contains("CPF"))
        assertFalse(row.contains("CNPJ"))
    }
}

