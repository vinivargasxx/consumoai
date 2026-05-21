package com.example.consumoai.data.datasource.qrcode

import com.example.consumoai.data.parser.NfceHtmlParserDataSource
import com.example.consumoai.domain.model.ParsedNfceReceipt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class NfceQrCodeDataSource(
    private val nfceHtmlParserDataSource: NfceHtmlParserDataSource
) {

    suspend fun extractReceipt(url: String): ParsedNfceReceipt = withContext(Dispatchers.IO) {
        val document = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Android)")
            .timeout(20_000)
            .get()

        nfceHtmlParserDataSource.parse(document)
    }
}

