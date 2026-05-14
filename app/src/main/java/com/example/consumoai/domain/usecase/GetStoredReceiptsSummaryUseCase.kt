package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.StoredReceiptsSummary
import com.example.consumoai.domain.repository.ReceiptRepository

class GetStoredReceiptsSummaryUseCase(
    private val receiptRepository: ReceiptRepository
) {

    suspend operator fun invoke(): StoredReceiptsSummary {
        val receipts = receiptRepository.getAllReceipts()
        return StoredReceiptsSummary(
            totalReceipts = receipts.size,
            totalItems = receipts.sumOf { it.items.size },
            totalValue = receipts.sumOf { it.totalValue }
        )
    }
}

