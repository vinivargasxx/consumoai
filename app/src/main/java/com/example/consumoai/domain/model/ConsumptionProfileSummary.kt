package com.example.consumoai.domain.model

enum class ProfileInterpretationType {
    // Leitura em que um único perfil aparece como predominante.
    PURE_PROFILE,
    // Leitura em que há mistura relevante entre dois ou mais perfis.
    HYBRID_PROFILE,
    // Leitura em que o sistema encontrou sinais fracos ou ambíguos.
    LOW_CONFIDENCE_PROFILE
}

// Resumo textual e estrutural da saída do modelo para consumo na UI.
data class ConsumptionProfileSummary(
    // Perfil principal usado como eixo central da interpretação.
    val primaryProfile: ConsumptionBehaviorProfile,
    // Perfis secundários que também influenciaram a leitura final.
    val secondaryProfiles: List<ConsumptionBehaviorProfile>,
    // Confiança consolidada da interpretação exibida.
    val confidence: Double,
    // Tipo de interpretação aplicada sobre o resultado bruto.
    val interpretationType: ProfileInterpretationType,
    // Descrição curta em linguagem humana para exibição no app.
    val humanReadableDescription: String,
    // Composição percentual dos perfis considerados na leitura final.
    val profileComposition: List<BehaviorCompositionItem>
)

