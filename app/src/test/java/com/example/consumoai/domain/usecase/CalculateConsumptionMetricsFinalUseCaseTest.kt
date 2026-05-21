package com.example.consumoai.domain.usecase

import com.example.consumoai.data.classifier.KeywordProductSemanticTagger
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CalculateConsumptionMetricsFinalUseCaseTest {

    private val useCase = CalculateConsumptionMetricsUseCase(
        semanticTagger = KeywordProductSemanticTagger()
    )

    @Test
    fun invoke_calculatesTemporalCooccurrenceRecurrenceAndScores() {
        val receipts = listOf(
            Receipt(
                date = LocalDate.of(2026, 5, 1),
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "COCA COLA 2L", price = 12.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "DORITOS 110G", price = 9.0, category = ProductCategory.INDUSTRIALIZED),
                    ProductItem(name = "ARROZ 1KG", price = 8.0, category = ProductCategory.BASIC_FOOD)
                )
            ),
            Receipt(
                date = LocalDate.of(2026, 5, 8),
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "CHOPP IPA", price = 18.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "DORITOS 120G", price = 10.0, category = ProductCategory.INDUSTRIALIZED),
                    ProductItem(name = "MELANCIA", price = 15.0, category = ProductCategory.PRODUCE)
                )
            ),
            Receipt(
                date = LocalDate.of(2026, 5, 15),
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "COCA COLA 350ML", price = 6.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "SABONETE DOVE", price = 7.0, category = ProductCategory.HYGIENE),
                    ProductItem(name = "OMO 1KG", price = 20.0, category = ProductCategory.CLEANING)
                )
            )
        )

        val result = useCase(receipts)

        assertTrue(result.timeSpanDays >= 14.0)
        assertTrue(result.receiptsPerWeek > 0.0)
        assertTrue(result.averageDaysBetweenReceipts > 0.0)
        assertTrue(result.recurringItemRatio >= 0.0)
        assertTrue(result.topItemRepetitionRate >= 0.0)
        assertTrue(result.beverageSnackCoOccurrenceFrequency > 0.0)
        assertTrue(result.alcoholSnackCoOccurrenceFrequency > 0.0)
        assertTrue(result.hygieneCleaningCoOccurrenceFrequency > 0.0)
        assertTrue(result.essentialRoutineScore in 0.0..1.0)
        assertTrue(result.convenienceRoutineScore in 0.0..1.0)
        assertTrue(result.householdRoutineScore in 0.0..1.0)
        assertTrue(result.freshFoodPresenceScore in 0.0..1.0)

        assertTrue(result.softDrinkFrequency > 0.0)
        assertTrue(result.alcoholicBeverageFrequency > 0.0)
        assertEquals(true, result.totalReceipts == 3)
    }
}

