package com.example.consumoai.domain.insights

import com.example.consumoai.domain.model.ConsumptionBehaviorAnalysis
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ConsumptionMetrics

interface ConsumptionInsightsEngine {
    fun generate(
        metrics: ConsumptionMetrics,
        result: ConsumptionBehaviorResult
    ): ConsumptionBehaviorAnalysis
}

