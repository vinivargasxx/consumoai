package com.example.consumoai.domain.model

/**
 * Perfis comportamentais retornados pela classificação (modelo treinado ou fallback técnico).
 */
enum class ConsumptionBehaviorProfile {
    // Perfil com maior peso de praticidade, industrializados e compras rápidas.
    CONVENIENCE_ORIENTED,
    // Perfil com predominância de itens essenciais e alimentação básica.
    ESSENTIAL_FOCUSED,
    // Perfil com distribuição mais equilibrada entre diferentes categorias de consumo.
    DIVERSIFIED_BALANCED,
    // Perfil em que bebidas não alcoólicas aparecem com alta recorrência.
    NON_ALCOHOLIC_BEVERAGE_RECURRENT,
    // Perfil em que bebidas alcoólicas (cerveja, vinho, chopp, etc.) aparecem com recorrência relevante.
    ALCOHOLIC_BEVERAGE_RECURRENT,
    // Perfil com baixa presença de hortifruti e alimentos frescos.
    LOW_FRESH_FOOD,
    // Perfil com destaque para higiene, limpeza e manutenção da casa.
    HOUSEHOLD_MAINTENANCE,
    // Perfil com gasto concentrado em poucas categorias dominantes.
    HIGHLY_CONCENTRATED,
    // Perfil com maior presença de compras não essenciais e sinais de impulso.
    IMPULSIVE_CONSUMPTION,
    // Saída usada quando não há confiança suficiente para definir um padrão claro.
    UNDEFINED
}

