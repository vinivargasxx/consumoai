package com.example.consumoai.domain.model

const val MODEL_INPUT_VERSION = "final"
const val MODEL_NAME = "XGBoost Beverage Split Top15"
const val MODEL_BACKEND_IDENTIFIER = "xgboost_beverage_split_top15"

/**
 * Contagem total de métricas calculadas internamente no app (para narrativa/análise).
 */
const val MODEL_INTERNAL_METRICS_COUNT = 64

/**
 * Contagem de features OFICIAIS enviadas ao modelo XGBoost final.
 * O modelo final usa exatamente 15 features.
 */
const val MODEL_FEATURE_COUNT = 15
const val MODEL_CLASS_COUNT = 10

/**
 * Ordem OFICIAL das 15 features enviadas ao backend XGBoost calibrado.
 * Ordem fixa: extraído do consumoai_model_features.json do backend (dataset sintético calibrado).
 * Modelo: consumoai_xgboost_beverage_split_top15.pkl (retreinado com overlap entre perfis).
 * Label Encoder: consumoai_label_encoder_final.pkl.
 *
 * Mantida fixa para preservar compatibilidade 1:1 com o modelo treinado.
 * QUALQUER alteração na ordem ou nome aqui quebra a integração com o backend.
 *
 * Features incluídas (estritamente nesta ordem):
 * 1.  classified_items_percentage
 * 2.  category_concentration_index
 * 3.  essential_routine_score
 * 4.  household_routine_score
 * 5.  produce_frequency
 * 6.  basic_produce_cooccurrence_frequency
 * 7.  alcoholic_beverage_frequency
 * 8.  essential_score
 * 9.  category_dominance_gap
 * 10. non_alcoholic_beverage_frequency
 * 11. category_stability_score
 * 12. other_value_pct
 * 13. hygiene_cleaning_cooccurrence_frequency
 * 14. soft_drink_frequency
 * 15. ticket_variation_coefficient
 *
 * Métricas calculadas internamente (NÃO enviadas ao modelo):
 * - non_alcoholic_beverage_snack_cooccurrence_frequency (mantida para análise/UI)
 * - alcohol_snack_cooccurrence_frequency (mantida para análise/UI)
 * - alcoholic_beverage_value_pct (mantida para análise/UI)
 * - non_alcoholic_beverage_value_pct (mantida para análise/UI)
 * - energy_drink_frequency (mantida para análise/UI)
 * - energy_drink_value_pct (mantida para análise/UI)
 * - soft_drink_value_pct (mantida para análise/UI)
 */
val MODEL_FINAL_FEATURES = listOf(
    "classified_items_percentage",
    "category_concentration_index",
    "essential_routine_score",
    "household_routine_score",
    "produce_frequency",
    "basic_produce_cooccurrence_frequency",
    "alcoholic_beverage_frequency",
    "essential_score",
    "category_dominance_gap",
    "non_alcoholic_beverage_frequency",
    "category_stability_score",
    "other_value_pct",
    "hygiene_cleaning_cooccurrence_frequency",
    "soft_drink_frequency",
    "ticket_variation_coefficient"
)

