package com.example.consumoai.data.classifier
import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionModelInput
import com.example.consumoai.domain.model.FallbackReason
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
class RemoteConsumptionBehaviorClassifierTest {
    @Test
    fun classify_returnsRemotePredictionWhenApiSucceeds() = runBlocking {
        val classifier = RemoteConsumptionBehaviorClassifier(
            api = object : ConsumptionModelApi {
                override suspend fun predict(request: ModelPredictionRequestDto): ModelPredictionResponseDto {
                    assertEquals("v1", request.version)
                    assertEquals(27, request.features.size)
                    return ModelPredictionResponseDto(
                        main_profile = "BEVERAGE_RECURRENT",
                        confidence = 0.465,
                        profile_scores = mapOf(
                            "BEVERAGE_RECURRENT" to 0.465,
                            "DIVERSIFIED_BALANCED" to 0.295,
                            "LOW_FRESH_FOOD" to 0.13
                        )
                    )
                }
            },
            fallbackClassifier = RuleBasedConsumptionBehaviorClassifier()
        )
        val result = classifier.classify(input())
        assertEquals(BehaviorClassificationSource.TRAINED_MODEL, result.source)
        assertEquals(ConsumptionBehaviorProfile.BEVERAGE_RECURRENT, result.mainProfile)
        assertEquals(0.465, result.confidence, 0.0001)
        assertEquals(0.295, result.profileScores[ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED] ?: -1.0, 0.0001)
        assertEquals(true, result.inferenceDurationMs >= 0L)
    }
    @Test
    fun classify_usesFallbackWhenApiFails() = runBlocking {
        val classifier = RemoteConsumptionBehaviorClassifier(
            api = object : ConsumptionModelApi {
                override suspend fun predict(request: ModelPredictionRequestDto): ModelPredictionResponseDto {
                    error("backend offline")
                }
            },
            fallbackClassifier = RuleBasedConsumptionBehaviorClassifier()
        )
        val result = classifier.classify(
            input(
                "beverages_value_pct" to 0.30,
                "beverages_frequency" to 0.75
            )
        )
        assertEquals(BehaviorClassificationSource.RULE_BASED_FALLBACK, result.source)
        assertEquals(ConsumptionBehaviorProfile.BEVERAGE_RECURRENT, result.mainProfile)
        assertEquals(1.0, result.confidence, 0.0001)
        assertEquals(FallbackReason.INFERENCE_ERROR, result.fallbackReason)
    }
    @Test
    fun classify_usesFallbackWhenInputHasNoFeatures() = runBlocking {
        val classifier = RemoteConsumptionBehaviorClassifier(
            api = object : ConsumptionModelApi {
                override suspend fun predict(request: ModelPredictionRequestDto): ModelPredictionResponseDto {
                    error("should not call API")
                }
            },
            fallbackClassifier = RuleBasedConsumptionBehaviorClassifier()
        )
        val result = classifier.classify(ConsumptionModelInput(features = emptyMap()))
        assertEquals(BehaviorClassificationSource.RULE_BASED_FALLBACK, result.source)
        assertEquals(FallbackReason.EMPTY_FEATURES, result.fallbackReason)
    }
    private fun input(vararg overrides: Pair<String, Double>): ConsumptionModelInput {
        val defaults = linkedMapOf(
            "total_receipts" to 5.0,
            "total_items" to 20.0,
            "total_value" to 200.0,
            "average_ticket" to 40.0,
            "average_items_per_receipt" to 4.0,
            "basic_food_value_pct" to 0.20,
            "industrialized_value_pct" to 0.20,
            "beverages_value_pct" to 0.10,
            "hygiene_value_pct" to 0.05,
            "cleaning_value_pct" to 0.05,
            "produce_value_pct" to 0.10,
            "other_value_pct" to 0.30,
            "basic_food_frequency" to 0.60,
            "industrialized_frequency" to 0.60,
            "beverages_frequency" to 0.30,
            "produce_frequency" to 0.30,
            "hygiene_frequency" to 0.20,
            "cleaning_frequency" to 0.20,
            "category_concentration_index" to 0.30,
            "category_dominance_gap" to 0.10,
            "category_diversity_index" to 0.60,
            "essential_categories_percentage" to 0.40,
            "non_essential_categories_percentage" to 0.60,
            "convenience_score" to 0.30,
            "essential_score" to 0.40,
            "diversity_score" to 0.50,
            "classified_items_percentage" to 0.90
        )
        overrides.forEach { (key, value) ->
            defaults[key] = value
        }
        return ConsumptionModelInput(features = defaults)
    }
}
