package com.example.consumoai.presentation.home.model

import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.FallbackReason
import com.example.consumoai.domain.model.MODEL_BACKEND_IDENTIFIER
import com.example.consumoai.domain.model.MODEL_CLASS_COUNT
import com.example.consumoai.domain.model.MODEL_FEATURE_COUNT
import com.example.consumoai.domain.model.MODEL_INTERNAL_METRICS_COUNT
import com.example.consumoai.domain.model.MODEL_NAME
import com.example.consumoai.domain.model.ProfileInterpretationType
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.StoredConsumptionAnalysis
import java.util.Locale

fun StoredConsumptionAnalysis.toHomeAnalysisPresentation(): HomeAnalysisPresentation {
	val selectedInput = modelInput
	val metrics = this.metrics
	val summary = behaviorResult.profileSummary

	val technical = mutableListOf<Pair<String, String>>()
	technical += "Modelo" to MODEL_NAME
	technical += "Backend" to (behaviorResult.backendModelUsed ?: MODEL_BACKEND_IDENTIFIER)
	technical += "Features enviadas" to "$MODEL_FEATURE_COUNT"
	technical += "Versão" to (behaviorResult.requestedInputVersion ?: selectedInput.version)
	technical += "Classes" to MODEL_CLASS_COUNT.toString()
	technical += "Fonte da classificação" to behaviorResult.source.toTechnicalLabel()
	technical += "Confiança" to behaviorResult.confidence.toPercentageText()
	technical += "Tipo de interpretação" to (summary?.interpretationType?.name ?: ProfileInterpretationType.PURE_PROFILE.name)
	technical += "Itens classificados" to metrics.classifiedItemsPercentage.toPercentageText()
	technical += "OTHER por valor" to metrics.otherPercentageByValue.toPercentageText()
	technical += "Inferência (ms)" to behaviorResult.inferenceDurationMs.toString()
	technical += "Input sanitizado" to if (behaviorResult.usedSanitizedInput) "Sim (${behaviorResult.sanitizationNotes.size} ajustes)" else "Não"
	behaviorResult.fallbackReason?.let { technical += "Motivo do fallback" to it.name }
	technical += "Métricas internas" to "$MODEL_INTERNAL_METRICS_COUNT calculadas"
	technical += "Notas analisadas" to metrics.totalReceipts.toString()
	technical += "Itens analisados" to metrics.totalItems.toString()
	if (metrics.classifiedItemsPercentage < 0.70) {
		technical += "Aviso técnico" to "Há muitos itens não classificados. Isso pode reduzir a confiabilidade da análise."
	}

	selectedInput.features.forEach { (name, value) ->
		technical += "Feature $name" to value.toNumberText()
	}

	behaviorResult.profileScores
		.toList()
		.sortedByDescending { (_, score) -> score }
		.take(3)
		.forEach { (profile, score) ->
			technical += "Probabilidade ${profile.toDisplayName()}" to score.toPercentageText()
		}

	return HomeAnalysisPresentation(
		profileTitle = summary.toPresentationTitle(behaviorResult.mainProfile),
		profileDescription = behaviorResult.mainProfile.toDescription(),
		consumptionReading = buildBehavioralReading(this),
		confidenceLabel = behaviorResult.confidence.toConfidenceLabel(),
		sourceLabel = behaviorResult.source.toDisplayLabel(),
		sourceWarning = behaviorResult.source.toWarningMessage(behaviorResult.fallbackReason),
		primarySignals = buildPrimarySignals(this),
		technicalItems = technical
	)
}

private fun buildPrimarySignals(analysis: StoredConsumptionAnalysis): List<String> {
	val metrics = analysis.metrics
	val mainProfile = analysis.behaviorResult.mainProfile
	val alcoholicProfileScore = analysis.behaviorResult.profileScores[
		ConsumptionBehaviorProfile.ALCOHOLIC_BEVERAGE_RECURRENT
	].orZero()

	return buildList {
		when (mainProfile) {
			ConsumptionBehaviorProfile.ALCOHOLIC_BEVERAGE_RECURRENT -> {
				add("Bebidas alcoólicas presentes em ${metrics.alcoholicBeverageFrequency.toPercentageText()} das notas")
				add("Bebidas alcoólicas + snacks em ${metrics.alcoholSnackCoOccurrenceFrequency.toPercentageText()} das notas")
				add("Bebidas + snacks em ${metrics.beverageSnackCoOccurrenceFrequency.toPercentageText()} das notas")
			}
			ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT -> {
				add("Bebidas presentes em ${metrics.frequencyByCategory[ProductCategory.BEVERAGES].orZero().toPercentageText()} das notas")
				add("Bebidas + snacks em ${metrics.beverageSnackCoOccurrenceFrequency.toPercentageText()} das notas")
				if (metrics.softDrinkFrequency >= 0.20) {
					add("Refrigerantes presentes em ${metrics.softDrinkFrequency.toPercentageText()} das notas")
				}
			}
			else -> {
				if (metrics.alcoholicBeverageFrequency >= 0.30) {
					add("Bebidas alcoólicas presentes em ${metrics.alcoholicBeverageFrequency.toPercentageText()} das notas")
				}
				add("Bebidas presentes em ${metrics.frequencyByCategory[ProductCategory.BEVERAGES].orZero().toPercentageText()} das notas")
				add("Bebidas + snacks em ${metrics.beverageSnackCoOccurrenceFrequency.toPercentageText()} das notas")
				if (metrics.softDrinkFrequency >= 0.20) {
					add("Refrigerantes presentes em ${metrics.softDrinkFrequency.toPercentageText()} das notas")
				}
			}
		}
		add("Alimentação básica representa ${metrics.valuePercentageByCategory[ProductCategory.BASIC_FOOD].orZero().toPercentageText()} do valor")
		add("Recorrência de itens em ${metrics.recurringItemRatio.toPercentageText()} das compras")
	}.take(5)
}

private fun buildBehavioralReading(analysis: StoredConsumptionAnalysis): String {
	val metrics = analysis.metrics
	val mainProfile = analysis.behaviorResult.mainProfile
	val alcoholicProfileScore = analysis.behaviorResult.profileScores[
		ConsumptionBehaviorProfile.ALCOHOLIC_BEVERAGE_RECURRENT
	].orZero()

	val beverageFrequency = metrics.frequencyByCategory[ProductCategory.BEVERAGES].orZero().toPercentageText()
	val essentialValue = metrics.valuePercentageByCategory[ProductCategory.BASIC_FOOD].orZero().toPercentageText()
	val beverageSnackFrequency = metrics.beverageSnackCoOccurrenceFrequency.toPercentageText()
	val alcoholSnackFrequency = metrics.alcoholSnackCoOccurrenceFrequency.toPercentageText()
	val alcoholFrequency = metrics.alcoholicBeverageFrequency.toPercentageText()
	val softDrinkFrequency = metrics.softDrinkFrequency.toPercentageText()

	return buildString {
		when (mainProfile) {
			ConsumptionBehaviorProfile.ALCOHOLIC_BEVERAGE_RECURRENT -> {
				append("As compras analisadas mostram presença recorrente de bebidas alcoólicas")
				append(" ($alcoholFrequency das notas)")
				append(", com combinação frequente com snacks em $alcoholSnackFrequency")
				append(" e contexto ampliado de bebidas + snacks em $beverageSnackFrequency.")
				append("\n\n")
				append("Apesar disso, alimentação básica continua presente em valor ($essentialValue), ")
				append("indicando que o consumo alcoólico coexiste com compras de rotina.")
			}
			ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT -> {
				val alcoholicPatternIsCompetitive = alcoholicProfileScore >= 0.25
				append("As compras analisadas mostram recorrência de bebidas em geral")
				if (metrics.softDrinkFrequency >= 0.20) {
					if (alcoholicPatternIsCompetitive) {
						append(", com presença relevante de refrigerantes")
					} else {
						append(", com destaque para refrigerantes e outras bebidas não alcoólicas")
					}
				}
				append(" ($beverageFrequency das notas)")
				append(" e combinação com snacks em $beverageSnackFrequency.")
				if (metrics.softDrinkFrequency >= 0.20) {
					append(" Refrigerantes aparecem em $softDrinkFrequency das notas.")
				}
				if (alcoholicPatternIsCompetitive) {
					append("\n\n")
					append("Também há sinais secundários de bebidas alcoólicas recorrentes ($alcoholFrequency), ")
					append("mas eles não superam o padrão principal identificado.")
				}
				append("\n\n")
				append("Alimentação básica continua relevante em valor ($essentialValue), ")
				append("o que sugere uma rotina de consumo complementar e não restrita a ocasiões pontuais.")
			}
			else -> {
				append("As compras analisadas mostram um padrão variado, com presença frequente de bebidas")
				append(" ($beverageFrequency das notas)")
				append(" e combinação com snacks em $beverageSnackFrequency.")
				append("\n\n")
				append("Apesar disso, alimentação básica continua relevante em valor ($essentialValue), ")
				append("indicando que o consumo não está concentrado apenas em conveniência. ")
				append("A diversidade entre categorias sugere uma rotina relativamente equilibrada, com presença complementar de itens domésticos e higiene.")
			}
		}
	}
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
		ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT -> "Recorrência de bebidas não alcoólicas"
		ConsumptionBehaviorProfile.ALCOHOLIC_BEVERAGE_RECURRENT -> "Recorrência de bebidas alcoólicas"
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
		ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT -> "Bebidas não alcoólicas aparecem com recorrência relevante nas notas analisadas."
		ConsumptionBehaviorProfile.ALCOHOLIC_BEVERAGE_RECURRENT -> "Bebidas alcoólicas aparecem com recorrência relevante nas notas analisadas."
		ConsumptionBehaviorProfile.LOW_FRESH_FOOD -> "Baixa participação de hortifruti e alimentos frescos no consumo analisado."
		ConsumptionBehaviorProfile.HOUSEHOLD_MAINTENANCE -> "Maior presença de produtos de higiene e limpeza doméstica."
		ConsumptionBehaviorProfile.HIGHLY_CONCENTRATED -> "Grande parte do consumo está concentrada em poucas categorias."
		ConsumptionBehaviorProfile.IMPULSIVE_CONSUMPTION -> "Maior presença de categorias não essenciais e compras de conveniência."
		ConsumptionBehaviorProfile.UNDEFINED -> "Não foi possível identificar um padrão confiável com os dados atuais."
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

private fun BehaviorClassificationSource.toWarningMessage(fallbackReason: FallbackReason?): String? {
	return when (this) {
		BehaviorClassificationSource.TRAINED_MODEL -> null
		BehaviorClassificationSource.RULE_BASED_FALLBACK -> buildString {
			if (fallbackReason == FallbackReason.BACKEND_REJECTED_INPUT) {
				append("Não foi possível usar o modelo treinado. Resultado gerado localmente.")
			} else {
				append("Backend indisponível. Resultado gerado por fallback local")
				fallbackReason?.let { append(" (${it.name})") }
				append('.')
			}
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


private fun Double.toPercentageText(): String = "${"%.1f".format(Locale.US, this * 100)}%"

private fun Double.toNumberText(): String = "%.4f".format(Locale.US, this)

private fun Double?.orZero(): Double = this ?: 0.0


