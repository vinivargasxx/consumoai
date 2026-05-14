package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource
import com.example.consumoai.domain.repository.ReceiptRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetStoredReceiptsSummaryUseCaseTest {

    @Test
    fun invoke_returnsAggregatedSummaryFromStoredReceipts() = runBlocking {
        val repository = object : ReceiptRepository {
            override suspend fun saveReceipt(receipt: Receipt) = Unit

            override suspend fun getAllReceipts(): List<Receipt> = listOf(
                Receipt(
                    id = 1L,
                    source = ReceiptSource.QR_CODE,
                    items = listOf(
                        ProductItem(name = "SUCO", price = 6.5, category = ProductCategory.BEVERAGES),
                        ProductItem(name = "PAO", price = 4.0, category = ProductCategory.BASIC_FOOD)
                    )
                ),
                Receipt(
                    id = 2L,
                    source = ReceiptSource.QR_CODE,
                    items = listOf(
                        ProductItem(name = "LEITE", price = 8.5, category = ProductCategory.BASIC_FOOD)
                    )
                )
            )

            override suspend fun clearReceipts() = Unit

            override suspend fun existsByAccessKeyOrUrl(accessKeyOrUrl: String): Boolean = false
        }

        val result = GetStoredReceiptsSummaryUseCase(repository)()

        assertEquals(2, result.totalReceipts)
        assertEquals(3, result.totalItems)
        assertEquals(19.0, result.totalValue, 0.0001)
    }
}

