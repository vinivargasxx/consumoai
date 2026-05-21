package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.insights.ConsumptionInsightsEngine
import com.example.consumoai.domain.model.StoredConsumptionAnalysis
import com.example.consumoai.domain.repository.ReceiptRepository

class AnalyzeStoredReceiptsUseCase(
    private val receiptRepository: ReceiptRepository,
    private val calculateConsumptionMetricsUseCase: CalculateConsumptionMetricsUseCase,
    private val buildConsumptionModelInputUseCase: BuildConsumptionModelInputUseCase,
    private val classifyConsumptionProfileUseCase: ClassifyConsumptionProfileUseCase,
    private val insightsEngine: ConsumptionInsightsEngine,
    private val consumptionFeatureSanitizer: ConsumptionFeatureSanitizer,
    private val buildConsumptionProfileSummaryUseCase: BuildConsumptionProfileSummaryUseCase
) {

    private companion object {
        const val ALCOHOL_AUDIT_TAG = "ALCOHOL_AUDIT"
        const val MODEL_RESPONSE_TAG = "MODEL_RESPONSE"
    }

    suspend operator fun invoke(): StoredConsumptionAnalysis {
        val receipts = receiptRepository.getAllReceipts()
        if (receipts.isEmpty()) {
            throw IllegalStateException("Nenhuma nota armazenada para análise.")
        }
        val metrics = calculateConsumptionMetricsUseCase(receipts)
        val rawModelInput = buildConsumptionModelInputUseCase(metrics)
        val sanitizedModelInput = consumptionFeatureSanitizer(rawModelInput)
        val classifiedResult = classifyConsumptionProfileUseCase(sanitizedModelInput.input)
        val alcoholReceiptCount = (metrics.alcoholicBeverageFrequency * receipts.size)
            .toInt()
            .coerceIn(0, receipts.size)
        safeLog(
            ALCOHOL_AUDIT_TAG,
            "summary total_receipts=${receipts.size} receipts_with_alcohol=$alcoholReceiptCount alcoholic_beverage_frequency=${metrics.alcoholicBeverageFrequency} alcohol_snack_cooccurrence_frequency=${metrics.alcoholSnackCoOccurrenceFrequency} non_alcoholic_beverage_frequency=${metrics.nonAlcoholicBeverageFrequency}"
        )
        val topThreeScores = classifiedResult.profileScores.entries
            .sortedByDescending { it.value }
            .take(3)
            .joinToString(",") { "${it.key.name}=${"%.4f".format(it.value)}" }
        safeLog(MODEL_RESPONSE_TAG, "top3_profile_scores=$topThreeScores")

        val profileSummary = buildConsumptionProfileSummaryUseCase(classifiedResult)
        val behaviorResult = classifiedResult.copy(
            profileSummary = profileSummary,
            usedSanitizedInput = sanitizedModelInput.hasChanges,
            sanitizationNotes = sanitizedModelInput.notes
        )
        val behaviorAnalysis = insightsEngine.generate(metrics, behaviorResult)

        return StoredConsumptionAnalysis(
            receipts = receipts,
            metrics = metrics,
            modelInput = sanitizedModelInput.input,
            behaviorResult = behaviorResult,
            behaviorAnalysis = behaviorAnalysis
        )
    }

    private fun safeLog(tag: String, message: String) {
        runCatching { println("$tag $message") }
    }
}


