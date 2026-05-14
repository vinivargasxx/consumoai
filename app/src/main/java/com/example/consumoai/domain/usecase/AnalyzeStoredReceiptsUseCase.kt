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
    private val buildConsumptionProfileSummaryUseCase: BuildConsumptionProfileSummaryUseCase,
    private val profileExplanationBuilder: ProfileExplanationBuilder,
    private val buildAnonymizedConsumptionExportUseCase: BuildAnonymizedConsumptionExportUseCase
) {

    suspend operator fun invoke(): StoredConsumptionAnalysis {
        val receipts = receiptRepository.getAllReceipts()
        if (receipts.isEmpty()) {
            throw IllegalStateException("Nenhuma nota armazenada para análise.")
        }
        val metrics = calculateConsumptionMetricsUseCase(receipts)
        val rawModelInput = buildConsumptionModelInputUseCase(metrics)
        val sanitizedModelInput = consumptionFeatureSanitizer(rawModelInput)
        val classifiedResult = classifyConsumptionProfileUseCase(sanitizedModelInput.input)
        val profileSummary = buildConsumptionProfileSummaryUseCase(classifiedResult)
        val behaviorResult = classifiedResult.copy(
            profileSummary = profileSummary,
            usedSanitizedInput = sanitizedModelInput.hasChanges,
            sanitizationNotes = sanitizedModelInput.notes
        )
        val behaviorAnalysis = insightsEngine.generate(metrics, behaviorResult)
        val profileExplanation = profileExplanationBuilder.build(profileSummary, metrics)
        val anonymizedExport = buildAnonymizedConsumptionExportUseCase(
            metrics = metrics,
            modelInput = sanitizedModelInput.input,
            result = behaviorResult
        )

        return StoredConsumptionAnalysis(
            receipts = receipts,
            metrics = metrics,
            modelInput = sanitizedModelInput.input,
            behaviorResult = behaviorResult,
            behaviorAnalysis = behaviorAnalysis,
            profileExplanation = profileExplanation,
            anonymizedExport = anonymizedExport
        )
    }
}

