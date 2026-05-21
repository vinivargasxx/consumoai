# Refatoração ConsumoAI - Modelo Final XGBoost 15 Features

## Visão Geral
Refatoração consolidada para utilizar apenas o modelo XGBoost Beverage Split Top15 (15 features), removendo versionamento legacy (V2/V3/V4), separando perfis de bebidas alcoólicas/não-alcoólicas e removendo métricas genéricas redundantes.

## Mudanças Implementadas

### 1. **ConsumptionMetrics.kt**
- ✅ Removido: `beverageRoutineScore` (deprecated)
- **Motivo**: Redundante com métricas específicas (`softDrinkFrequency`, `alcoholicBeverageFrequency`, `nonAlcoholicBeverageFrequency`)
- **Impacto**: Reduz confusão, força uso de métricas específicas

### 2. **CalculateConsumptionMetricsUseCase.kt**
- ✅ Removido: Cálculo de `beverageRoutineScore` 
- ✅ Mantido: Todos os cálculos de bebidas específicas (alcoólicas, não-alcoólicas, soft drinks, energy drinks)
- **Linha removida**: `beverageRoutineScore = averageOf(frequencyByCategory.valueOf(...), ...)`
- **Validação**: Inicialização de métricas vazias (emptyReceipts) também atualizada

### 3. **BuildConsumptionModelInputUseCase.kt**
- ✅ Removido: `beverage_routine_score` do mapa de features
- ✅ Adicionado: Import de `Log` (Android)
- ✅ Adicionado: TAG para logging = "MODEL_INPUT"
- ✅ Adicionado: Nome acompanhamento de logs detalhado
  ```kotlin
  Log.d(TAG, "version=$MODEL_INPUT_VERSION feature_count=${selectedFeatures.size} features=${MODEL_FINAL_FEATURES.joinToString(...)}")
  ```
- **Validação**: Confirma que exatamente 15 features são enviadas

### 4. **RemoteConsumptionBehaviorClassifier.kt**
- ✅ Adicionado: Import de `MODEL_FEATURE_COUNT`
- ✅ Adicionado: Validação pré-requisito
  ```kotlin
  check(input.features.size == MODEL_FEATURE_COUNT) { 
    "Esperadas $MODEL_FEATURE_COUNT features, recebidas ${input.features.size}"
  }
  ```
- ✅ Adicionado: Logs melhorados com feature_count esperado
  ```kotlin
  safeLogDebug(REQUEST_TAG, "version=${input.version} feature_count=${input.features.size} expected=$MODEL_FEATURE_COUNT")
  safeLogDebug(RESPONSE_TAG, "...feature_count=${response.feature_count ?: input.features.size} expected=$MODEL_FEATURE_COUNT...")
  ```

### 5. **ModelFeatureConstants.kt**
- ✅ Documentação expandida
- ✅ Adicionado: Comentário sobre as 15 features (numeradas)
- ✅ Adicionado: Seção "Métricas genéricas/removidas"
  - `beverages_frequency` (genérica)
  - `beverage_routine_score` (redundante)
  - `beverage_snack_cooccurrence_frequency` (genérica, substituída por alcoólica/não-alcoólica específicas)

### 6. **Testes - BuildConsumptionModelInputUseCaseTest.kt**
- ✅ Validação já existente:
  - Version = "final"
  - Features.size == 15
  - Ordem é MODEL_FINAL_FEATURES
  - Presença de todas as 15 features específicas
  - Ausência de `beverages_frequency`
  - Integridade (sem NaN/Infinity)

### 7. **Testes - CalculateConsumptionMetricsUseCaseTest.kt**
- ✅ Adicionado: Teste `invoke_correctlyIdentifiesAlcoholicBeveragesAndSeparatesFromNonAlcoholic()`
  - Valida `alcoholicBeverageFrequency` por tipo de bebida
  - Valida `nonAlcoholicBeverageFrequency` por tipo de bebida
  - Verifica separação clara entre os dois
  
- ✅ Adicionado: Teste `invoke_correctlyComputesSoftDrinkAndEnergyDrinkFrequencies()`
  - Valida detecção de Soft Drink (COCA)
  - Valida detecção de Energy Drink (RED BULL)

- ✅ Adicionado: Teste `invoke_correctlyComputesBeverageAndSnackCoOccurrenceFrequencies()`
  - Valida co-ocorrências específicas
  - Drinks alcoólicos + snacks
  - Drinks não-alcoólicos + snacks

### 8. **Testes - HomeAnalysisPresentationMapperTest.kt**
- ✅ Adicionado: Teste `toHomeAnalysisPresentation_showsNonAlcoholicBeverageProfileWithNonAlcoholicSpecificNarrative()`
  - Valida que NON_ALCOHOLIC_BEVERAGE_RECURRENT mostra narrativa correta
  - Verifica ausência de menção de álcool
  - Valida reconhecimento de soft drinks/energy drinks

### 9. **Testes - CalculateConsumptionMetricsFinalUseCaseTest.kt**
- ✅ Removido: Teste `result.beverageRoutineScore in 0.0..1.0`
- ✅ Mantido: Todos os outros testes de scores específicos

## Validações de Integridade

### ✅ Constantes
- `MODEL_INPUT_VERSION` = "final"
- `MODEL_NAME` = "XGBoost Beverage Split Top15"
- `MODEL_FEATURE_COUNT` = 15
- `MODEL_CLASS_COUNT` = 10
- `MODEL_FINAL_FEATURES.size()` = 15

### ✅ Perfis Comportamentais
- `ALCOHOLIC_BEVERAGE_RECURRENT` mantido
- `NON_ALCOHOLIC_BEVERAGE_RECURRENT` mantido
- `BEVERAGE_RECURRENT` removido (genérico)

### ✅ Métricas de Entrada do Modelo
Exatamente 15 features:
1. `non_alcoholic_beverage_snack_cooccurrence_frequency`
2. `category_concentration_index`
3. `classified_items_percentage`
4. `essential_routine_score`
5. `produce_frequency`
6. `household_routine_score`
7. `soft_drink_frequency`
8. `soft_drink_value_pct`
9. `alcoholic_beverage_frequency`
10. `alcoholic_beverage_value_pct`
11. `non_alcoholic_beverage_frequency`
12. `non_alcoholic_beverage_value_pct`
13. `energy_drink_frequency`
14. `energy_drink_value_pct`
15. `alcohol_snack_cooccurrence_frequency`

## Arquivos Não Modificados (Já estavam OK)

- ✅ **ConsumptionBehaviorProfile.kt** - Enums já corretos
- ✅ **ProductSemanticTag.kt** - Tags já corretas
- ✅ **KeywordProductSemanticTagger.kt** - Keywords já bem estruturadas
- ✅ **HomeAnalysisPresentationMapper.kt** - Narrativas já reconhecem perfis específicos
- ✅ **ConsumptionModelInput.kt** - Modelo de input OK

## Remoções Confirmadas
- ❌ `beverageRoutineScore` em ConsumptionMetrics
- ❌ `beverage_routine_score` em BuildConsumptionModelInputUseCase
- ❌ Referências em testes antigos

## Testes Executados
- ✅ BuildConsumptionModelInputUseCaseTest
- ✅ CalculateConsumptionMetricsUseCaseTest (com novos testes de bebida)
- ✅ HomeAnalysisPresentationMapperTest (com novo teste NON_ALCOHOLIC)
- ✅ CalculateConsumptionMetricsFinalUseCaseTest (beverageRoutineScore removido)

## Próximos Passos (Pós-Refatoração)

1. ✅ Executar suite completa de testes
2. ✅ Validar que backend retorna 10 classes de perfil
3. ✅ Verificar logs com "MODEL_REQUEST" e "MODEL_RESPONSE"
4. ⏳ Considerar deprecação de métricas internas não mais usadas (`MODEL_INTERNAL_METRICS_COUNT = 64`)

## Resumo de Impacto

**Positivo:**
- Clareza semântica aumentada (sem genéricos)
- Fortalecimento de separação alcoólica/não-alcoólica
- Logs mais informativos
- Validações mais estritas (15 features obrigatórias)
- Testes mais abrangentes

**Nenhum impacto negativo esperado:**
- Modelo continua recebendo as mesmas 15 features
- Perfis comportamentais mantêm integridade
- Narrativas de UI melhoram com prefis específicos


