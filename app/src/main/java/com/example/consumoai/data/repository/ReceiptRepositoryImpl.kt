package com.example.consumoai.data.repository

import com.example.consumoai.data.local.dao.ReceiptDao
import com.example.consumoai.data.mapper.toDomain
import com.example.consumoai.data.mapper.toEntity
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.repository.ReceiptRepository

class ReceiptRepositoryImpl(
    private val receiptDao: ReceiptDao
) : ReceiptRepository {

    override suspend fun saveReceipt(receipt: Receipt) {
        val key = receipt.accessKeyOrUrl
        if (!key.isNullOrBlank() && receiptDao.existsByAccessKeyOrUrl(key)) {
            return
        }

        val receiptId = receiptDao.insertReceipt(receipt.toEntity())
        val items = receipt.items.map { item -> item.toEntity(receiptId) }
        if (items.isNotEmpty()) {
            receiptDao.insertItems(items)
        }
    }

    override suspend fun getAllReceipts(): List<Receipt> {
        return receiptDao.getAllReceiptsWithItems().map { it.toDomain() }
    }

    override suspend fun clearReceipts() {
        receiptDao.deleteAllReceipts()
    }

    override suspend fun existsByAccessKeyOrUrl(accessKeyOrUrl: String): Boolean {
        return receiptDao.existsByAccessKeyOrUrl(accessKeyOrUrl)
    }
}

