package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.classifier.ConsumptionBehaviorClassifier
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ConsumptionModelInput

/**
 * Encapsula a classificação de perfil usando o backend treinado,
 * com fallback local apenas em falhas técnicas.
 */
class ClassifyConsumptionProfileUseCase(
    private val consumptionBehaviorClassifier: ConsumptionBehaviorClassifier
) {

    suspend operator fun invoke(modelInput: ConsumptionModelInput): ConsumptionBehaviorResult {
        return consumptionBehaviorClassifier.classify(modelInput)
    }
}

