package com.example.consumoai.data.mapper

import com.example.consumoai.domain.model.ConsumptionModelInput
import com.example.consumoai.domain.model.MODEL_FEATURE_COUNT
import com.example.consumoai.domain.model.MODEL_FINAL_FEATURES
import com.example.consumoai.domain.model.MODEL_INPUT_VERSION

const val MODEL_INPUT_CSV_EXPORT_FILE_NAME = "consumoai_model_input_final.csv"

fun ConsumptionModelInput.toModelInputCsvHeader(): String {
    return (listOf("version", "feature_count") + MODEL_FINAL_FEATURES).joinToString(",")
}

fun ConsumptionModelInput.toModelInputCsvRow(): String {
    val orderedValues = MODEL_FINAL_FEATURES.map { feature ->
        (features[feature] ?: 0.0).toString()
    }

    return buildList {
        add(version.ifBlank { MODEL_INPUT_VERSION })
        add(MODEL_FEATURE_COUNT.toString())
        addAll(orderedValues)
    }.joinToString(",")
}

fun ConsumptionModelInput.toModelInputCsv(): String {
    return listOf(
        toModelInputCsvHeader(),
        toModelInputCsvRow()
    ).joinToString("\n")
}

