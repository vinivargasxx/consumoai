package com.example.consumoai.domain.insights

import com.example.consumoai.domain.model.BehaviorCompositionItem
import com.example.consumoai.domain.model.ConsumptionBehaviorAnalysis
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ConsumptionInsight
import com.example.consumoai.domain.model.ConsumptionMetrics
import com.example.consumoai.domain.model.InsightSeverity
import com.example.consumoai.domain.model.InsightType
import com.example.consumoai.domain.model.ProductCategory

class DefaultConsumptionInsightsEngine : ConsumptionInsightsEngine {

    override fun generate(
        metrics: ConsumptionMetrics,
        result: ConsumptionBehaviorResult
    ): ConsumptionBehaviorAnalysis {
        val insights = mutableListOf<ConsumptionInsight>()

        // Generate primary insights based on metrics and model scores
        generateRecurrenceInsights(metrics, insights)
        generateDiversityInsights(metrics, insights)
        generateConcentrationInsights(metrics, insights)
        generateCategoryDominanceInsights(metrics, insights)
        generateFreshFoodInsights(metrics, insights)
        generateBalanceInsights(metrics, insights)
        generateCompositeInsights(metrics, result, insights)
        generateHybridBehaviorInsights(result, insights)
        generateConfidenceInsights(result, insights)

        val sortedInsights = insights.sortedWith(
            compareByDescending<ConsumptionInsight> { it.severity.toPriority() }
                .thenByDescending { it.relatedProfiles.size }
                .thenBy { it.title }
        )

        // Generate behavioral composition from profile scores
        val composition = generateBehavioralComposition(result)

        // Generate text summary
        val summary = generateSummary(result, metrics, composition)

        return ConsumptionBehaviorAnalysis(
            behaviorResult = result,
            insights = sortedInsights,
            summary = summary,
            behavioralComposition = composition
        )
    }

    private fun generateRecurrenceInsights(
        metrics: ConsumptionMetrics,
        insights: MutableList<ConsumptionInsight>
    ) {
        val beverageFrequency = metrics.frequencyByCategory[ProductCategory.BEVERAGES] ?: 0.0
        
        if (beverageFrequency >= 0.70) {
            insights.add(
                ConsumptionInsight(
                    title = "Bebidas aparecem com alta recorrência",
                    description = "Bebidas estiveram presentes em grande parte das compras analisadas.",
                    type = InsightType.RECURRENCE,
                    severity = InsightSeverity.HIGH,
                    relatedProfiles = listOf(ConsumptionBehaviorProfile.BEVERAGE_RECURRENT),
                    relatedFeatures = listOf("beverages_frequency")
                )
            )
        }
    }

    private fun generateDiversityInsights(
        metrics: ConsumptionMetrics,
        insights: MutableList<ConsumptionInsight>
    ) {
        if (metrics.categoryDiversityIndex < 0.40) {
            insights.add(
                ConsumptionInsight(
                    title = "Baixa diversidade de categorias",
                    description = "As compras analisadas apresentam baixa variedade entre categorias de consumo.",
                    type = InsightType.DIVERSITY,
                    severity = InsightSeverity.MEDIUM,
                    relatedFeatures = listOf("category_diversity_index")
                )
            )
        }
    }

    private fun generateConcentrationInsights(
        metrics: ConsumptionMetrics,
        insights: MutableList<ConsumptionInsight>
    ) {
        if (metrics.categoryConcentrationIndex >= 0.60) {
            insights.add(
                ConsumptionInsight(
                    title = "Consumo mais concentrado",
                    description = "Grande parte do valor consumido está concentrado em poucas categorias.",
                    type = InsightType.CONCENTRATION,
                    severity = InsightSeverity.MEDIUM,
                    relatedFeatures = listOf("category_concentration_index")
                )
            )
        }
    }

    private fun generateCategoryDominanceInsights(
        metrics: ConsumptionMetrics,
        insights: MutableList<ConsumptionInsight>
    ) {
        val industrializedPercentage = metrics.valuePercentageByCategory[ProductCategory.INDUSTRIALIZED] ?: 0.0

        if (industrializedPercentage >= 0.35) {
            insights.add(
                ConsumptionInsight(
                    title = "Alta presença de industrializados",
                    description = "Produtos industrializados representam parcela relevante das compras analisadas.",
                    type = InsightType.CATEGORY_DOMINANCE,
                    severity = InsightSeverity.MEDIUM,
                    relatedProfiles = listOf(ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED),
                    relatedFeatures = listOf("industrialized_value_pct")
                )
            )
        }
    }

    private fun generateFreshFoodInsights(
        metrics: ConsumptionMetrics,
        insights: MutableList<ConsumptionInsight>
    ) {
        val producePercentage = metrics.produceToTotalRatio

        if (producePercentage <= 0.05) {
            insights.add(
                ConsumptionInsight(
                    title = "Baixa presença de alimentos frescos",
                    description = "As compras apresentam baixa participação de hortifruti e alimentos frescos.",
                    type = InsightType.CONSUMPTION_BALANCE,
                    severity = InsightSeverity.MEDIUM,
                    relatedProfiles = listOf(ConsumptionBehaviorProfile.LOW_FRESH_FOOD),
                    relatedFeatures = listOf("produce_value_pct")
                )
            )
        }
    }

    private fun generateBalanceInsights(
        metrics: ConsumptionMetrics,
        insights: MutableList<ConsumptionInsight>
    ) {
        if (metrics.diversityScore >= 0.65 && metrics.categoryConcentrationIndex <= 0.35) {
            insights.add(
                ConsumptionInsight(
                    title = "Padrão de consumo equilibrado",
                    description = "As categorias de consumo estão relativamente distribuídas entre as compras.",
                    type = InsightType.BEHAVIORAL_PATTERN,
                    severity = InsightSeverity.LOW,
                    relatedProfiles = listOf(ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED),
                    relatedFeatures = listOf("diversity_score", "category_concentration_index")
                )
            )
        }
    }

    private fun generateCompositeInsights(
        metrics: ConsumptionMetrics,
        result: ConsumptionBehaviorResult,
        insights: MutableList<ConsumptionInsight>
    ) {
        val topProfiles = result.profileScores
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }

        val beveragesStrong = metrics.beveragesToTotalRatio > 0.25
        val diversityStrong = metrics.diversityScore > 0.45
        val essentialsStrong = metrics.essentialScore > 0.50
        val householdStrong = (metrics.valuePercentageByCategory[ProductCategory.HYGIENE] ?: 0.0) +
            (metrics.valuePercentageByCategory[ProductCategory.CLEANING] ?: 0.0) > 0.22

        if (
            beveragesStrong &&
            diversityStrong &&
            essentialsStrong &&
            topProfiles.contains(ConsumptionBehaviorProfile.BEVERAGE_RECURRENT) &&
            topProfiles.contains(ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED)
        ) {
            insights.add(
                ConsumptionInsight(
                    title = "Equilíbrio entre itens essenciais e bebidas",
                    description = "Seu consumo demonstra equilíbrio entre itens essenciais e bebidas recorrentes.",
                    type = InsightType.BEHAVIORAL_PATTERN,
                    severity = InsightSeverity.MEDIUM,
                    relatedProfiles = listOf(
                        ConsumptionBehaviorProfile.BEVERAGE_RECURRENT,
                        ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED,
                        ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED
                    ),
                    relatedFeatures = listOf("beverages_value_pct", "diversity_score", "essential_score")
                )
            )
        }

        if (
            householdStrong &&
            essentialsStrong &&
            topProfiles.contains(ConsumptionBehaviorProfile.HOUSEHOLD_MAINTENANCE)
        ) {
            insights.add(
                ConsumptionInsight(
                    title = "Rotina doméstica bem marcada",
                    description = "As compras combinam manutenção doméstica com presença consistente de itens essenciais.",
                    type = InsightType.BEHAVIORAL_PATTERN,
                    severity = InsightSeverity.LOW,
                    relatedProfiles = listOf(
                        ConsumptionBehaviorProfile.HOUSEHOLD_MAINTENANCE,
                        ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED
                    ),
                    relatedFeatures = listOf("hygiene_value_pct", "cleaning_value_pct", "essential_score")
                )
            )
        }
    }

    private fun generateHybridBehaviorInsights(
        result: ConsumptionBehaviorResult,
        insights: MutableList<ConsumptionInsight>
    ) {
        // Find second highest probability
        val sortedScores = result.profileScores.values.sortedDescending()
        if (sortedScores.size >= 2 && sortedScores[1] >= 0.25) {
            insights.add(
                ConsumptionInsight(
                    title = "Comportamento híbrido identificado",
                    description = "O consumo apresenta sinais relevantes de múltiplos perfis comportamentais.",
                    type = InsightType.MODEL_INTERPRETATION,
                    severity = InsightSeverity.LOW,
                    relatedFeatures = listOf("second_highest_probability")
                )
            )
        }
    }

    private fun generateConfidenceInsights(
        result: ConsumptionBehaviorResult,
        insights: MutableList<ConsumptionInsight>
    ) {
        if (result.confidence < 0.50) {
            insights.add(
                ConsumptionInsight(
                    title = "Baixa confiança da classificação",
                    description = "O modelo identificou sinais comportamentais menos consistentes nas compras analisadas.",
                    type = InsightType.MODEL_INTERPRETATION,
                    severity = InsightSeverity.MEDIUM,
                    relatedFeatures = listOf("confidence")
                )
            )
        }
    }

    private fun generateBehavioralComposition(
        result: ConsumptionBehaviorResult
    ): List<BehaviorCompositionItem> {
        return result.profileScores
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { (profile, score) ->
                BehaviorCompositionItem(
                    profile = profile,
                    percentage = (score * 100).coerceIn(0.0, 100.0)
                )
            }
    }

    private fun generateSummary(
        result: ConsumptionBehaviorResult,
        metrics: ConsumptionMetrics,
        composition: List<BehaviorCompositionItem>
    ): String {
        return buildString {
            append("As compras analisadas apresentam predominância de consumo com perfil ")
            append(getProfileDescription(result.mainProfile))
            append(".")

            if (composition.size >= 2) {
                append(" O segundo padrão identificado é ")
                append(getProfileDescription(composition[1].profile))
                append(".")
            }

            if (metrics.categoryConcentrationIndex >= 0.60) {
                append(" O padrão geral mostra concentração de gasto em poucas categorias.")
            } else if (metrics.diversityScore >= 0.65) {
                append(" A distribuição entre categorias é relativamente equilibrada.")
            }

            if (metrics.produceToTotalRatio <= 0.05) {
                append(" Destaca-se a baixa presença de alimentos frescos.")
            }
        }
    }

    private fun getProfileDescription(profile: ConsumptionBehaviorProfile): String {
        return when (profile) {

            ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED -> "orientado ao consumo de conveniência"
            ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED -> "focado em itens essenciais"
            ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED -> "diversificado e equilibrado"
            ConsumptionBehaviorProfile.BEVERAGE_RECURRENT -> "recorrente em bebidas"
            ConsumptionBehaviorProfile.LOW_FRESH_FOOD -> "com baixa presença de alimentos frescos"
            ConsumptionBehaviorProfile.HOUSEHOLD_MAINTENANCE -> "focado em higiene e limpeza"
            ConsumptionBehaviorProfile.HIGHLY_CONCENTRATED -> "muito concentrado em categoria"
            ConsumptionBehaviorProfile.IMPULSIVE_CONSUMPTION -> "com características impulsivas"
            ConsumptionBehaviorProfile.UNDEFINED -> "não claramente definido"
        }
    }

    private fun InsightSeverity.toPriority(): Int {
        return when (this) {
            InsightSeverity.HIGH -> 3
            InsightSeverity.MEDIUM -> 2
            InsightSeverity.LOW -> 1
        }
    }
}


