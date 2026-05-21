package com.example.consumoai.domain.usecase

import com.example.consumoai.data.classifier.KeywordProductSemanticTagger
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Assert.assertFalse
import org.junit.Test

class CalculateConsumptionMetricsUseCaseTest {

    private val useCase = CalculateConsumptionMetricsUseCase()

    @Test
    fun invoke_computesExtendedMetricsAndTotals() {
        val receipts = listOf(
            Receipt(
                id = 1,
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "SUCO", price = 10.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "ARROZ", price = 20.0, category = ProductCategory.BASIC_FOOD)
                )
            ),
            Receipt(
                id = 2,
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "DETERGENTE", price = 5.0, category = ProductCategory.CLEANING),
                    ProductItem(name = "ITEM DESCONHECIDO", price = 15.0, category = ProductCategory.OTHER)
                )
            )
        )

        val metrics = useCase(receipts)

        assertEquals(2, metrics.totalReceipts)
        assertEquals(4, metrics.totalItems)
        assertEquals(50.0, metrics.totalValue, 0.0001)
        assertEquals(25.0, metrics.averageTicket, 0.0001)
        assertEquals(2.0, metrics.averageItemsPerReceipt, 0.0001)
        assertEquals(ProductCategory.BASIC_FOOD, metrics.maxCategoryByValue)
        assertEquals(ProductCategory.BASIC_FOOD, metrics.maxCategoryByItems)
        assertEquals(12.5, metrics.averageValuePerItem, 0.0001)
        assertEquals(30.0, metrics.highestReceiptValue, 0.0001)
        assertEquals(20.0, metrics.lowestReceiptValue, 0.0001)
        assertEquals(10.0, metrics.receiptValueAmplitude, 0.0001)
        assertEquals(20.0, metrics.categoryValueTotals[ProductCategory.BASIC_FOOD] ?: -1.0, 0.0001)
        assertEquals(1, metrics.categoryItemTotals[ProductCategory.CLEANING])
        assertEquals(20.0, metrics.categoryMetrics[ProductCategory.BASIC_FOOD]?.totalValue ?: -1.0, 0.0001)
        assertEquals(1, metrics.categoryMetrics[ProductCategory.CLEANING]?.totalItems ?: -1)
        assertEquals(5.0, metrics.categoryMetrics[ProductCategory.BEVERAGES]?.averageValuePerReceipt ?: -1.0, 0.0001)
        assertEquals(5.0, metrics.receiptAverageValueByCategory[ProductCategory.BEVERAGES] ?: -1.0, 0.0001)
        assertEquals(0.40, metrics.categoryConcentrationIndex, 0.0001)
        assertEquals(listOf(ProductCategory.BASIC_FOOD, ProductCategory.OTHER, ProductCategory.BEVERAGES), metrics.topThreeCategoriesByValue)
        assertEquals(0.90, metrics.topThreeCategoriesValuePercentage, 0.0001)
        assertEquals(0.10, metrics.categoryDominanceGap, 0.0001)
        assertEquals(2.0, metrics.averageCategoriesPerReceipt, 0.0001)
        assertEquals(4.0 / 7.0, metrics.categoryDiversityIndex, 0.0001)
        assertEquals(0.50, metrics.essentialCategoriesPercentage, 0.0001)
        assertEquals(0.50, metrics.nonEssentialCategoriesPercentage, 0.0001)
        assertEquals(0.0, metrics.industrializedToBasicFoodRatio, 0.0001)
        assertEquals(0.50, metrics.beveragesToBasicFoodRatio, 0.0001)
        assertEquals(0.20, metrics.beveragesToTotalRatio, 0.0001)
        assertEquals(0.0, metrics.produceToTotalRatio, 0.0001)
        assertEquals(0.0, metrics.receiptsWithIndustrializedPercentage, 0.0001)
        assertEquals(0.50, metrics.receiptsWithBeveragesPercentage, 0.0001)
        assertEquals(0.50, metrics.receiptsWithBasicFoodPercentage, 0.0001)
        assertEquals(0.0, metrics.receiptsWithProducePercentage, 0.0001)
        assertEquals(0.0, metrics.receiptsWithHygienePercentage, 0.0001)
        assertEquals(0.50, metrics.receiptsWithCleaningPercentage, 0.0001)
        assertEquals(0.0, metrics.averageIndustrializedItemsPerReceipt, 0.0001)
        assertEquals(0.50, metrics.averageBeveragesItemsPerReceipt, 0.0001)
        assertEquals(0.50, metrics.averageBasicFoodItemsPerReceipt, 0.0001)
        assertEquals(0.0, metrics.averageProduceItemsPerReceipt, 0.0001)
        assertEquals(0.50, metrics.highValueReceiptsPercentage, 0.0001)
        assertEquals(0.50, metrics.lowValueReceiptsPercentage, 0.0001)
        assertEquals(0.0666666667, metrics.convenienceScore, 0.0001)
        assertEquals(0.3333333333, metrics.essentialScore, 0.0001)
        assertEquals(0.4285714286, metrics.diversityScore, 0.0001)
        assertTrue(metrics.valuePercentageByCategory.values.all { it >= 0.0 })
        assertEquals(0.30, metrics.otherPercentageByValue, 0.0001)
        assertEquals(0.25, metrics.otherPercentageByItems, 0.0001)
        assertEquals(0.75, metrics.classifiedItemsPercentage, 0.0001)
    }

    @Test
    fun invoke_returnsZeroedMetricsForEmptyInput() {
        val metrics = useCase(emptyList())

        assertEquals(0, metrics.totalReceipts)
        assertEquals(0, metrics.totalItems)
        assertEquals(0.0, metrics.totalValue, 0.0001)
        assertEquals(0.0, metrics.averageItemsPerReceipt, 0.0001)
        assertNull(metrics.maxCategoryByValue)
        assertNull(metrics.maxCategoryByItems)
    }

    @Test
    fun invoke_correctlyIdentifiesAlcoholicBeveragesAndSeparatesFromNonAlcoholic() {
        val tagger = KeywordProductSemanticTagger()

        val receipts = listOf(
            Receipt(
                id = 1,
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "CERVEJA", price = 25.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "SALGADINHO", price = 10.0, category = ProductCategory.INDUSTRIALIZED)
                )
            ),
            Receipt(
                id = 2,
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "REFRIGERANTE", price = 8.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "AGUA", price = 5.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "ARROZ", price = 20.0, category = ProductCategory.BASIC_FOOD)
                )
            ),
            Receipt(
                id = 3,
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "VINHO", price = 40.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "AMENDOIM", price = 8.0, category = ProductCategory.INDUSTRIALIZED)
                )
            )
        )

        val metrics = CalculateConsumptionMetricsUseCase(semanticTagger = tagger)(receipts)

        // Validar métricas alcoólicas
        assertEquals(2.0 / 3.0, metrics.alcoholicBeverageFrequency, 0.0001) // 2 de 3 notas têm cerveja/vinho
        assertTrue(metrics.alcoholicBeverageValuePct > 0.0)

        // Validar métricas não-alcoólicas
        assertEquals(1.0 / 3.0, metrics.nonAlcoholicBeverageFrequency, 0.0001) // 1 de 3 notas têm água/suco/refri (nota 2)
        assertTrue(metrics.nonAlcoholicBeverageValuePct > 0.0)

        // Separação clara
        assertTrue(metrics.alcoholicBeverageFrequency > 0.0)
        assertTrue(metrics.nonAlcoholicBeverageFrequency > 0.0)
        assertFalse(metrics.alcoholicBeverageFrequency == metrics.nonAlcoholicBeverageFrequency)
    }

    @Test
    fun invoke_correctlyComputesSoftDrinkAndEnergyDrinkFrequencies() {
        val tagger = KeywordProductSemanticTagger()

        val receipts = listOf(
            Receipt(
                id = 1,
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "COCA COLA", price = 8.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "ARROZ", price = 20.0, category = ProductCategory.BASIC_FOOD)
                )
            ),
            Receipt(
                id = 2,
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "RED BULL", price = 15.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "BISCOITO", price = 5.0, category = ProductCategory.INDUSTRIALIZED)
                )
            )
        )

        val metrics = CalculateConsumptionMetricsUseCase(semanticTagger = tagger)(receipts)

        // Soft drink
        assertEquals(0.5, metrics.softDrinkFrequency, 0.0001) // 1 de 2 notas
        assertTrue(metrics.softDrinkValuePct > 0.0)

        // Energy drink
        assertEquals(0.5, metrics.energyDrinkFrequency, 0.0001) // 1 de 2 notas
        assertTrue(metrics.energyDrinkValuePct > 0.0)
    }

    @Test
    fun invoke_correctlyComputesBeverageAndSnackCoOccurrenceFrequencies() {
        val tagger = KeywordProductSemanticTagger()

        val receipts = listOf(
            Receipt(
                id = 1,
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "CERVEJA", price = 25.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "DORITOS", price = 10.0, category = ProductCategory.INDUSTRIALIZED)
                )
            ),
            Receipt(
                id = 2,
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "COCA", price = 8.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "BISCOITO", price = 5.0, category = ProductCategory.INDUSTRIALIZED)
                )
            ),
            Receipt(
                id = 3,
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "AGUA", price = 5.0, category = ProductCategory.BEVERAGES)
                )
            )
        )

        val metrics = CalculateConsumptionMetricsUseCase(semanticTagger = tagger)(receipts)

        // Ambas as co-ocorrências
        assertTrue(metrics.beverageSnackCoOccurrenceFrequency > 0.0)
        assertTrue(metrics.alcoholSnackCoOccurrenceFrequency > 0.0)
        assertTrue(metrics.nonAlcoholicBeverageSnackCoOccurrenceFrequency > 0.0)
    }
}

