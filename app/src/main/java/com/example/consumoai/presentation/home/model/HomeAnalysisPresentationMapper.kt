package com.example.consumoai.presentation.home.model

import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionInsight
import com.example.consumoai.domain.model.InsightSeverity
import com.example.consumoai.domain.model.ProfileInterpretationType
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.StoredConsumptionAnalysis
import java.util.Locale

fun StoredConsumptionAnalysis.toHomeAnalysisPresentation(): HomeAnalysisPresentation {
	val summary = behaviorResult.profileSummary
	val compositionLines = summary?.profileComposition
		?.map { composition ->
			"${composition.profile.toMainCharacteristicName()}: ${(composition.percentage / 100.0).toPercentageText()}"
		}
		?: behaviorResult.profileScores
			.entries
			.sortedByDescending { it.value }
			.take(3)
			.map { (profile, score) ->
				"${profile.toMainCharacteristicName()}: ${score.toPercentageText()}"
			}

	val observationLines = behaviorAnalysis
		?.insights
		.orEmpty()
		.sortedWith(compareByDescending<ConsumptionInsight> { it.severity.toOrder() }.thenBy { it.title })
		.take(5)
		.map { it.description.trim().trimEnd('.') + "." }

	val characteristics = buildList {
		summary?.humanReadableDescription?.let { add(it) }
		profileExplanation?.let { add(it) }
		addAll(compositionLines)
		if (observationLines.isNotEmpty()) {
			add("Observações:")
			addAll(observationLines)
		}
	}

	val technical = mutableListOf<Pair<String, String>>()
	technical += "Versão do input" to modelInput.version
	technical += "Quantidade de features" to modelInput.features.size.toString()
	technical += "Fonte da classificação" to behaviorResult.source.toTechnicalLabel()
	technical += "Tipo de interpretação" to (summary?.interpretationType?.name ?: ProfileInterpretationType.PURE_PROFILE.name)
	technical += "Resumo técnico" to "input=${modelInput.version}, features=${modelInput.features.size}, notas=${metrics.totalReceipts}, itens=${metrics.totalItems}"
	technical += "Itens classificados" to metrics.classifiedItemsPercentage.toPercentageText()
	technical += "OTHER por valor" to metrics.otherPercentageByValue.toPercentageText()
	technical += "OTHER por quantidade" to metrics.otherPercentageByItems.toPercentageText()
	technical += "Inferência (ms)" to behaviorResult.inferenceDurationMs.toString()
	technical += "Input sanitizado" to if (behaviorResult.usedSanitizedInput) "Sim (${behaviorResult.sanitizationNotes.size} ajustes)" else "Não"
	behaviorResult.fallbackReason?.let { technical += "Motivo do fallback" to it.name }
	if (metrics.classifiedItemsPercentage < 0.70) {
		technical += "Aviso técnico" to "Há muitos itens não classificados. Isso pode reduzir a confiabilidade da análise."
	}

	modelInput.features.toSortedMap().forEach { (name, value) ->
		technical += "Feature $name" to value.toNumberText()
	}

	behaviorResult.profileScores
		.toList()
		.sortedByDescending { (_, score) -> score }
		.forEach { (profile, score) ->
			technical += "Probabilidade ${profile.toDisplayName()}" to score.toPercentageText()
		}

	metrics.categoryMetrics
		.toList()
		.sortedBy { (category, _) -> category.name }
		.forEach { (category, categoryMetrics) ->
			val details = "valor=${categoryMetrics.totalValue.toCurrencyText()}, itens=${categoryMetrics.totalItems}, frequência=${categoryMetrics.frequency.toPercentageText()}"
			technical += "Métrica ${category.toDisplayName()}" to details
		}

	return HomeAnalysisPresentation(
		profileTitle = summary.toPresentationTitle(behaviorResult.mainProfile),
		profileDescription = profileExplanation ?: summary?.humanReadableDescription ?: behaviorResult.mainProfile.toDescription(),
		confidenceLabel = behaviorResult.confidence.toConfidenceLabel(),
		sourceLabel = behaviorResult.source.toDisplayLabel(),
		sourceWarning = behaviorResult.source.toWarningMessage(behaviorResult.fallbackReason?.name),
		mainCharacteristics = characteristics,
		consumptionSummaryItems = listOf(
			"Total gasto" to metrics.totalValue.toCurrencyText(),
			"Ticket médio" to metrics.averageTicket.toCurrencyText(),
			"Itens analisados" to metrics.totalItems.toString(),
			"Itens classificados" to metrics.classifiedItemsPercentage.toPercentageText(),
			"Percentual essencial" to metrics.essentialCategoriesPercentage.toPercentageText(),
			"Percentual não essencial" to metrics.nonEssentialCategoriesPercentage.toPercentageText(),
			"Diversidade" to metrics.categoryDiversityIndex.toPercentageText(),
			"Categoria dominante por valor" to metrics.maxCategoryByValue.toDisplayName()
		),
		technicalItems = technical
	)
}

private fun com.example.consumoai.domain.model.ConsumptionProfileSummary?.toPresentationTitle(
	defaultProfile: ConsumptionBehaviorProfile
): String {
	val summary = this ?: return defaultProfile.toDisplayName()
	return when (summary.interpretationType) {
		ProfileInterpretationType.PURE_PROFILE -> summary.primaryProfile.toDisplayName()
		ProfileInterpretationType.HYBRID_PROFILE -> "Perfil híbrido: ${summary.primaryProfile.toDisplayName()}"
		ProfileInterpretationType.LOW_CONFIDENCE_PROFILE -> "Baixa confiança: ${summary.primaryProfile.toDisplayName()}"
	}
}

fun ConsumptionBehaviorProfile.toDisplayName(): String {
	return when (this) {
		ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED -> "Orientado à conveniência"
		ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED -> "Focado no essencial"
		ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED -> "Diversificado e equilibrado"
		ConsumptionBehaviorProfile.BEVERAGE_RECURRENT -> "Recorrente em bebidas"
		ConsumptionBehaviorProfile.LOW_FRESH_FOOD -> "Baixa presença de hortifruti"
		ConsumptionBehaviorProfile.HOUSEHOLD_MAINTENANCE -> "Foco em manutenção doméstica"
		ConsumptionBehaviorProfile.HIGHLY_CONCENTRATED -> "Consumo concentrado"
		ConsumptionBehaviorProfile.IMPULSIVE_CONSUMPTION -> "Consumo impulsivo"
		ConsumptionBehaviorProfile.UNDEFINED -> "Indefinido"
	}
}

fun ConsumptionBehaviorProfile.toDescription(): String {
	return when (this) {
		ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED -> "Maior presença de produtos industrializados e compras voltadas à praticidade."
		ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED -> "Predominância de itens essenciais e alimentação básica nas compras analisadas."
		ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED -> "Distribuição relativamente equilibrada entre diferentes categorias de consumo."
		ConsumptionBehaviorProfile.BEVERAGE_RECURRENT -> "Bebidas aparecem com recorrência relevante nas notas analisadas."
		ConsumptionBehaviorProfile.LOW_FRESH_FOOD -> "Baixa participação de hortifruti e alimentos frescos no consumo analisado."
		ConsumptionBehaviorProfile.HOUSEHOLD_MAINTENANCE -> "Maior presença de produtos de higiene e limpeza doméstica."
		ConsumptionBehaviorProfile.HIGHLY_CONCENTRATED -> "Grande parte do consumo está concentrada em poucas categorias."
		ConsumptionBehaviorProfile.IMPULSIVE_CONSUMPTION -> "Maior presença de categorias não essenciais e compras de conveniência."
		ConsumptionBehaviorProfile.UNDEFINED -> "Não foi possível identificar um padrão confiável com os dados atuais."
	}
}

private fun ConsumptionBehaviorProfile.toMainCharacteristicName(): String {
	return when (this) {
		ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED -> "Consumo diversificado"
		else -> toDisplayName()
	}
}

private fun BehaviorClassificationSource.toDisplayLabel(): String {
	return when (this) {
		BehaviorClassificationSource.TRAINED_MODEL -> "Modelo treinado"
		BehaviorClassificationSource.RULE_BASED_FALLBACK -> "Fallback local"
	}
}

private fun BehaviorClassificationSource.toTechnicalLabel(): String {
	return when (this) {
		BehaviorClassificationSource.TRAINED_MODEL -> "TRAINED_MODEL"
		BehaviorClassificationSource.RULE_BASED_FALLBACK -> "RULE_BASED_FALLBACK"
	}
}

private fun BehaviorClassificationSource.toWarningMessage(fallbackReason: String?): String? {
	return when (this) {
		BehaviorClassificationSource.TRAINED_MODEL -> null
		BehaviorClassificationSource.RULE_BASED_FALLBACK -> buildString {
			append("Backend indisponível. Resultado gerado por fallback local")
			fallbackReason?.let { append(" ($it)") }
			append('.')
		}
	}
}

private fun Double.toConfidenceLabel(): String {
	return when {
		this >= 0.85 -> "Padrão de consumo muito consistente"
		this >= 0.70 -> "Padrão de consumo consistente"
		this >= 0.50 -> "Padrão de consumo parcialmente consistente"
		else -> "Padrão de consumo variado"
	}
}

private fun InsightSeverity.toOrder(): Int {
	return when (this) {
		InsightSeverity.HIGH -> 3
		InsightSeverity.MEDIUM -> 2
		InsightSeverity.LOW -> 1
	}
}

private fun ProductCategory?.toDisplayName(): String {
	return when (this) {
		ProductCategory.BASIC_FOOD -> "Alimentação básica"
		ProductCategory.INDUSTRIALIZED -> "Industrializados"
		ProductCategory.BEVERAGES -> "Bebidas"
		ProductCategory.HYGIENE -> "Higiene"
		ProductCategory.CLEANING -> "Limpeza"
		ProductCategory.PRODUCE -> "Hortifruti"
		ProductCategory.OTHER -> "Outros"
		null -> "Indefinido"
	}
}

private fun Double.toCurrencyText(): String = "R$ ${"%.2f".format(Locale.US, this).replace('.', ',')}"

private fun Double.toPercentageText(): String = "${"%.1f".format(Locale.US, this * 100)}%"

private fun Double.toNumberText(): String = "%.4f".format(Locale.US, this)

