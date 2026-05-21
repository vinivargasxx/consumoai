package com.example.consumoai.data.classifier

data class ModelPredictionRequestDto(
    val version: String,
    val features: Map<String, Double>
)

// Resposta bruta devolvida pelo serviço de inferência.
data class ModelPredictionResponseDto(
    // Nome do perfil principal retornado pelo modelo.
    val main_profile: String,
    // Confiança numérica associada ao perfil principal.
    val confidence: Double,
    // Pontuação de cada perfil considerado na inferência.
    val profile_scores: Map<String, Double>,
    // Versão do payload processado pelo backend (opcional).
    val version: String? = null,
    // Quantidade de features processadas pelo backend (opcional).
    val feature_count: Int? = null,
    // Identificador do modelo carregado no backend (opcional).
    val model: String? = null,
    // Lista de features efetivamente usadas pelo backend (opcional).
    val used_features: List<String>? = null
)
