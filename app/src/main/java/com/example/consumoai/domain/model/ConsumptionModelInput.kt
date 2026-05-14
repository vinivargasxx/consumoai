package com.example.consumoai.domain.model

const val MODEL_INPUT_VERSION = "v1"

/**
 * Official feature payload consumed by the behavior classifier and, in the future,
 * by the trained model integrated into the app.
 */
data class ConsumptionModelInput(
    val version: String = MODEL_INPUT_VERSION,
    val features: Map<String, Double>
)

