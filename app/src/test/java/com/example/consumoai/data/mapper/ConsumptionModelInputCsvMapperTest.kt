package com.example.consumoai.data.mapper

import com.example.consumoai.domain.model.ConsumptionModelInput
import com.example.consumoai.domain.model.MODEL_FEATURE_COUNT
import com.example.consumoai.domain.model.MODEL_FINAL_FEATURES
import com.example.consumoai.domain.model.MODEL_INPUT_VERSION
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConsumptionModelInputCsvMapperTest {

    @Test
    fun toModelInputCsv_exportsV3AlcoholTop15InOfficialOrder() {
        val features = MODEL_FINAL_FEATURES
            .mapIndexed { index, feature -> feature to (index + 1).toDouble() }
            .toMap()

        val input = ConsumptionModelInput(
            version = MODEL_INPUT_VERSION,
            features = features
        )

        val header = input.toModelInputCsvHeader().split(',')
        val row = input.toModelInputCsvRow().split(',')
        val csv = input.toModelInputCsv()

        assertEquals("consumoai_model_input_final.csv", MODEL_INPUT_CSV_EXPORT_FILE_NAME)
        assertEquals(2 + MODEL_FEATURE_COUNT, header.size)
        assertEquals("version", header.first())
        assertEquals("feature_count", header[1])
        assertEquals(MODEL_FINAL_FEATURES, header.drop(2))

        assertEquals(MODEL_INPUT_VERSION, row[0])
        assertEquals(MODEL_FEATURE_COUNT.toString(), row[1])
        assertEquals("1.0", row[2])
        assertEquals("15.0", row.last())
        assertTrue(csv.startsWith("version,feature_count,"))
        assertTrue(csv.contains("\n"))
    }
}

