package com.example.consumoai.domain.usecase

import com.example.consumoai.data.datasource.qrcode.NfceQrCodeDataSource
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource

class AnalyzeReceiptFromQrCodeUrlUseCase(
    private val nfceQrCodeDataSource: NfceQrCodeDataSource,
    private val classifyProductsUseCase: ClassifyProductsUseCase
) {

    suspend operator fun invoke(url: String): Receipt {
        val products = classifyProductsUseCase(nfceQrCodeDataSource.extractProducts(url))
        return Receipt(
            accessKeyOrUrl = url,
            source = ReceiptSource.QR_CODE,
            items = products
        )
    }
}

