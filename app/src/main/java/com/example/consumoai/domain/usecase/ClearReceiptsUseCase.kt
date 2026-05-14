package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.repository.ReceiptRepository

class ClearReceiptsUseCase(
    private val receiptRepository: ReceiptRepository
) {

    suspend operator fun invoke() {
        receiptRepository.clearReceipts()
    }
}

