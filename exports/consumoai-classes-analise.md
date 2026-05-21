# ConsumoAI — Documento de Análise de Classes para TCC

> **Finalidade:** Referência técnica e acadêmica para calibração semântica, calibração de interpretação, calibração comportamental e solidez do modelo.
> Gerado em: 2026-05-21

---

## Índice

1. [ConsumptionMetrics.kt](#1-consumptionmetricskt)
2. [CalculateConsumptionMetricsUseCase.kt — todos os cálculos](#2-calculateconsumptionmetricsusecasekt--todos-os-cálculos)
   - 2.1 [Frequency (frequência por categoria e por tag semântica)](#21-frequency-frequência-por-categoria-e-por-tag-semântica)
   - 2.2 [Co-occurrence (co-ocorrência)](#22-co-occurrence-co-ocorrência)
   - 2.3 [Score (scores comportamentais)](#23-score-scores-comportamentais)
   - 2.4 [Routine Scores (rotinas de comportamento)](#24-routine-scores-rotinas-de-comportamento)
   - 2.5 [Funções auxiliares matemáticas](#25-funções-auxiliares-matemáticas)
3. [BuildConsumptionModelInputUseCase.kt](#3-buildconsumptionmodelinputusecasekt)
   - 3.1 [Features oficiais do modelo (MODEL_FINAL_FEATURES)](#31-features-oficiais-do-modelo-model_final_features)
   - 3.2 [Código de montagem do vetor de features](#32-código-de-montagem-do-vetor-de-features)
4. [ConsumptionBehaviorProfile.kt](#4-consumptionbehaviorprofilekt)
5. [HomeAnalysisPresentationMapper.kt](#5-homeanalysispresentationmapperkt)
6. [KeywordProductSemanticTagger.kt](#6-keywordproductsemantictaggert)
7. [ImportSampleNfceReceiptsUseCase.kt](#7-importsamplenfcereceiptsusecasekt)
8. [Mapa semântico consolidado](#8-mapa-semântico-consolidado)
9. [Notas de calibração para TCC](#9-notas-de-calibração-para-tcc)

---

## 1. ConsumptionMetrics.kt

**Localização:** `domain/model/ConsumptionMetrics.kt`

Payload analítico completo que trafega entre o cálculo de métricas e todos os consumidores (UI, modelo, narrativa). Contém 64 métricas internas calculadas (`MODEL_INTERNAL_METRICS_COUNT = 64`), das quais **apenas 15 são enviadas ao modelo XGBoost final**.

```kotlin
package com.example.consumoai.domain.model

/**
 * Full analytics payload used by the app UI, debugging, and future experiments.
 * Nem todas as métricas calculadas fazem parte da entrada oficial do modelo.
 */
data class ConsumptionMetrics(
    // --- Métricas por categoria ---
    val valuePercentageByCategory: Map<ProductCategory, Double>,
    val itemPercentageByCategory: Map<ProductCategory, Double>,
    val frequencyByCategory: Map<ProductCategory, Double>,
    val categoryMetrics: Map<ProductCategory, CategoryMetrics>,

    val categoryValueTotals: Map<ProductCategory, Double>,
    val categoryItemTotals: Map<ProductCategory, Int>,

    // --- Métricas gerais ---
    val totalReceipts: Int,
    val totalItems: Int,
    val totalValue: Double,
    val averageTicket: Double,
    val averageItemsPerReceipt: Double,
    val maxCategoryByValue: ProductCategory?,
    val maxCategoryByItems: ProductCategory?,
    val receiptAverageValueByCategory: Map<ProductCategory, Double>,
    val categoryConcentrationIndex: Double,
    val topThreeCategoriesByValue: List<ProductCategory>,
    val averageValuePerItem: Double,
    val highestReceiptValue: Double,
    val lowestReceiptValue: Double,
    val receiptValueAmplitude: Double,
    val highValueReceiptsPercentage: Double,
    val lowValueReceiptsPercentage: Double,
    val categoryDominanceGap: Double,
    val topThreeCategoriesValuePercentage: Double,
    val otherPercentageByValue: Double,
    val otherPercentageByItems: Double,
    val classifiedItemsPercentage: Double,
    val averageCategoriesPerReceipt: Double,
    val categoryDiversityIndex: Double,

    // --- Métricas comportamentais ---
    val essentialCategoriesPercentage: Double,
    val nonEssentialCategoriesPercentage: Double,
    val industrializedToBasicFoodRatio: Double,
    val beveragesToBasicFoodRatio: Double,
    val beveragesToTotalRatio: Double,
    val produceToTotalRatio: Double,
    val receiptsWithIndustrializedPercentage: Double,
    val receiptsWithBeveragesPercentage: Double,
    val receiptsWithBasicFoodPercentage: Double,
    val receiptsWithProducePercentage: Double,
    val receiptsWithHygienePercentage: Double,
    val receiptsWithCleaningPercentage: Double,
    val averageIndustrializedItemsPerReceipt: Double,
    val averageBeveragesItemsPerReceipt: Double,
    val averageBasicFoodItemsPerReceipt: Double,
    val averageProduceItemsPerReceipt: Double,

    // --- Scores-base (reaproveitados em métricas e narrativa) ---
    val convenienceScore: Double,
    val essentialScore: Double,
    val diversityScore: Double,

    // === TEMPORAL / PADRÃO TEMPORAL ===
    val timeSpanDays: Double,
    val receiptsPerWeek: Double,
    val averageDaysBetweenReceipts: Double,
    val purchaseRegularityScore: Double,
    val ticketStandardDeviation: Double,
    val ticketVariationCoefficient: Double,
    val itemCountVariationCoefficient: Double,
    val highTicketReceiptsPercentage: Double,
    val lowTicketReceiptsPercentage: Double,
    val categoryStabilityScore: Double,
    val averageCategoryOverlapBetweenReceipts: Double,
    val recurringItemRatio: Double,
    val topItemRepetitionRate: Double,

    // === BEVERAGE-SPECIFIC METRICS ===
    /** Frequência de notas com bebidas não alcoólicas industrializadas */
    val softDrinkFrequency: Double,
    val softDrinkValuePct: Double,
    /** Frequência de notas com bebidas não alcoólicas (água, suco, refrigerante, energético e similares) */
    val nonAlcoholicBeverageFrequency: Double,
    val nonAlcoholicBeverageValuePct: Double,
    /** Frequência de notas com bebidas alcoólicas (cerveja, vinho, chopp, vodka, etc) */
    val alcoholicBeverageFrequency: Double,
    val alcoholicBeverageValuePct: Double,

    // --- Co-ocorrências (consumo combinado) ---
    /** Frequência de notas com QUALQUER bebida + snacks */
    val beverageSnackCoOccurrenceFrequency: Double,
    /** Frequência de notas com bebidas não alcoólicas + snacks */
    val nonAlcoholicBeverageSnackCoOccurrenceFrequency: Double,
    /** Frequência de notas com bebidas alcoólicas + snacks */
    val alcoholSnackCoOccurrenceFrequency: Double,

    val energyDrinkFrequency: Double,
    val energyDrinkValuePct: Double,
    val snackSweetFrequency: Double,
    val snackSweetValuePct: Double,

    // === CO-OCCURRENCE & LIFESTYLE PATTERNS ===
    val hygieneCleaningCoOccurrenceFrequency: Double,
    val basicProduceCoOccurrenceFrequency: Double,

    // === SPECIALIZED CATEGORY METRICS ===
    val frozenConvenienceValuePct: Double,
    val frozenConvenienceFrequency: Double,
    val dairyValuePct: Double,
    val meatProteinValuePct: Double,
    val freshProduceValuePct: Double,
    val convenienceMealValuePct: Double,
    val convenienceMealFrequency: Double,

    // === ROUTINE / BEHAVIORAL SCORES ===
    /** Frequência/consistência de itens essenciais nas compras */
    val essentialRoutineScore: Double,
    /** Frequência/consistência de itens de conveniência nas compras */
    val convenienceRoutineScore: Double,
    /** Frequência/consistência de compras com itens de limpeza/higiene */
    val householdRoutineScore: Double,
    /** Score de presença de alimentos frescos nas compras */
    val freshFoodPresenceScore: Double
)
```

---

## 2. CalculateConsumptionMetricsUseCase.kt — todos os cálculos

**Localização:** `domain/usecase/CalculateConsumptionMetricsUseCase.kt`

Este Use Case é o **núcleo matemático** do sistema. Ele lê a lista de `Receipt` e produz o `ConsumptionMetrics` completo.

---

### 2.1 Frequency (frequência por categoria e por tag semântica)

#### Frequência por categoria (base: notas fiscais)

> Dado: proporção de notas que contêm ao menos 1 item de determinada categoria.

```kotlin
val frequencyByCategory = ProductCategory.entries.associateWith { category ->
    val receiptsWithCategory = receipts.count { receipt ->
        receipt.items.any { it.category == category }
    }
    safeDivide(receiptsWithCategory.toDouble(), totalReceipts.toDouble())
}
```

**Interpretação acadêmica:**
- `frequencyByCategory[BEVERAGES]` = proporção de notas com bebida → uso imediato no perfil `NON_ALCOHOLIC_BEVERAGE_RECURRENT`
- `frequencyByCategory[BASIC_FOOD]` → componente do `essentialRoutineScore`
- `frequencyByCategory[PRODUCE]` → componente do `essentialRoutineScore` e `freshFoodPresenceScore`
- `frequencyByCategory[HYGIENE]` e `[CLEANING]` → componentes do `householdRoutineScore`

#### Frequência por tag semântica (via `ProductSemanticTagger`)

> Tags são atribuídas por `KeywordProductSemanticTagger` com base em keywords no nome do produto. A função auxiliar `freqByTag` retorna a proporção de notas que contêm ao menos 1 item com a tag alvo.

```kotlin
fun freqByTag(tag: ProductSemanticTag): Double {
    val withTag = receipts.count { receipt ->
        receipt.items.any { semanticTagger.tagsFor(it).contains(tag) }
    }
    return safeDivide(withTag.toDouble(), receipts.size.toDouble())
}

val alcoholicBeverageFrequency  = freqByTag(ProductSemanticTag.ALCOHOLIC_BEVERAGE)
val nonAlcoholicBeverageFrequency = freqByTag(ProductSemanticTag.NON_ALCOHOLIC_BEVERAGE)
val softDrinkFrequency          = freqByTag(ProductSemanticTag.SOFT_DRINK)
val energyDrinkFrequency        = freqByTag(ProductSemanticTag.ENERGY_DRINK)
val snackSweetFrequency         = freqByTag(ProductSemanticTag.SNACK_OR_SWEET)
val frozenConvenienceFrequency  = freqByTag(ProductSemanticTag.FROZEN_OR_READY_MEAL)
```

#### Percentual de valor por tag semântica

```kotlin
fun valuePctByTag(tag: ProductSemanticTag): Double {
    val value = receipts.flatMap { it.items }
        .filter { semanticTagger.tagsFor(it).contains(tag) }
        .sumOf { it.price }
    return safeDivide(value, totalValueForTags)
}

val alcoholicBeverageValuePct     = valuePctByTag(ProductSemanticTag.ALCOHOLIC_BEVERAGE)
val nonAlcoholicBeverageValuePct  = valuePctByTag(ProductSemanticTag.NON_ALCOHOLIC_BEVERAGE)
val softDrinkValuePct             = valuePctByTag(ProductSemanticTag.SOFT_DRINK)
val energyDrinkValuePct           = valuePctByTag(ProductSemanticTag.ENERGY_DRINK)
val snackSweetValuePct            = valuePctByTag(ProductSemanticTag.SNACK_OR_SWEET)
val frozenConvenienceValuePct     = valuePctByTag(ProductSemanticTag.FROZEN_OR_READY_MEAL)
val dairyValuePct                 = valuePctByTag(ProductSemanticTag.DAIRY)
val meatProteinValuePct           = valuePctByTag(ProductSemanticTag.MEAT_OR_PROTEIN)
val freshProduceValuePct          = valuePctByTag(ProductSemanticTag.FRESH_PRODUCE)
```

**Nota:** `nonAlcoholicBeverageValuePct` e `softDrinkValuePct` estão presentes no payload completo mas **não integram as 15 features oficiais do modelo**. São mantidas para análise/UI.

---

### 2.2 Co-occurrence (co-ocorrência)

> Co-ocorrência mede a frequência com que **dois grupos** de produtos aparecem **na mesma nota fiscal**.

#### Função genérica de co-ocorrência

```kotlin
private fun coOccurrence(
    receiptTagSets: List<Set<ProductSemanticTag>>,
    first: ProductSemanticTag,
    second: ProductSemanticTag
): Double {
    if (receiptTagSets.isEmpty()) return 0.0
    val withBoth = receiptTagSets.count { it.contains(first) && it.contains(second) }
    return safeDivide(withBoth.toDouble(), receiptTagSets.size.toDouble())
}
```

**Fórmula matemática:**

$$\text{coOccurrence}(A, B) = \frac{|\{r \in R : A \in r \land B \in r\}|}{|R|}$$

Onde $R$ é o conjunto de notas e $r$ é o conjunto de tags semânticas de uma nota.

#### Todas as co-ocorrências calculadas

```kotlin
// Pré-computação: conjunto de tags por nota
val receiptTagSets = receipts.map { receipt ->
    receipt.items.flatMap { semanticTagger.tagsFor(it) }.toSet()
}

// 1. Qualquer bebida (alcoólica ou não) + snack
val beverageSnackCoOccurrenceFrequency = safeDivide(
    receiptTagSets.count { tags ->
        (tags.contains(ProductSemanticTag.ALCOHOLIC_BEVERAGE) ||
            tags.contains(ProductSemanticTag.NON_ALCOHOLIC_BEVERAGE)) &&
            tags.contains(ProductSemanticTag.SNACK_OR_SWEET)
    }.toDouble(),
    receiptTagSets.size.toDouble()
)

// 2. Bebida alcoólica + snack
val alcoholSnackCoOccurrenceFrequency =
    coOccurrence(receiptTagSets, ProductSemanticTag.ALCOHOLIC_BEVERAGE, ProductSemanticTag.SNACK_OR_SWEET)

// 3. Bebida não alcoólica + snack
val nonAlcoholicBeverageSnackCoOccurrenceFrequency =
    coOccurrence(receiptTagSets, ProductSemanticTag.NON_ALCOHOLIC_BEVERAGE, ProductSemanticTag.SNACK_OR_SWEET)

// 4. Higiene pessoal + limpeza doméstica
val hygieneCleaningCoOccurrenceFrequency =
    coOccurrence(receiptTagSets, ProductSemanticTag.PERSONAL_CARE, ProductSemanticTag.HOUSEHOLD_CLEANING)

// 5. Alimento básico + hortifruti (baseado em categoria, não em tag)
val basicProduceCoOccurrenceFrequency = receipts.count { receipt ->
    val categories = receipt.items.map { it.category }.toSet()
    categories.contains(ProductCategory.BASIC_FOOD) && categories.contains(ProductCategory.PRODUCE)
}.toDouble() / receipts.size
```

**Tabela resumo das co-ocorrências:**

| Feature | Tags envolvidas | Uso no modelo |
|---|---|---|
| `beverage_snack_cooccurrence_frequency` | `(ALCOHOLIC_BEVERAGE ∨ NON_ALCOHOLIC_BEVERAGE) ∧ SNACK_OR_SWEET` | ❌ não está nas 15 |
| `alcohol_snack_cooccurrence_frequency` | `ALCOHOLIC_BEVERAGE ∧ SNACK_OR_SWEET` | ✅ feature #15 |
| `non_alcoholic_beverage_snack_cooccurrence_frequency` | `NON_ALCOHOLIC_BEVERAGE ∧ SNACK_OR_SWEET` | ✅ feature #4 |
| `hygiene_cleaning_cooccurrence_frequency` | `PERSONAL_CARE ∧ HOUSEHOLD_CLEANING` | ✅ feature #13 |
| `basic_produce_cooccurrence_frequency` | `BASIC_FOOD ∧ PRODUCE` (categoria) | ✅ feature #14 |

---

### 2.3 Score (scores comportamentais)

#### convenienceScore

> Mede a tendência de consumo conveniente/industrializado.

```kotlin
val convenienceScore = (
    (valuePercentageByCategory.valueOf(ProductCategory.INDUSTRIALIZED) +
        beveragesToTotalRatio +
        receiptsWithIndustrializedPercentage) / 3.0
).coerceIn(0.0, 1.0)
```

**Componentes:**
- `valuePercentageByCategory[INDUSTRIALIZED]` — peso financeiro em industrializados
- `beveragesToTotalRatio` — peso financeiro em bebidas
- `receiptsWithIndustrializedPercentage` — presença frequencial de industrializados

#### essentialScore

> Mede o alinhamento com consumo essencial e básico.

```kotlin
val essentialScore = (
    (essentialCategoriesPercentage +
        receiptsWithBasicFoodPercentage +
        receiptsWithProducePercentage) / 3.0
).coerceIn(0.0, 1.0)
```

**Componentes:**
- `essentialCategoriesPercentage` = `BASIC_FOOD + PRODUCE + HYGIENE + CLEANING` (% por valor)
- `receiptsWithBasicFoodPercentage` = frequência de notas com alimento básico
- `receiptsWithProducePercentage` = frequência de notas com hortifruti

#### diversityScore

> Mede a diversidade de categorias de consumo.

```kotlin
val diversityScore = (
    (categoryDiversityIndex +
        safeDivide(averageCategoriesPerReceipt, ProductCategory.entries.size.toDouble())) / 2.0
).coerceIn(0.0, 1.0)
```

**Componentes:**
- `categoryDiversityIndex` = proporção de categorias com valor > 0 em relação ao total de categorias
- `averageCategoriesPerReceipt / n_categorias` = diversidade média por nota

#### purchaseRegularityScore

> Mede a regularidade temporal das compras (quanto menor a variância nos intervalos, mais regular).

```kotlin
val gaps = sortedByDate.zipWithNext { a, b ->
    ChronoUnit.DAYS.between(a.date, b.date).toDouble().coerceAtLeast(0.0)
}
val averageDaysBetweenReceipts = gaps.averageOrZero()
val gapStdDev = standardDeviation(gaps)
val purchaseRegularityScore = (
    1.0 - safeDivide(gapStdDev, averageDaysBetweenReceipts.coerceAtLeast(1.0))
).coerceIn(0.0, 1.0)
```

**Fórmula:**

$$\text{regularityScore} = \left(1 - \frac{\sigma(\Delta t)}{\overline{\Delta t}}\right)_{[0,1]}$$

Ou seja, é o complemento do coeficiente de variação dos intervalos. Valor próximo de 1 = compras muito regulares.

#### categoryStabilityScore

> Mede a estabilidade de categorias entre compras consecutivas (similaridade de Jaccard).

```kotlin
val categorySets = receipts.map { receipt ->
    receipt.items.map { it.category }.toSet()
}
val overlaps = categorySets.zipWithNext { a, b -> jaccard(a, b) }
val averageCategoryOverlapBetweenReceipts = overlaps.averageOrZero()
val categoryStabilityScore = averageCategoryOverlapBetweenReceipts

private fun jaccard(a: Set<ProductCategory>, b: Set<ProductCategory>): Double {
    val union = (a + b).size.toDouble()
    if (union == 0.0) return 0.0
    val intersection = a.intersect(b).size.toDouble()
    return safeDivide(intersection, union)
}
```

**Fórmula (Jaccard):**

$$J(A, B) = \frac{|A \cap B|}{|A \cup B|}$$

#### categoryConcentrationIndex

> Máxima concentração de valor em uma única categoria (equivalente ao máximo de `valuePercentageByCategory`).

```kotlin
val categoryConcentrationIndex = valuePercentageByCategory.values.maxOrNull() ?: 0.0
```

#### categoryDominanceGap

> Diferença entre a categoria dominante e a segunda maior (por valor percentual). Mede quão destacada é a categoria principal.

```kotlin
val categoryDominanceGap = if (orderedValueCategories.isNotEmpty()) {
    val first  = valuePercentageByCategory.valueOf(orderedValueCategories.first())
    val second = orderedValueCategories.getOrNull(1)?.let { valuePercentageByCategory.valueOf(it) } ?: 0.0
    (first - second).coerceAtLeast(0.0)
} else 0.0
```

---

### 2.4 Routine Scores (rotinas de comportamento)

> Scores de rotina combinam frequência e co-ocorrência via média aritmética (`averageOf`). Medem consistência comportamental ao longo das compras, não apenas presença pontual.

```kotlin
private fun averageOf(vararg values: Double): Double {
    if (values.isEmpty()) return 0.0
    return values.map { if (it.isFinite()) it else 0.0 }.average().coerceIn(0.0, 1.0)
}
```

#### essentialRoutineScore ✅ feature #8

> Consistência de compras essenciais (alimento básico + hortifruti).

```kotlin
val essentialRoutineScore = averageOf(
    frequencyByCategory.valueOf(ProductCategory.BASIC_FOOD),   // freq. notas com alimento básico
    frequencyByCategory.valueOf(ProductCategory.PRODUCE),       // freq. notas com hortifruti
    essentialCategoriesPercentage,                              // % financeiro das categorias essenciais
    basicProduceCoOccurrenceFrequency                           // co-ocorrência básico + hortifruti
)
```

**Interpretação:** Score alto → consumidor compra frequentemente alimentos básicos e frescos de forma combinada e consistente.

#### convenienceRoutineScore

> Consistência de compras de conveniência (industrializados + snacks + congelados).

```kotlin
val convenienceRoutineScore = averageOf(
    frequencyByCategory.valueOf(ProductCategory.INDUSTRIALIZED), // freq. notas com industrializados
    snackSweetFrequency,                                         // freq. notas com snacks/doces
    frozenConvenienceFrequency,                                  // freq. notas com congelados
    convenienceMealValuePct                                      // % financeiro de refeições de conveniência
)
```

**Nota:** `convenienceMealValuePct = (frozenConvenienceValuePct + snackSweetValuePct) / 2.0`

#### householdRoutineScore ✅ feature #5

> Consistência de compras domésticas (higiene + limpeza combinadas).

```kotlin
val householdRoutineScore = averageOf(
    frequencyByCategory.valueOf(ProductCategory.HYGIENE),    // freq. notas com higiene
    frequencyByCategory.valueOf(ProductCategory.CLEANING),   // freq. notas com limpeza
    hygieneCleaningCoOccurrenceFrequency                     // co-ocorrência higiene + limpeza
)
```

**Interpretação:** Score alto → consumidor mantém rotina doméstica consistente, comprando higiene e limpeza frequentemente e juntos.

#### freshFoodPresenceScore

> Score de presença de alimentos frescos e naturais nas compras.

```kotlin
val freshFoodPresenceScore = averageOf(
    valuePercentageByCategory.valueOf(ProductCategory.PRODUCE), // % financeiro em hortifruti
    frequencyByCategory.valueOf(ProductCategory.PRODUCE),        // freq. notas com hortifruti
    freshProduceValuePct,                                        // % financeiro da tag FRESH_PRODUCE
    basicProduceCoOccurrenceFrequency                            // co-ocorrência básico + hortifruti
)
```

**Interpretação:** Score baixo → perfil `LOW_FRESH_FOOD`.

---

### 2.5 Funções auxiliares matemáticas

```kotlin
// Divisão segura: evita NaN/Inf por divisão por zero
private fun safeDivide(numerator: Double, denominator: Double): Double =
    if (denominator == 0.0) 0.0 else numerator / denominator

// Desvio padrão populacional
private fun standardDeviation(values: List<Double>): Double {
    if (values.isEmpty()) return 0.0
    val mean = values.average()
    val variance = values.sumOf { (it - mean).pow(2) } / values.size
    return sqrt(abs(variance))
}

// Média protegida contra lista vazia
private fun List<Double>.averageOrZero(): Double =
    if (isEmpty()) 0.0 else average()

// Média com clamp [0, 1] e proteção contra Inf/NaN
private fun averageOf(vararg values: Double): Double {
    if (values.isEmpty()) return 0.0
    return values.map { if (it.isFinite()) it else 0.0 }.average().coerceIn(0.0, 1.0)
}

// Similaridade de Jaccard entre conjuntos de categorias
private fun jaccard(a: Set<ProductCategory>, b: Set<ProductCategory>): Double {
    val union = (a + b).size.toDouble()
    if (union == 0.0) return 0.0
    val intersection = a.intersect(b).size.toDouble()
    return safeDivide(intersection, union)
}
```

---

## 3. BuildConsumptionModelInputUseCase.kt

**Localização:** `domain/usecase/BuildConsumptionModelInputUseCase.kt`

Este Use Case **filtra e ordena** as features do `ConsumptionMetrics` para montar o vetor de entrada do modelo XGBoost. Garante ordem estrita e proteção contra `NaN`/`Infinity`.

### 3.1 Features oficiais do modelo (MODEL_FINAL_FEATURES)

**Localização:** `domain/model/ModelFeatureConstants.kt`

```
Modelo: XGBoost Beverage Split Top15
Backend: consumoai_xgboost_beverage_split_top15.pkl
Label Encoder: consumoai_label_encoder_final.pkl
Total de features: 15
Total de classes: 10
```

| # | Feature | Grupo | Fórmula/Origem |
|---|---|---|---|
| 1 | `non_alcoholic_beverage_frequency` | Frequency/Tag | `freqByTag(NON_ALCOHOLIC_BEVERAGE)` |
| 2 | `category_concentration_index` | Score/Concentração | `max(valuePercentageByCategory)` |
| 3 | `classified_items_percentage` | Qualidade | `1.0 - itemPct[OTHER]` |
| 4 | `non_alcoholic_beverage_snack_cooccurrence_frequency` | Co-occurrence | `coOccurrence(NON_ALCOHOLIC_BEVERAGE, SNACK_OR_SWEET)` |
| 5 | `household_routine_score` | Routine | `avg(freq[HYGIENE], freq[CLEANING], hygieneCleaningCoOcc)` |
| 6 | `alcoholic_beverage_frequency` | Frequency/Tag | `freqByTag(ALCOHOLIC_BEVERAGE)` |
| 7 | `produce_frequency` | Frequency/Categoria | `frequencyByCategory[PRODUCE]` |
| 8 | `essential_routine_score` | Routine | `avg(freq[BASIC_FOOD], freq[PRODUCE], essentialPct, basicProduceCoOcc)` |
| 9 | `category_dominance_gap` | Score/Estrutura | `valuePct[#1] - valuePct[#2]` |
| 10 | `category_stability_score` | Score/Temporal | `avg(jaccard(A,B) das notas consecutivas)` |
| 11 | `essential_score` | Score | `avg(essentialPct, freq[BASIC_FOOD], freq[PRODUCE])` |
| 12 | `other_value_pct` | Qualidade | `valuePercentageByCategory[OTHER]` |
| 13 | `hygiene_cleaning_cooccurrence_frequency` | Co-occurrence | `coOccurrence(PERSONAL_CARE, HOUSEHOLD_CLEANING)` |
| 14 | `basic_produce_cooccurrence_frequency` | Co-occurrence | `receipts com BASIC_FOOD ∧ PRODUCE / totalReceipts` |
| 15 | `alcohol_snack_cooccurrence_frequency` | Co-occurrence | `coOccurrence(ALCOHOLIC_BEVERAGE, SNACK_OR_SWEET)` |

### 3.2 Código de montagem do vetor de features

```kotlin
class BuildConsumptionModelInputUseCase {

    operator fun invoke(metrics: ConsumptionMetrics): ConsumptionModelInput {
        val valuePercentages = metrics.valuePercentageByCategory
        val frequencies = metrics.frequencyByCategory

        // Mapeamento completo de TODAS as métricas do sistema (não apenas as 15 do modelo)
        val allFeatures = linkedMapOf(
            "total_receipts"                    to metrics.totalReceipts.toDouble(),
            "total_items"                       to metrics.totalItems.toDouble(),
            "total_value"                       to metrics.totalValue,
            "average_ticket"                    to metrics.averageTicket,
            "average_items_per_receipt"         to metrics.averageItemsPerReceipt,
            "basic_food_value_pct"              to valuePercentages[ProductCategory.BASIC_FOOD].orZero(),
            "industrialized_value_pct"          to valuePercentages[ProductCategory.INDUSTRIALIZED].orZero(),
            "beverages_value_pct"               to valuePercentages[ProductCategory.BEVERAGES].orZero(),
            "hygiene_value_pct"                 to valuePercentages[ProductCategory.HYGIENE].orZero(),
            "cleaning_value_pct"                to valuePercentages[ProductCategory.CLEANING].orZero(),
            "produce_value_pct"                 to valuePercentages[ProductCategory.PRODUCE].orZero(),
            "other_value_pct"                   to valuePercentages[ProductCategory.OTHER].orZero(),
            "basic_food_frequency"              to frequencies[ProductCategory.BASIC_FOOD].orZero(),
            "industrialized_frequency"          to frequencies[ProductCategory.INDUSTRIALIZED].orZero(),
            "beverages_frequency"               to frequencies[ProductCategory.BEVERAGES].orZero(),
            "produce_frequency"                 to frequencies[ProductCategory.PRODUCE].orZero(),
            "hygiene_frequency"                 to frequencies[ProductCategory.HYGIENE].orZero(),
            "cleaning_frequency"                to frequencies[ProductCategory.CLEANING].orZero(),
            "classified_items_percentage"       to metrics.classifiedItemsPercentage,
            "category_concentration_index"      to metrics.categoryConcentrationIndex,
            "category_dominance_gap"            to metrics.categoryDominanceGap,
            "category_diversity_index"          to metrics.categoryDiversityIndex,
            "essential_categories_percentage"   to metrics.essentialCategoriesPercentage,
            "non_essential_categories_percentage" to metrics.nonEssentialCategoriesPercentage,
            "essential_score"                   to metrics.essentialScore,
            "convenience_score"                 to metrics.convenienceScore,
            "diversity_score"                   to metrics.diversityScore,
            "time_span_days"                    to metrics.timeSpanDays,
            "receipts_per_week"                 to metrics.receiptsPerWeek,
            "average_days_between_receipts"     to metrics.averageDaysBetweenReceipts,
            "purchase_regularity_score"         to metrics.purchaseRegularityScore,
            "ticket_standard_deviation"         to metrics.ticketStandardDeviation,
            "ticket_variation_coefficient"      to metrics.ticketVariationCoefficient,
            "item_count_variation_coefficient"  to metrics.itemCountVariationCoefficient,
            "high_ticket_receipts_percentage"   to metrics.highTicketReceiptsPercentage,
            "low_ticket_receipts_percentage"    to metrics.lowTicketReceiptsPercentage,
            "category_stability_score"          to metrics.categoryStabilityScore,
            "average_category_overlap_between_receipts" to metrics.averageCategoryOverlapBetweenReceipts,
            "recurring_item_ratio"              to metrics.recurringItemRatio,
            "top_item_repetition_rate"          to metrics.topItemRepetitionRate,
            "beverage_snack_cooccurrence_frequency" to metrics.beverageSnackCoOccurrenceFrequency,
            "alcohol_snack_cooccurrence_frequency" to metrics.alcoholSnackCoOccurrenceFrequency,
            "hygiene_cleaning_cooccurrence_frequency" to metrics.hygieneCleaningCoOccurrenceFrequency,
            "basic_produce_cooccurrence_frequency" to metrics.basicProduceCoOccurrenceFrequency,
            "alcoholic_beverage_value_pct"      to metrics.alcoholicBeverageValuePct,
            "alcoholic_beverage_frequency"      to metrics.alcoholicBeverageFrequency,
            "non_alcoholic_beverage_value_pct"  to metrics.nonAlcoholicBeverageValuePct,
            "non_alcoholic_beverage_frequency"  to metrics.nonAlcoholicBeverageFrequency,
            "soft_drink_value_pct"              to metrics.softDrinkValuePct,
            "soft_drink_frequency"              to metrics.softDrinkFrequency,
            "energy_drink_value_pct"            to metrics.energyDrinkValuePct,
            "energy_drink_frequency"            to metrics.energyDrinkFrequency,
            "snack_sweet_value_pct"             to metrics.snackSweetValuePct,
            "snack_sweet_frequency"             to metrics.snackSweetFrequency,
            "frozen_convenience_value_pct"      to metrics.frozenConvenienceValuePct,
            "frozen_convenience_frequency"      to metrics.frozenConvenienceFrequency,
            "dairy_value_pct"                   to metrics.dairyValuePct,
            "meat_protein_value_pct"            to metrics.meatProteinValuePct,
            "fresh_produce_value_pct"           to metrics.freshProduceValuePct,
            "convenience_meal_value_pct"        to metrics.convenienceMealValuePct,
            "convenience_meal_frequency"        to metrics.convenienceMealFrequency,
            "essential_routine_score"           to metrics.essentialRoutineScore,
            "convenience_routine_score"         to metrics.convenienceRoutineScore,
            "non_alcoholic_beverage_snack_cooccurrence_frequency" to metrics.nonAlcoholicBeverageSnackCoOccurrenceFrequency,
            "household_routine_score"           to metrics.householdRoutineScore,
            "fresh_food_presence_score"         to metrics.freshFoodPresenceScore
        )

        // Filtra apenas as 15 features oficiais, na ordem exata
        val selectedFeatures = linkedMapOf<String, Double>()
        MODEL_FINAL_FEATURES.forEach { feature ->
            val value = allFeatures[feature]
                ?: error("Feature obrigatoria ausente: $feature")
            selectedFeatures[feature] = if (value.isNaN() || value.isInfinite()) 0.0 else value
        }

        check(selectedFeatures.size == MODEL_FEATURE_COUNT) {
            "Quantidade de features invalida para o modelo final: ${selectedFeatures.size}"
        }

        return ConsumptionModelInput(
            version = MODEL_INPUT_VERSION,
            features = selectedFeatures
        )
    }
}
```

---

## 4. ConsumptionBehaviorProfile.kt

**Localização:** `domain/model/ConsumptionBehaviorProfile.kt`

Enum com os 10 perfis comportamentais possíveis na saída do classificador.

```kotlin
enum class ConsumptionBehaviorProfile {
    // Perfil com maior peso de praticidade, industrializados e compras rápidas.
    CONVENIENCE_ORIENTED,
    // Perfil com predominância de itens essenciais e alimentação básica.
    ESSENTIAL_FOCUSED,
    // Perfil com distribuição mais equilibrada entre diferentes categorias de consumo.
    DIVERSIFIED_BALANCED,
    // Bebidas não alcoólicas aparecem com alta recorrência.
    NON_ALCOHOLIC_BEVERAGE_RECURRENT,
    // Bebidas alcoólicas aparecem com recorrência relevante.
    ALCOHOLIC_BEVERAGE_RECURRENT,
    // Baixa presença de hortifruti e alimentos frescos.
    LOW_FRESH_FOOD,
    // Destaque para higiene, limpeza e manutenção da casa.
    HOUSEHOLD_MAINTENANCE,
    // Gasto concentrado em poucas categorias dominantes.
    HIGHLY_CONCENTRATED,
    // Maior presença de compras não essenciais e sinais de impulso.
    IMPULSIVE_CONSUMPTION,
    // Não há confiança suficiente para definir um padrão claro.
    UNDEFINED
}
```

**Mapeamento de perfis para nomes e descrições de apresentação:**

| Enum | Nome de Exibição | Descrição |
|---|---|---|
| `CONVENIENCE_ORIENTED` | Orientado à conveniência | Maior presença de produtos industrializados e compras voltadas à praticidade. |
| `ESSENTIAL_FOCUSED` | Focado no essencial | Predominância de itens essenciais e alimentação básica nas compras analisadas. |
| `DIVERSIFIED_BALANCED` | Diversificado e equilibrado | Distribuição relativamente equilibrada entre diferentes categorias de consumo. |
| `NON_ALCOHOLIC_BEVERAGE_RECURRENT` | Recorrência de bebidas não alcoólicas | Bebidas não alcoólicas aparecem com recorrência relevante nas notas analisadas. |
| `ALCOHOLIC_BEVERAGE_RECURRENT` | Recorrência de bebidas alcoólicas | Bebidas alcoólicas aparecem com recorrência relevante nas notas analisadas. |
| `LOW_FRESH_FOOD` | Baixa presença de hortifruti | Baixa participação de hortifruti e alimentos frescos no consumo analisado. |
| `HOUSEHOLD_MAINTENANCE` | Foco em manutenção doméstica | Maior presença de produtos de higiene e limpeza doméstica. |
| `HIGHLY_CONCENTRATED` | Consumo concentrado | Grande parte do consumo está concentrada em poucas categorias. |
| `IMPULSIVE_CONSUMPTION` | Consumo impulsivo | Maior presença de categorias não essenciais e compras de conveniência. |
| `UNDEFINED` | Indefinido | Não foi possível identificar um padrão confiável com os dados atuais. |

---

## 5. HomeAnalysisPresentationMapper.kt

**Localização:** `presentation/home/model/HomeAnalysisPresentationMapper.kt`

Mapper de `StoredConsumptionAnalysis` → `HomeAnalysisPresentation`. Controla toda a lógica de **interpretação narrativa** dos perfis para o usuário final.

### Leitura comportamental por perfil (`buildBehavioralReading`)

```kotlin
private fun buildBehavioralReading(analysis: StoredConsumptionAnalysis): String {
    val metrics = analysis.metrics
    val mainProfile = analysis.behaviorResult.mainProfile
    val alcoholicProfileScore = analysis.behaviorResult.profileScores[
        ConsumptionBehaviorProfile.ALCOHOLIC_BEVERAGE_RECURRENT
    ].orZero()

    val beverageFrequency       = metrics.frequencyByCategory[ProductCategory.BEVERAGES].orZero().toPercentageText()
    val essentialValue          = metrics.valuePercentageByCategory[ProductCategory.BASIC_FOOD].orZero().toPercentageText()
    val beverageSnackFrequency  = metrics.beverageSnackCoOccurrenceFrequency.toPercentageText()
    val alcoholSnackFrequency   = metrics.alcoholSnackCoOccurrenceFrequency.toPercentageText()
    val alcoholFrequency        = metrics.alcoholicBeverageFrequency.toPercentageText()
    val softDrinkFrequency      = metrics.softDrinkFrequency.toPercentageText()

    return buildString {
        when (mainProfile) {
            ALCOHOLIC_BEVERAGE_RECURRENT -> {
                // Narrativa: presença alcoólica recorrente + co-ocorrência com snacks
                append("As compras analisadas mostram presença recorrente de bebidas alcoólicas")
                append(" ($alcoholFrequency das notas)")
                append(", com combinação frequente com snacks em $alcoholSnackFrequency")
                append(" e contexto ampliado de bebidas + snacks em $beverageSnackFrequency.")
                append("\n\nApesar disso, alimentação básica continua presente em valor ($essentialValue), ")
                append("indicando que o consumo alcoólico coexiste com compras de rotina.")
            }
            NON_ALCOHOLIC_BEVERAGE_RECURRENT -> {
                // Narrativa: bebidas não alcoólicas recorrentes + verificação de sinal secundário alcoólico
                val alcoholicPatternIsCompetitive = alcoholicProfileScore >= 0.25
                // [detalhes de narrativa adaptativa omitidos para brevidade]
            }
            else -> {
                // Narrativa genérica: padrão variado com presença de bebidas
            }
        }
    }
}
```

### Sinais primários por perfil (`buildPrimarySignals`)

```kotlin
private fun buildPrimarySignals(analysis: StoredConsumptionAnalysis): List<String> {
    return buildList {
        when (mainProfile) {
            ALCOHOLIC_BEVERAGE_RECURRENT -> {
                add("Bebidas alcoólicas presentes em ${metrics.alcoholicBeverageFrequency.toPercentageText()} das notas")
                add("Bebidas alcoólicas + snacks em ${metrics.alcoholSnackCoOccurrenceFrequency.toPercentageText()} das notas")
                add("Bebidas + snacks em ${metrics.beverageSnackCoOccurrenceFrequency.toPercentageText()} das notas")
            }
            NON_ALCOHOLIC_BEVERAGE_RECURRENT -> {
                add("Bebidas presentes em ${metrics.frequencyByCategory[BEVERAGES].orZero().toPercentageText()} das notas")
                add("Bebidas + snacks em ${metrics.beverageSnackCoOccurrenceFrequency.toPercentageText()} das notas")
                if (metrics.softDrinkFrequency >= 0.20) {
                    add("Refrigerantes presentes em ${metrics.softDrinkFrequency.toPercentageText()} das notas")
                }
            }
            else -> {
                if (metrics.alcoholicBeverageFrequency >= 0.30) {
                    add("Bebidas alcoólicas presentes em ${metrics.alcoholicBeverageFrequency.toPercentageText()} das notas")
                }
                add("Bebidas presentes em ${metrics.frequencyByCategory[BEVERAGES].orZero().toPercentageText()} das notas")
                add("Bebidas + snacks em ${metrics.beverageSnackCoOccurrenceFrequency.toPercentageText()} das notas")
                if (metrics.softDrinkFrequency >= 0.20) {
                    add("Refrigerantes presentes em ${metrics.softDrinkFrequency.toPercentageText()} das notas")
                }
            }
        }
        add("Alimentação básica representa ${metrics.valuePercentageByCategory[BASIC_FOOD].orZero().toPercentageText()} do valor")
        add("Recorrência de itens em ${metrics.recurringItemRatio.toPercentageText()} das compras")
    }.take(5)
}
```

### Labels de confiança

```kotlin
private fun Double.toConfidenceLabel(): String = when {
    this >= 0.85 -> "Padrão de consumo muito consistente"
    this >= 0.70 -> "Padrão de consumo consistente"
    this >= 0.50 -> "Padrão de consumo parcialmente consistente"
    else         -> "Padrão de consumo variado"
}
```

---

## 6. KeywordProductSemanticTagger.kt

**Localização:** `data/classifier/KeywordProductSemanticTagger.kt`

Tagger semântico baseado em keywords. Atribui `Set<ProductSemanticTag>` a cada `ProductItem` com base no nome normalizado (uppercase, sem acentos, sem pontuação).

### Normalização

```kotlin
private fun normalize(value: String): String {
    val uppercase  = value.uppercase(Locale.ROOT)
    val noAccents  = Normalizer.normalize(uppercase, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
    return noAccents.replace(Regex("[^A-Z0-9 ]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}
```

### Tags e keywords por categoria semântica

| Tag | Keywords |
|---|---|
| `ALCOHOLIC_BEVERAGE` | CERVEJA, CERV, CHOPP, IPA, LAGER, PILSEN, PILSNER, PALE ALE, AMBER ALE, STOUT, VINHO, ESPUMANTE, ROSE, WHISKY, WHISKEY, VODKA, GIN, CACHACA, CAIPIRINHA, SKOL, BRAHMA, ANTARCTICA, HEINEKEN, STELLA, CORONA, BUDWEISER, GUINNESS, SPATEN, BECK, BADEN BADEN, BLUE MOON, KAISERDOM, TUPINIQUIM, AURORA, CONCHA Y TORO |
| `SOFT_DRINK` | COCA, GUARANA, REFRIGERANTE, REFRI, FANTA, SPRITE, PEPSI, SUKITA, DOLLY |
| `ENERGY_DRINK` | MONSTER, ENERGETICO, ENERG, RED BULL, REDBULL, FLASH POWER, TNT ENERGY |
| `JUICE` | SUCO, NECTAR, DEL VALLE, NATURALE, SUFRESH, JOTA BE |
| `SNACK_OR_SWEET` | DORITOS, FANDANGOS, TRENTO, BALA, SALG, CHOC, HALLS, PAÇOCA, PACOCA, BISCOITO, BOLACHA |
| `FROZEN_OR_READY_MEAL` | PIZZA, LASANHA, NISSIN, MIOJO, LAMEN, MACARRAO INST, HOT POCKET |
| `DAIRY` | LEITE, QUEIJO, QJO, REQUEIJAO, IOGURTE, IOG, MANTEIGA, RICOTA, CREME DE LEITE |
| `MEAT_OR_PROTEIN` | PATINHO, COXAO, FRANGO, CARNE, PRESUNTO, MORTADELA, LOMBO, FILE, PEIXE, ATUM, SARDINHA, OVO |
| `FRESH_PRODUCE` | MELANCIA, PIMENTAO, ALHO, CEBOLINHA, TOMATE, BANANA, MACA, MORANGO, ALFACE, CENOURA, BROCOLIS, LARANJA, LIMAO |
| `PERSONAL_CARE` | SHAMPOO, DOVE, REXONA, COLGATE, SABONETE, DESODORANTE, CONDICIONADOR, CREME DENTAL, FRALDA |
| `HOUSEHOLD_CLEANING` | OMO, AJAX, DET LQ, DESINF, DETERGENTE, AMACIANTE, PINHO SOL, FLASH LIMP, VEJA, CLOROX, DOMESTOS |
| `PET` | PEDIGREE, PETHAND, RACAO, ALIM CAO, WHISKAS, PREMIER PET |
| `UTILITY` | CANECA, FITA, CADERNO, PULVERIZ, CAD |

### Tag inferida: NON_ALCOHOLIC_BEVERAGE

```kotlin
// Regra de inferência: tag derivada de SOFT_DRINK, ENERGY_DRINK ou JUICE,
// apenas se DAIRY não estiver presente (evita que "LEITE" seja classificado como bebida não alcoólica).
if (!tags.contains(ProductSemanticTag.DAIRY) &&
    (tags.contains(ProductSemanticTag.SOFT_DRINK) ||
        tags.contains(ProductSemanticTag.ENERGY_DRINK) ||
        tags.contains(ProductSemanticTag.JUICE))
) {
    tags += ProductSemanticTag.NON_ALCOHOLIC_BEVERAGE
}
```

### Fallback por categoria

Quando nenhuma keyword bate, o item recebe uma tag-padrão baseada na sua categoria NF-e:

```kotlin
private fun fallbackTagFromCategory(category: ProductCategory): ProductSemanticTag = when (category) {
    ProductCategory.BEVERAGES    -> ProductSemanticTag.NON_ALCOHOLIC_BEVERAGE
    ProductCategory.INDUSTRIALIZED -> ProductSemanticTag.SNACK_OR_SWEET
    ProductCategory.BASIC_FOOD   -> ProductSemanticTag.MEAT_OR_PROTEIN
    ProductCategory.PRODUCE      -> ProductSemanticTag.FRESH_PRODUCE
    ProductCategory.HYGIENE      -> ProductSemanticTag.PERSONAL_CARE
    ProductCategory.CLEANING     -> ProductSemanticTag.HOUSEHOLD_CLEANING
    ProductCategory.OTHER        -> ProductSemanticTag.UNKNOWN
}
```

---

## 7. ImportSampleNfceReceiptsUseCase.kt

**Localização:** `domain/usecase/ImportSampleNfceReceiptsUseCase.kt`

Use Case de importação de amostra de notas NFC-e reais do RS (SEFAZ/SVRS) para fins de validação do modelo e calibração do sistema.

```kotlin
class ImportSampleNfceReceiptsUseCase(
    private val analyzeReceiptFromQrCodeUrlUseCase: AnalyzeReceiptFromQrCodeUrlUseCase,
    private val saveReceiptUseCase: SaveReceiptUseCase,
    private val receiptRepository: ReceiptRepository
) {
    suspend operator fun invoke(): ImportReceiptsResult {
        var importedCount = 0
        var skippedCount  = 0
        var failedCount   = 0

        sampleNfceUrls.forEach { url ->
            // Proteção de duplicatas por access key ou URL
            if (receiptRepository.existsByAccessKeyOrUrl(url)) {
                skippedCount += 1
                return@forEach
            }

            val receipt = runCatching {
                analyzeReceiptFromQrCodeUrlUseCase(url)
            }.getOrElse {
                failedCount += 1
                return@forEach
            }

            // Nota inválida (sem itens): descartada com falha
            if (receipt.items.isEmpty()) {
                failedCount += 1
                return@forEach
            }

            saveReceiptUseCase(receipt)
            importedCount += 1
        }

        return ImportReceiptsResult(importedCount, skippedCount, failedCount)
    }

    companion object {
        // 35 URLs de NFC-e reais (SVRS + SEFAZ-RS) para amostragem
        val sampleNfceUrls = listOf( /* 35 URLs */ )
    }
}
```

**Fontes das notas amostra:**
- `https://dfe-portal.svrs.rs.gov.br/Dfe/QrCodeNFce` — Portal SVRS (NFC-e eletrônica)
- `https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx` — Portal SEFAZ-RS legado

**Total de notas amostrais:** 35 URLs únicas, cobrindo múltiplos estabelecimentos e períodos de 2025–2026.

---

## 8. Mapa semântico consolidado

### Pipeline de dados: Nota Fiscal → Perfil

```
NFC-e (QR Code URL)
        │
        ▼
AnalyzeReceiptFromQrCodeUrlUseCase
  (scraping/parsing da nota)
        │
        ▼
Receipt { items: List<ProductItem> }
  ProductItem { name, category, price }
        │
        ▼
CalculateConsumptionMetricsUseCase
  ├─ frequency por categoria (receipts que contêm a categoria)
  ├─ valuePct por categoria (R$ / totalValue)
  ├─ freqByTag + valuePctByTag (via KeywordProductSemanticTagger)
  ├─ co-occurrence (pares de tags/categorias na mesma nota)
  ├─ scores comportamentais (convenienceScore, essentialScore, diversityScore)
  ├─ scores temporais (purchaseRegularityScore, categoryStabilityScore)
  └─ routine scores (essentialRoutineScore, householdRoutineScore, etc.)
        │
        ▼
ConsumptionMetrics (64 métricas internas)
        │
        ▼
BuildConsumptionModelInputUseCase
  (filtra → 15 features oficiais em ordem estrita)
        │
        ▼
ConsumptionModelInput { version, features: Map<String, Double> }
        │
        ▼
XGBoost Backend (consumoai_xgboost_beverage_split_top15.pkl)
  → profileScores: Map<ConsumptionBehaviorProfile, Double>
  → mainProfile: ConsumptionBehaviorProfile
  → confidence: Double
        │
        ▼
HomeAnalysisPresentationMapper
  (narrativa + sinais primários + dados técnicos)
        │
        ▼
HomeAnalysisPresentation (UI)
```

### Dependências semânticas das features oficiais do modelo

```
non_alcoholic_beverage_frequency          ← freqByTag(NON_ALCOHOLIC_BEVERAGE)
                                              ← tagged por: SOFT_DRINK ∨ ENERGY_DRINK ∨ JUICE (sem DAIRY)

non_alcoholic_beverage_snack_cooccurrence_frequency
                                          ← coOccurrence(NON_ALCOHOLIC_BEVERAGE, SNACK_OR_SWEET)

alcohol_snack_cooccurrence_frequency      ← coOccurrence(ALCOHOLIC_BEVERAGE, SNACK_OR_SWEET)

hygiene_cleaning_cooccurrence_frequency   ← coOccurrence(PERSONAL_CARE, HOUSEHOLD_CLEANING)

basic_produce_cooccurrence_frequency      ← receipts com BASIC_FOOD ∧ PRODUCE / total

household_routine_score                   ← avg(freq[HYGIENE], freq[CLEANING], hygieneCleaningCoOcc)

essential_routine_score                   ← avg(freq[BASIC_FOOD], freq[PRODUCE],
                                               essentialCategoriesPercentage,
                                               basicProduceCoOccurrenceFrequency)

essential_score                           ← avg(essentialCategoriesPercentage,
                                               freq[BASIC_FOOD], freq[PRODUCE])

category_concentration_index              ← max(valuePercentageByCategory.values)

category_dominance_gap                    ← valuePct[rank1] - valuePct[rank2]

category_stability_score                  ← avg(jaccard(categorias_nota_i, categorias_nota_i+1))

classified_items_percentage               ← 1.0 - itemPercentageByCategory[OTHER]

other_value_pct                           ← valuePercentageByCategory[OTHER]

alcoholic_beverage_frequency              ← freqByTag(ALCOHOLIC_BEVERAGE)

produce_frequency                         ← frequencyByCategory[PRODUCE]
```

---

## 9. Notas de calibração para TCC

### 9.1 Co-ocorrência: decisões de design

- **`beverageSnackCoOccurrenceFrequency`** usa OR lógico (qualquer bebida) para ser mais abrangente na UI/narrativa, porém **não está nas 15 features oficiais** — foi descartada na seleção pelo modelo XGBoost por redundância com as versões mais específicas.
- **`basicProduceCoOccurrenceFrequency`** é a única co-ocorrência calculada via **categoria** (e não via tag semântica), pois `BASIC_FOOD` e `PRODUCE` são categorias NF-e diretas, já confiáveis sem necessidade de tagger.

### 9.2 Frequência: distinção semântica vs. categórica

O sistema mantém duas camadas de frequência:
1. **Por categoria NF-e** (`frequencyByCategory`) — mais estável, depende da atribuição da categoria na nota fiscal.
2. **Por tag semântica** (`freqByTag`) — mais granular, depende do `KeywordProductSemanticTagger`. Permite distinguir `ALCOHOLIC_BEVERAGE` de `NON_ALCOHOLIC_BEVERAGE` dentro da mesma categoria `BEVERAGES`, o que é impossível apenas pela categoria NF-e.

### 9.3 Routine scores: justificativa da média simples

Os routine scores usam `averageOf()` (média aritmética simples com clamp `[0,1]`). Para fins acadêmicos, alternativas a considerar:
- **Média ponderada** — dar peso maior à frequência direta do que à co-ocorrência.
- **Score produto** — `freq_A * freq_B` como proxy de independência condicional.
- **Correlação de Pearson** entre listas binárias por nota — mais robusto estatisticamente, porém caro computacionalmente para execução no dispositivo.

A média simples foi escolhida deliberadamente por: (a) execução eficiente no Android, (b) interpretabilidade direta para TCC, (c) compatibilidade com o vetor de features já validado no backend.

### 9.4 Classificação semântica: limitações conhecidas

- Produtos sem match de keyword caem no `fallbackTagFromCategory`, o que pode distorcer métricas de tags para categorias amplas como `INDUSTRIALIZED` (mapeada para `SNACK_OR_SWEET`).
- Produtos genéricos na categoria `OTHER` não recebem tag semântica útil (`UNKNOWN`), o que aumenta o ruído em `classified_items_percentage`.
- Marcas novas ou regionais não cobertas pelas keywords do tagger são classificadas pelo fallback.

### 9.5 Interpretação do modelo: ProfileInterpretationType

```
PURE_PROFILE         → Perfil único com confiança suficiente
HYBRID_PROFILE       → Dois ou mais perfis com scores próximos
LOW_CONFIDENCE_PROFILE → Confiança abaixo do limiar
```

A narrativa apresentada ao usuário é adaptada a cada tipo de interpretação via `buildBehavioralReading` e `buildPrimarySignals`, considerando sinais secundários (ex.: `alcoholicProfileScore >= 0.25` mesmo em perfil primário `NON_ALCOHOLIC_BEVERAGE_RECURRENT`).

