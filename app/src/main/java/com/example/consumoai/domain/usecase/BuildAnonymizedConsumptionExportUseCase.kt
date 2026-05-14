package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.AnonymizedConsumptionExport
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ConsumptionMetrics
import com.example.consumoai.domain.model.ConsumptionModelInput

class BuildAnonymizedConsumptionExportUseCase {

    operator fun invoke(
        metrics: ConsumptionMetrics,
        modelInput: ConsumptionModelInput,
        result: ConsumptionBehaviorResult
    ): AnonymizedConsumptionExport {
        return AnonymizedConsumptionExport(
            inputVersion = modelInput.version,
            finalProfile = result.mainProfile.name,
            confidence = result.confidence,
            source = result.source.name,
            fallbackReason = result.fallbackReason?.name,
            featureSnapshot = modelInput.features.toSortedMap(),
            profileScores = result.profileScores
                .mapKeys { (profile, _) -> profile.name }
                .toSortedMap(),
            aggregatedMetrics = linkedMapOf(
                "total_receipts" to metrics.totalReceipts.toDouble(),
                "total_items" to metrics.totalItems.toDouble(),
                "total_value" to metrics.totalValue,
                "average_ticket" to metrics.averageTicket,
                "classified_items_percentage" to metrics.classifiedItemsPercentage,
                "other_percentage_by_value" to metrics.otherPercentageByValue,
                "other_percentage_by_items" to metrics.otherPercentageByItems,
                "essential_score" to metrics.essentialScore,
                "diversity_score" to metrics.diversityScore,
                "convenience_score" to metrics.convenienceScore
            )
        )
    }
}

