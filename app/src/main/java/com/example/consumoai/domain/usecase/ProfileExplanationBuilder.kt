package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.ConsumptionMetrics
import com.example.consumoai.domain.model.ConsumptionProfileSummary
import java.util.Locale

class ProfileExplanationBuilder {

    fun build(summary: ConsumptionProfileSummary, metrics: ConsumptionMetrics): String {
        val reasons = mutableListOf<String>()

        if (metrics.beveragesToTotalRatio >= 0.25 || metrics.receiptsWithBeveragesPercentage >= 0.70) {
            reasons += "alta frequência de bebidas (${metrics.receiptsWithBeveragesPercentage.toPercentText()})"
        }
        if (metrics.essentialScore >= 0.60) {
            reasons += "presença consistente de itens essenciais (${metrics.essentialScore.toPercentText()})"
        }
        if (metrics.diversityScore >= 0.65) {
            reasons += "alta diversidade de categorias (${metrics.diversityScore.toPercentText()})"
        }
        if (metrics.categoryConcentrationIndex >= 0.60) {
            reasons += "forte concentração em poucas categorias (${metrics.categoryConcentrationIndex.toPercentText()})"
        }
        if (metrics.produceToTotalRatio <= 0.05) {
            reasons += "baixa participação de hortifruti (${metrics.produceToTotalRatio.toPercentText()})"
        }
        if ((metrics.valuePercentageByCategory.values.sum()) == 0.0) {
            reasons += "ausência de distribuição válida entre categorias"
        }

        val explanationBody = reasons.take(3).joinToString(", ").ifBlank {
            "combinação equilibrada dos principais indicadores comportamentais"
        }

        return buildString {
            append(summary.humanReadableDescription.trim().trimEnd('.'))
            append(" Esse resultado foi identificado devido à ")
            append(explanationBody)
            append('.')
        }
    }

    private fun Double.toPercentText(): String = "${"%.0f".format(Locale.US, this * 100)}%"
}

