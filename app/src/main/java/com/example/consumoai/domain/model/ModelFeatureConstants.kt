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
 * Ordem OFICIAL das 15 features enviadas ao backend XGBoost final.
 * Ordem fixa: extraído do consumoai_final_features.json do backend.
 * Modelo: consumoai_xgboost_beverage_split_top15.pkl.
 * Label Encoder: consumoai_label_encoder_final.pkl.
 *
 * Mantida fixa para preservar compatibilidade 1:1 com o modelo treinado.
 * QUALQUER alternação aqui quebra a integração com o backend.
 *
 * Features incluídas (estritamente nesta ordem):
 * 1. non_alcoholic_beverage_frequency
 * 2. category_concentration_index
 * 3. classified_items_percentage
 * 4. non_alcoholic_beverage_snack_cooccurrence_frequency
 * 5. household_routine_score
 * 6. alcoholic_beverage_frequency
 * 7. produce_frequency
 * 8. essential_routine_score
 * 9. category_dominance_gap
 * 10. category_stability_score
 * 11. essential_score
 * 12. other_value_pct
 * 13. hygiene_cleaning_cooccurrence_frequency
 * 14. basic_produce_cooccurrence_frequency
 * 15. alcohol_snack_cooccurrence_frequency
 *
 * Métricas calculadas internamente (NÃO enviadas ao modelo):
 * - soft_drink_frequency (mantida para análise/UI)
 * - soft_drink_value_pct (mantida para análise/UI)
 * - alcoholic_beverage_value_pct (mantida para análise/UI)
 * - non_alcoholic_beverage_value_pct (mantida para análise/UI)
 * - energy_drink_frequency (mantida para análise/UI)
 * - energy_drink_value_pct (mantida para análise/UI)
 */
val MODEL_FINAL_FEATURES = listOf(
    "non_alcoholic_beverage_frequency",
    "category_concentration_index",
    "classified_items_percentage",
    "non_alcoholic_beverage_snack_cooccurrence_frequency",
    "household_routine_score",
    "alcoholic_beverage_frequency",
    "produce_frequency",
    "essential_routine_score",
    "category_dominance_gap",
    "category_stability_score",
    "essential_score",
    "other_value_pct",
    "hygiene_cleaning_cooccurrence_frequency",
    "basic_produce_cooccurrence_frequency",
    "alcohol_snack_cooccurrence_frequency"
)

