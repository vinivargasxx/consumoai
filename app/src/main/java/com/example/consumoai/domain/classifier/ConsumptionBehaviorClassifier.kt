package com.example.consumoai.domain.classifier

import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ConsumptionModelInput

interface ConsumptionBehaviorClassifier {
	suspend fun classify(input: ConsumptionModelInput): ConsumptionBehaviorResult
}

