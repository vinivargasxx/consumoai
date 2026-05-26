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
import com.example.consumoai.domain.model.ConsumptionMetrics
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.topProfiles
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

fun StoredConsumptionAnalysis.toHomeAnalysisPresentation(): HomeAnalysisPresentation {
	val selectedInput = modelInput
	val metrics = this.metrics
	val topProfiles = behaviorResult.topProfiles(limit = 3)
	val interpretationType = determineInterpretationType(behaviorResult)
	val secondaryProfileSummary = topProfiles.getOrNull(1)
	val secondaryProfileTitle = secondaryProfileSummary
		?.takeIf { it.score >= 0.25 }
		?.profile
		?.toDisplayName()
	val secondaryProfileDescription = secondaryProfileSummary
		?.takeIf { it.score >= 0.25 }
		?.profile
		?.toDescription()
	val explanationSignals = buildExplanationSignals(metrics, behaviorResult)

	val technical = mutableListOf<Pair<String, String>>()
	technical += "Modelo" to MODEL_NAME
	technical += "Backend" to (behaviorResult.backendModelUsed ?: MODEL_BACKEND_IDENTIFIER)
	technical += "Features enviadas" to "$MODEL_FEATURE_COUNT"
	technical += "Versão" to (behaviorResult.requestedInputVersion ?: selectedInput.version)
	technical += "Classes" to MODEL_CLASS_COUNT.toString()
	technical += "Fonte da classificação" to behaviorResult.source.toTechnicalLabel()
	technical += "Confiança" to behaviorResult.confidence.toPercentageText()
	technical += "Tipo de interpretação" to interpretationType.name
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

	topProfiles.forEach { summary ->
		technical += "Probabilidade ${summary.profile.toDisplayName()}" to summary.score.toPercentageText()
		}

	return HomeAnalysisPresentation(
		profileTitle = behaviorResult.mainProfile.toPresentationTitle(interpretationType),
		profileDescription = behaviorResult.mainProfile.toDescription(),
		consumptionReading = buildFinancialInterpretation(this),
		confidenceLabel = behaviorResult.confidence.toConfidenceLabel(),
		sourceLabel = behaviorResult.source.toDisplayLabel(),
		sourceWarning = behaviorResult.source.toWarningMessage(behaviorResult.fallbackReason),
		primarySignals = buildFinancialSignals(this),
		technicalItems = technical,
		interpretationType = interpretationType,
		secondaryProfileTitle = secondaryProfileTitle,
		secondaryProfileDescription = secondaryProfileDescription,
		explanationSignals = explanationSignals,
		technicalExplanation = buildTechnicalExplanation(
			interpretationType = interpretationType,
			topProfiles = topProfiles,
			metrics = metrics
		)
	)
}

private fun buildFinancialInterpretation(analysis: StoredConsumptionAnalysis): String {
	val metrics = analysis.metrics
	val result = analysis.behaviorResult
	val mainProfile = result.mainProfile
	val interpretationType = determineInterpretationType(result)
	val secondary = result.topProfiles(limit = 2).getOrNull(1)
		?.takeIf { interpretationType == ProfileInterpretationType.HYBRID_PROFILE && it.score >= 0.25 }
		?.profile

	val dominantCategory = categoryDisplayName(metrics.maxCategoryByValue)
	val dominantPct = metrics.valuePercentageByCategory[metrics.maxCategoryByValue].orZero()
	val dominantValue = valueFromPct(metrics.totalValue, dominantPct)

	val hybridPrefix = if (secondary != null) {
		"Embora o perfil principal seja ${mainProfile.toDisplayName()}, também há sinais de ${secondary.toDisplayName().lowercase()}.\n\n"
	} else {
		""
	}

	return hybridPrefix + when (mainProfile) {
		ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT -> {
			val freq = metrics.nonAlcoholicBeverageFrequency
			val valuePct = metrics.nonAlcoholicBeverageValuePct
			val impact = valueFromPct(metrics.totalValue, valuePct)
			buildString {
				append("Bebidas não alcoólicas aparecem em ${freq.toPercentageText()} das compras analisadas, indicando um hábito recorrente.")
				append("\n\n")
				append("Financeiramente, esse grupo representa ${valuePct.toPercentageText()} do total gasto (${impact.toCurrencyText()} no período analisado).")
				append("\n\n")
				if (valuePct >= 0.15) {
					append("Além da alta frequência, esse grupo também possui participação financeira relevante, indicando um ponto importante para acompanhamento do orçamento.")
				} else {
					append("Embora não seja necessariamente a categoria de maior impacto financeiro, a recorrência elevada sugere pequenos gastos frequentes, que podem passar despercebidos no orçamento diário.")
				}
				if (metrics.maxCategoryByValue != ProductCategory.BEVERAGES && dominantPct > 0.0) {
					append("\n\n")
					append("A maior concentração de valor está em $dominantCategory, com ${dominantPct.toPercentageText()} do total. Assim, o perfil identificado representa mais um padrão de frequência do que o maior peso financeiro.")
				}
			}
		}
		ConsumptionBehaviorProfile.ALCOHOLIC_BEVERAGE_RECURRENT -> {
			val freq = metrics.alcoholicBeverageFrequency
			val valuePct = metrics.alcoholicBeverageValuePct
			val impact = valueFromPct(metrics.totalValue, valuePct)
			buildString {
				append("Bebidas alcoólicas aparecem em ${freq.toPercentageText()} das compras analisadas, indicando um padrão recorrente associado a momentos sociais, conveniência ou compras complementares.")
				append("\n\n")
				append("Financeiramente, esse grupo representa ${valuePct.toPercentageText()} do total gasto (${impact.toCurrencyText()} no período analisado).")
				append("\n\n")
				append("O sistema interpreta esse comportamento pela repetição nas notas, não necessariamente por ser o maior gasto do período.")
				if (metrics.alcoholSnackCoOccurrenceFrequency >= 0.20) {
					append("\n\n")
					append("A combinação com snacks também aparece com frequência, reforçando um padrão de compra complementar.")
				}
			}
		}
		ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED -> {
			val essentialPct = essentialValuePct(metrics)
			val essentialValue = valueFromPct(metrics.totalValue, essentialPct)
			"As compras analisadas indicam predominância de itens essenciais, como alimentação básica, hortifruti, higiene ou limpeza.\n\n" +
				"Financeiramente, categorias essenciais representam ${essentialPct.toPercentageText()} do total gasto (${essentialValue.toCurrencyText()} no período analisado).\n\n" +
				"Esse padrão sugere maior direcionamento do orçamento para necessidades recorrentes, o que pode indicar um consumo mais planejado."
		}
		ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED -> {
			val convenienceFreq = maxOf(metrics.receiptsWithIndustrializedPercentage, metrics.receiptsWithBeveragesPercentage)
			val conveniencePct = metrics.valuePercentageByCategory[ProductCategory.INDUSTRIALIZED].orZero() +
				metrics.valuePercentageByCategory[ProductCategory.BEVERAGES].orZero()
			"As notas indicam presença relevante de itens de conveniência, industrializados ou compras práticas.\n\n" +
				"Esse padrão aparece em ${convenienceFreq.toPercentageText()} das compras analisadas e representa ${conveniencePct.toPercentageText()} do valor total.\n\n" +
				"Gastos de conveniência costumam ocorrer em pequenas compras recorrentes, por isso podem impactar o orçamento mesmo quando não parecem altos isoladamente."
		}
		ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED -> {
			"As compras apresentam distribuição equilibrada entre diferentes categorias, sem concentração excessiva em um único grupo.\n\n" +
				"A categoria de maior gasto foi $dominantCategory, com ${dominantPct.toPercentageText()} do total (${dominantValue.toCurrencyText()}).\n\n" +
				"Esse padrão sugere uma cesta de consumo variada, com menor dependência de uma única categoria."
		}
		ConsumptionBehaviorProfile.HOUSEHOLD_MAINTENANCE -> {
			val householdPct = metrics.valuePercentageByCategory[ProductCategory.HYGIENE].orZero() +
				metrics.valuePercentageByCategory[ProductCategory.CLEANING].orZero()
			"As notas indicam presença recorrente de itens de higiene, limpeza e manutenção doméstica.\n\n" +
				"Financeiramente, esses itens representam ${householdPct.toPercentageText()} do total gasto.\n\n" +
				"Esse padrão mostra parte do orçamento direcionada à manutenção da casa, um tipo de gasto recorrente que pode ser planejado com antecedência."
		}
		ConsumptionBehaviorProfile.LOW_FRESH_FOOD -> {
			val producePct = metrics.valuePercentageByCategory[ProductCategory.PRODUCE].orZero()
			"As compras analisadas indicam baixa presença de hortifruti e alimentos frescos.\n\n" +
				"Hortifruti aparece em ${metrics.receiptsWithProducePercentage.toPercentageText()} das notas e representa ${producePct.toPercentageText()} do valor total.\n\n" +
				"Esse sinal pode ajudar o usuário a visualizar a composição da cesta de compras e refletir sobre o espaço de alimentos frescos no orçamento."
		}
		ConsumptionBehaviorProfile.HIGHLY_CONCENTRATED -> {
			"O consumo está concentrado em poucas categorias.\n\n" +
				"A principal categoria foi $dominantCategory, representando ${dominantPct.toPercentageText()} do valor total (${dominantValue.toCurrencyText()}).\n\n" +
				"Esse padrão pode indicar dependência de poucos grupos de gasto, o que ajuda o usuário a entender onde está a maior parte do orçamento."
		}
		ConsumptionBehaviorProfile.IMPULSIVE_CONSUMPTION -> {
			"As compras apresentam sinais de maior variação e presença de itens não essenciais.\n\n" +
				"Esses sinais podem indicar compras menos planejadas ou mais influenciadas por conveniência e oportunidade.\n\n" +
				"Acompanhar esses gastos ajuda a identificar pequenas despesas recorrentes que podem impactar o orçamento ao longo do tempo."
		}
		ConsumptionBehaviorProfile.UNDEFINED -> {
			"Os dados atuais ainda não mostram um padrão dominante com confiança alta.\n\n" +
				"Mesmo assim, o acompanhamento de frequência e participação financeira por categoria já permite identificar hábitos recorrentes e oportunidades de ajuste no orçamento."
		}
	}
}

private fun buildFinancialSignals(analysis: StoredConsumptionAnalysis): List<String> {
	val metrics = analysis.metrics
	val mainProfile = analysis.behaviorResult.mainProfile
	val dominantCategory = categoryDisplayName(metrics.maxCategoryByValue)
	val dominantPct = metrics.valuePercentageByCategory[metrics.maxCategoryByValue].orZero()

	return buildList {
		when (mainProfile) {
			ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT -> {
				val impact = valueFromPct(metrics.totalValue, metrics.nonAlcoholicBeverageValuePct)
				add("Presente em ${metrics.nonAlcoholicBeverageFrequency.toPercentageText()} das notas")
				add("Representa ${metrics.nonAlcoholicBeverageValuePct.toPercentageText()} do valor total")
				add("Equivale a ${impact.toCurrencyText()} no período")
				add("Maior gasto: $dominantCategory, ${dominantPct.toPercentageText()}")
			}
			ConsumptionBehaviorProfile.ALCOHOLIC_BEVERAGE_RECURRENT -> {
				add("Presente em ${metrics.alcoholicBeverageFrequency.toPercentageText()} das notas")
				add("Representa ${metrics.alcoholicBeverageValuePct.toPercentageText()} do valor total")
				if (metrics.alcoholSnackCoOccurrenceFrequency > 0.0) {
					add("Álcool + snacks em ${metrics.alcoholSnackCoOccurrenceFrequency.toPercentageText()} das notas")
				}
				add("Maior gasto: $dominantCategory, ${dominantPct.toPercentageText()}")
			}
			ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED -> {
				val essentialPct = essentialValuePct(metrics)
				add("Itens essenciais representam ${essentialPct.toPercentageText()} do valor")
				add("Hortifruti aparece em ${metrics.receiptsWithProducePercentage.toPercentageText()} das notas")
				add("Alimentação básica é a maior categoria")
			}
			else -> {
				add("Maior gasto: $dominantCategory, ${dominantPct.toPercentageText()}")
				add("Score essencial: ${metrics.essentialScore.toPercentageText()}")
				add("Hortifruti em ${metrics.receiptsWithProducePercentage.toPercentageText()} das notas")
				if (metrics.householdRoutineScore >= 0.35) {
					add("Higiene/limpeza com rotina de ${metrics.householdRoutineScore.toPercentageText()}")
				}
			}
		}
	}.take(5)
}

private fun ConsumptionBehaviorProfile.toPresentationTitle(
	interpretationType: ProfileInterpretationType
): String {
	return when (interpretationType) {
		ProfileInterpretationType.PURE_PROFILE -> toDisplayName()
		ProfileInterpretationType.HYBRID_PROFILE -> "Leitura híbrida: ${toDisplayName()}"
		ProfileInterpretationType.LOW_CONFIDENCE_PROFILE -> "Leitura exploratória: ${toDisplayName()}"
	}
}

private fun determineInterpretationType(result: ConsumptionBehaviorResult): ProfileInterpretationType {
	val topProfiles = result.topProfiles(limit = 2)
	val firstScore = topProfiles.getOrNull(0)?.score ?: result.confidence
	val secondScore = topProfiles.getOrNull(1)?.score ?: 0.0
	val scoreGap = firstScore - secondScore

	return when {
		result.confidence < 0.50 -> ProfileInterpretationType.LOW_CONFIDENCE_PROFILE
		secondScore >= 0.25 && scoreGap <= 0.35 -> ProfileInterpretationType.HYBRID_PROFILE
		else -> ProfileInterpretationType.PURE_PROFILE
	}
}

private fun buildExplanationSignals(
	metrics: ConsumptionMetrics,
	result: ConsumptionBehaviorResult
): List<String> {
	return buildList {
		val topProfiles = result.topProfiles(limit = 2)
		val secondary = topProfiles.getOrNull(1)
		if (determineInterpretationType(result) == ProfileInterpretationType.HYBRID_PROFILE && secondary != null) {
			add("Leitura híbrida: além de ${result.mainProfile.toDisplayName().lowercase()}, há sinais de ${secondary.profile.toDisplayName().lowercase()} (${secondary.score.toPercentageText()}).")
		}
		if (metrics.alcoholicBeverageFrequency >= 0.30) {
			add("Bebidas alcoólicas aparecem em ${metrics.alcoholicBeverageFrequency.toPercentageText()} das notas analisadas.")
		}
		if (metrics.alcoholSnackCoOccurrenceFrequency >= 0.20) {
			add("Bebidas alcoólicas aparecem junto com snacks em ${metrics.alcoholSnackCoOccurrenceFrequency.toPercentageText()} das notas.")
		}
		if (metrics.nonAlcoholicBeverageFrequency >= 0.50) {
			add("Bebidas não alcoólicas também aparecem com alta recorrência (${metrics.nonAlcoholicBeverageFrequency.toPercentageText()}).")
		}
		if (metrics.essentialScore >= 0.55) {
			add("Há presença consistente de itens essenciais, indicando compras de rotina (score ${metrics.essentialScore.toPercentageText()}).")
		}
		if (metrics.receiptsWithProducePercentage >= 0.40) {
			add("Hortifruti aparece em parte relevante das compras (${metrics.receiptsWithProducePercentage.toPercentageText()}).")
		}
		if (metrics.householdRoutineScore >= 0.35) {
			add("Itens de higiene e limpeza aparecem como rotina secundária (${metrics.householdRoutineScore.toPercentageText()}).")
		}
		if (isEmpty()) {
			add("Os sinais estão distribuídos entre categorias, sem concentração extrema em um único padrão.")
		}
		if (size < 3) {
			add("O modelo também identificou sinais secundários em outros perfis com menor intensidade.")
		}
	}
}

private fun buildTechnicalExplanation(
	interpretationType: ProfileInterpretationType,
	topProfiles: List<com.example.consumoai.domain.model.ProfileScoreSummary>,
	metrics: ConsumptionMetrics
): List<String> {
	val primary = topProfiles.firstOrNull()
	val secondary = topProfiles.getOrNull(1)
	return buildList {
		primary?.let {
			add("Perfil principal: ${it.profile.toDisplayName()}")
			add("Score principal: ${it.score.toPercentageText()}")
		}
		secondary?.let {
			add("Segundo padrão: ${it.profile.toDisplayName()} (${it.score.toPercentageText()})")
		}
		add("Tipo de leitura: ${interpretationType.toInterpretationDisplayName()}")
		add("alcoholic_beverage_frequency = ${metrics.alcoholicBeverageFrequency.toPercentageText()}")
		add("alcohol_snack_cooccurrence_frequency = ${metrics.alcoholSnackCoOccurrenceFrequency.toPercentageText()}")
		add("non_alcoholic_beverage_frequency = ${metrics.nonAlcoholicBeverageFrequency.toPercentageText()}")
		add("essential_score = ${metrics.essentialScore.toPercentageText()}")
	}
}

fun ConsumptionBehaviorProfile.toDisplayName(): String {
	return when (this) {
		ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED -> "Consumo orientado à praticidade"
		ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED -> "Consumo focado no essencial"
		ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED -> "Consumo diversificado e equilibrado"
		ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT -> "Rotina de consumo com bebidas e conveniência"
		ConsumptionBehaviorProfile.ALCOHOLIC_BEVERAGE_RECURRENT -> "Padrão social de bebidas"
		ConsumptionBehaviorProfile.LOW_FRESH_FOOD -> "Baixa presença de alimentos frescos"
		ConsumptionBehaviorProfile.HOUSEHOLD_MAINTENANCE -> "Rotina de manutenção doméstica"
		ConsumptionBehaviorProfile.HIGHLY_CONCENTRATED -> "Consumo concentrado em poucas categorias"
		ConsumptionBehaviorProfile.IMPULSIVE_CONSUMPTION -> "Sinais de consumo impulsivo"
		ConsumptionBehaviorProfile.UNDEFINED -> "Padrão ainda indefinido"
	}
}

fun ConsumptionBehaviorProfile.toDescription(): String {
	return when (this) {
		ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED -> "Itens de conveniência e compras práticas aparecem com frequência nas notas analisadas."
		ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED -> "Itens essenciais aparecem com maior regularidade e peso no consumo analisado."
		ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED -> "As compras se distribuem de forma equilibrada entre diferentes categorias."
		ConsumptionBehaviorProfile.NON_ALCOHOLIC_BEVERAGE_RECURRENT -> "Bebidas não alcoólicas e itens de conveniência aparecem de forma recorrente nas notas analisadas."
		ConsumptionBehaviorProfile.ALCOHOLIC_BEVERAGE_RECURRENT -> "Bebidas alcoólicas aparecem de forma recorrente nas notas, especialmente associadas a snacks ou compras de conveniência."
		ConsumptionBehaviorProfile.LOW_FRESH_FOOD -> "Hortifruti e alimentos frescos têm participação reduzida nas compras analisadas."
		ConsumptionBehaviorProfile.HOUSEHOLD_MAINTENANCE -> "Higiene, limpeza e manutenção da casa aparecem com recorrência."
		ConsumptionBehaviorProfile.HIGHLY_CONCENTRATED -> "Grande parte do valor está concentrada em poucas categorias de compra."
		ConsumptionBehaviorProfile.IMPULSIVE_CONSUMPTION -> "Há sinais de variação e compras menos planejadas ao longo do período."
		ConsumptionBehaviorProfile.UNDEFINED -> "Não foi possível identificar um padrão confiável com os dados atuais."
	}
}

private fun ProfileInterpretationType.toInterpretationDisplayName(): String {
	return when (this) {
		ProfileInterpretationType.PURE_PROFILE -> "Predominante"
		ProfileInterpretationType.HYBRID_PROFILE -> "Híbrida"
		ProfileInterpretationType.LOW_CONFIDENCE_PROFILE -> "Baixa confiança"
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


private fun Double.toPercentageText(): String = "${"%.1f".format(PT_BR, this * 100)}%"

private fun Double.toCurrencyText(): String {
	val formatter = NumberFormat.getCurrencyInstance(PT_BR)
	formatter.currency = Currency.getInstance("BRL")
	return formatter.format(this)
}

private fun valueFromPct(total: Double, pct: Double): Double = total * pct.coerceIn(0.0, 1.0)

private fun categoryDisplayName(category: ProductCategory?): String {
	return when (category) {
		ProductCategory.BASIC_FOOD -> "Alimentação básica"
		ProductCategory.INDUSTRIALIZED -> "Industrializados"
		ProductCategory.BEVERAGES -> "Bebidas"
		ProductCategory.HYGIENE -> "Higiene"
		ProductCategory.CLEANING -> "Limpeza"
		ProductCategory.PRODUCE -> "Hortifruti"
		ProductCategory.OTHER, null -> "Outros"
	}
}

private fun essentialValuePct(metrics: ConsumptionMetrics): Double {
	return metrics.valuePercentageByCategory[ProductCategory.BASIC_FOOD].orZero() +
		metrics.valuePercentageByCategory[ProductCategory.PRODUCE].orZero() +
		metrics.valuePercentageByCategory[ProductCategory.HYGIENE].orZero() +
		metrics.valuePercentageByCategory[ProductCategory.CLEANING].orZero()
}

private fun Double?.orZero(): Double = this ?: 0.0

private fun Double.toNumberText(): String = "%.4f".format(Locale.US, this)

private val PT_BR: Locale = Locale("pt", "BR")


