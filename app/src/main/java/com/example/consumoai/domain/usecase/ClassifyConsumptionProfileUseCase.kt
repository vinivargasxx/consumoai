package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.classifier.ConsumptionBehaviorClassifier
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ConsumptionModelInput

/**
 * Classification temporária até integração do modelo treinado.
 */
class ClassifyConsumptionProfileUseCase(
    private val consumptionBehaviorClassifier: ConsumptionBehaviorClassifier
) {

    suspend operator fun invoke(modelInput: ConsumptionModelInput): ConsumptionBehaviorResult {
        return consumptionBehaviorClassifier.classify(modelInput)
    }
}

