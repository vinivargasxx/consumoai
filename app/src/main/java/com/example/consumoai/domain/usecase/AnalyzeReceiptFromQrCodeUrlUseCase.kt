package com.example.consumoai.domain.usecase

import com.example.consumoai.data.datasource.qrcode.NfceQrCodeDataSource
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource
import java.time.LocalDate

class AnalyzeReceiptFromQrCodeUrlUseCase(
    private val nfceQrCodeDataSource: NfceQrCodeDataSource,
    private val classifyProductsUseCase: ClassifyProductsUseCase
) {

    suspend operator fun invoke(url: String): Receipt {
        val parsedReceipt = nfceQrCodeDataSource.extractReceipt(url)
        val products = classifyProductsUseCase(parsedReceipt.items)
        return Receipt(
            accessKeyOrUrl = url,
            date = parsedReceipt.issueDate ?: LocalDate.now(),
            source = ReceiptSource.QR_CODE,
            items = products
        )
    }
}

