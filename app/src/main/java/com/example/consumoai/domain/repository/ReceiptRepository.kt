package com.example.consumoai.domain.repository

import com.example.consumoai.domain.model.Receipt

interface ReceiptRepository {

    suspend fun saveReceipt(receipt: Receipt)

    suspend fun getAllReceipts(): List<Receipt>

    suspend fun clearReceipts()

    suspend fun existsByAccessKeyOrUrl(accessKeyOrUrl: String): Boolean
}
