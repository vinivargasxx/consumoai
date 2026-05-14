package com.example.consumoai.data.repository

import com.example.consumoai.data.local.dao.ReceiptDao
import com.example.consumoai.data.local.entity.ProductItemEntity
import com.example.consumoai.data.local.entity.ReceiptEntity
import com.example.consumoai.data.local.entity.ReceiptWithItems
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceiptRepositoryImplTest {

    @Test
    fun saveReceipt_persistsReceiptAndSkipsDuplicatesByUrl() = runBlocking {
        val dao = FakeReceiptDao()
        val repository = ReceiptRepositoryImpl(dao)
        val receipt = Receipt(
            accessKeyOrUrl = "https://example.com/nfce/1",
            source = ReceiptSource.QR_CODE,
            items = listOf(
                ProductItem(itemNumber = 1, name = "SUCO", price = 10.0),
                ProductItem(itemNumber = 2, name = "PAO", price = 5.0)
            )
        )

        repository.saveReceipt(receipt)
        repository.saveReceipt(receipt)

        val stored = repository.getAllReceipts()
        assertEquals(1, stored.size)
        assertEquals(2, stored.first().items.size)
        assertEquals(15.0, stored.first().totalValue, 0.0001)
        assertTrue(repository.existsByAccessKeyOrUrl("https://example.com/nfce/1"))
    }

    @Test
    fun clearReceipts_removesAllStoredData() = runBlocking {
        val dao = FakeReceiptDao()
        val repository = ReceiptRepositoryImpl(dao)

        repository.saveReceipt(
            Receipt(
                accessKeyOrUrl = "https://example.com/nfce/2",
                source = ReceiptSource.QR_CODE,
                items = listOf(ProductItem(itemNumber = 1, name = "BISCOITO", price = 3.5))
            )
        )

        repository.clearReceipts()

        assertTrue(repository.getAllReceipts().isEmpty())
    }

    private class FakeReceiptDao : ReceiptDao {
        private val receipts = mutableListOf<ReceiptEntity>()
        private val items = mutableListOf<ProductItemEntity>()
        private var nextReceiptId = 1L
        private var nextItemId = 1L

        override suspend fun insertReceipt(receipt: ReceiptEntity): Long {
            val id = nextReceiptId++
            receipts.add(receipt.copy(id = id))
            return id
        }

        override suspend fun insertItems(items: List<ProductItemEntity>) {
            this.items.addAll(items.map { it.copy(id = nextItemId++) })
        }

        override suspend fun getAllReceiptsWithItems(): List<ReceiptWithItems> {
            return receipts.reversed().map { receipt ->
                ReceiptWithItems(
                    receipt = receipt,
                    items = items.filter { it.receiptId == receipt.id }
                )
            }
        }

        override suspend fun deleteAllReceipts() {
            receipts.clear()
            items.clear()
            nextReceiptId = 1L
            nextItemId = 1L
        }

        override suspend fun existsByAccessKeyOrUrl(accessKeyOrUrl: String): Boolean {
            return receipts.any { it.accessKeyOrUrl == accessKeyOrUrl }
        }
    }
}
