package com.example.consumoai.domain.model

enum class FallbackReason {
    MODEL_LOAD_ERROR,
    INVALID_INPUT,
    BACKEND_REJECTED_INPUT,
    INFERENCE_ERROR,
    EMPTY_FEATURES
}

