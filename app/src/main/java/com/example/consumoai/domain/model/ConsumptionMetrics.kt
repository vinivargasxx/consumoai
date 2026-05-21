package com.example.consumoai.domain.model

/**
 * Full analytics payload used by the app UI, debugging, and future experiments.
 * Nem todas as métricas calculadas fazem parte da entrada oficial do modelo.
 */
data class ConsumptionMetrics(
    // Métricas por categoria
    val valuePercentageByCategory: Map<ProductCategory, Double>,
    val itemPercentageByCategory: Map<ProductCategory, Double>,
    val frequencyByCategory: Map<ProductCategory, Double>,
    val categoryMetrics: Map<ProductCategory, CategoryMetrics>,

    val categoryValueTotals: Map<ProductCategory, Double>,
    val categoryItemTotals: Map<ProductCategory, Int>,

    // Métricas gerais
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

    // Métricas comportamentais
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

    // Scores-base reaproveitados nas métricas e narrativa do fluxo final.
    val convenienceScore: Double,
    val essentialScore: Double,
    val diversityScore: Double

    // === TEMPORAL/TEMPORAL PATTERN ===
    // Métricas de distribuição temporal e recorrência
    , val timeSpanDays: Double,
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
    // Frequência de bebidas: notas que possuem QUALQUER tipo de bebida (genérica BEVERAGES)
    // Nota: beverages_frequency já está em frequencyByCategory[ProductCategory.BEVERAGES]
    //
    // Frequência de refr igerantes: notas que possuem bebidas não alcoólicas industrializadas
    /** Frequência de notas com bebidas não alcoólicas industrializadas (refrigerante, suco industrializado, etc) */
    val softDrinkFrequency: Double,
    /** Valor percentual de refrigerantes em relação ao total */
    val softDrinkValuePct: Double,
    /** Frequência de notas com bebidas não alcoólicas (água, suco, refrigerante, energético e similares) */
    val nonAlcoholicBeverageFrequency: Double,
    /** Valor percentual de bebidas não alcoólicas em relação ao total */
    val nonAlcoholicBeverageValuePct: Double,
    //
    // Frequência de bebidas alcoólicas: notas que possuem alguma bebida alcoólica
    /** Frequência de notas com bebidas alcoólicas (cerve ja, vinho, chopp, vodka, etc) */
    val alcoholicBeverageFrequency: Double,
    /** Valor percentual de bebidas alcoólicas em relação ao total */
    val alcoholicBeverageValuePct: Double,
    //
    // Co-ocorrências (consumo combinado)
    /** Frequência de notas que possuem bebidas de QUALQUER tipo junto com snacks */
    val beverageSnackCoOccurrenceFrequency: Double,
    /** Frequência de notas que possuem bebidas não alcoólicas junto com snacks */
    val nonAlcoholicBeverageSnackCoOccurrenceFrequency: Double,
    /** Frequência de notas que possuem bebidas alcoólicas junto com snacks */
    val alcoholSnackCoOccurrenceFrequency: Double,
    //
    // Bebidas adicionais/especializadas (raramente usadas no modelo, mas mantidas para análise)
    val energyDrinkFrequency: Double,
    val energyDrinkValuePct: Double,
    val snackSweetFrequency: Double,
    val snackSweetValuePct: Double,

    // === CO-OCCURRENCE & LIFESTYLE PATTERNS ===
    val hygieneCleaningCoOccurrenceFrequency: Double,
    val basicProduceCoOccurrenceFrequency: Double,

    // === SPECIALIZED CATEGORY METRICS ===
    /** Valor percentual de frozen convenience meals */
    val frozenConvenienceValuePct: Double,
    val frozenConvenienceFrequency: Double,
    val dairyValuePct: Double,
    val meatProteinValuePct: Double,
    val freshProduceValuePct: Double,
    val convenienceMealValuePct: Double,
    val convenienceMealFrequency: Double,

    // === ROUTINE/BEHAVIORAL SCORES ===
    // Scores que agregam padrões comportamentais específicos
    /** Frequência/consistência de itens essenciais nas compras */
    val essentialRoutineScore: Double,
    /** Frequência/consistência de itens de conveniência nas compras */
    val convenienceRoutineScore: Double,
    /** Frequência/consistência de compras com itens de limpeza/higiene */
    val householdRoutineScore: Double,
    /** Score de presença de alimentos frescos nas compras */
    val freshFoodPresenceScore: Double
)