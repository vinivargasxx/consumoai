package com.example.consumoai.domain.repository

import com.example.consumoai.domain.model.Receipt
import java.time.LocalDate

interface ReceiptRepository {

    suspend fun getReceiptsByPeriod(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Receipt>

    suspend fun saveReceipt(receipt: Receipt)
}

