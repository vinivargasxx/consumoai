package com.example.consumoai.domain.model

// Resultado consolidado da classificação de perfil de consumo.
data class ConsumptionBehaviorResult(
    // Perfil principal escolhido como saída final da classificação.
    val mainProfile: ConsumptionBehaviorProfile,
    // Confiança numérica da saída principal.
    val confidence: Double,
    // Pontuações de todos os perfis avaliados pelo classificador.
    val profileScores: Map<ConsumptionBehaviorProfile, Double>,
    // Origem da classificação: modelo treinado ou fallback local.
    val source: BehaviorClassificationSource,
    // Resumo interpretativo montado para exibição mais humana.
    val profileSummary: ConsumptionProfileSummary? = null,
    // Motivo do fallback, quando o backend/modelo principal não foi usado.
    val fallbackReason: FallbackReason? = null,
    // Tempo gasto para produzir a classificação.
    val inferenceDurationMs: Long = 0L,
    // Versão do input efetivamente enviada ao backend.
    val requestedInputVersion: String? = null,
    // Quantidade de features efetivamente enviada ao backend.
    val requestedFeatureCount: Int? = null,
    // Versão informada na resposta do backend, quando disponível.
    val responseVersion: String? = null,
    // Quantidade de features informada na resposta do backend, quando disponível.
    val responseFeatureCount: Int? = null,
    // Nome do modelo usado no backend, quando disponível.
    val backendModelUsed: String? = null,
    // Indica se o input precisou ser saneado antes da inferência.
    val usedSanitizedInput: Boolean = false,
    // Lista de ajustes aplicados no saneamento do input.
    val sanitizationNotes: List<FeatureSanitizationNote> = emptyList()
)

enum class BehaviorClassificationSource {
    // Saída produzida pelo modelo treinado/servido pelo backend.
    TRAINED_MODEL,
    // Saída produzida por regras locais quando o modelo principal não está disponível.
    RULE_BASED_FALLBACK
}

