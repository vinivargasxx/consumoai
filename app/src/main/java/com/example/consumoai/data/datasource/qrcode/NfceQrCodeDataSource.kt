package com.example.consumoai.data.datasource.qrcode

import android.util.Log
import com.example.consumoai.data.parser.NfceHtmlParserDataSource
import com.example.consumoai.domain.model.ProductItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class NfceQrCodeDataSource(
    private val nfceHtmlParserDataSource: NfceHtmlParserDataSource
) {

    suspend fun extractProducts(url: String): List<ProductItem> = withContext(Dispatchers.IO) {
        val document = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Android)")
            .timeout(20_000)
            .get()

        Log.d("NFCE_HTML", document.html().take(3000))
        nfceHtmlParserDataSource.parse(document)
    }
}

