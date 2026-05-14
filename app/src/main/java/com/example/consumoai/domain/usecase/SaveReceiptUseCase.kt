package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.repository.ReceiptRepository

class SaveReceiptUseCase(
    private val receiptRepository: ReceiptRepository
) {

    suspend operator fun invoke(receipt: Receipt) {
        receiptRepository.saveReceipt(receipt)
    }
}

