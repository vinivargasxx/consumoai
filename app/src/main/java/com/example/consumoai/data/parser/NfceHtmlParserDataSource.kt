package com.example.consumoai.data.parser

import com.example.consumoai.domain.model.ProductItem
import org.jsoup.nodes.Document
import java.util.Locale

class NfceHtmlParserDataSource {

    private val moneyRegex = Regex("(\\d{1,4}[,.]\\d{2})")
    private val rowRegex = Regex("^(\\d{1,3})\\s+(.+?)\\s+(\\d{1,4}[,.]\\d{2})$")
    private val forbiddenLineTokens = setOf(
        "QTD TOTAL", "VALOR TOTAL", "VALOR PAGO", "FORMA DE PAGAMENTO", "CONSUMIDOR", "CHAVE", "PROTOCOLO"
    )

    fun parse(document: Document): List<ProductItem> {
        val products = parseTabResultTable(document)
            .ifEmpty { parseByRows(document) }
            .ifEmpty {
            parseFallbackFromText(document.body()?.text().orEmpty())
            }
            .distinctBy { "${it.itemNumber}|${it.name}|${formatPrice(it.price)}" }
            .sortedBy { it.itemNumber ?: Int.MAX_VALUE }


        return products
    }

    private fun parseTabResultTable(document: Document): List<ProductItem> {
        val rows = document.select("table#tabResult tr")
        if (rows.isEmpty()) return emptyList()

        return rows.mapNotNull { row ->
            val rawId = row.id().trim()
            val itemNumber = Regex("(\\d{1,3})").find(rawId)?.groupValues?.getOrNull(1)?.toIntOrNull()
                ?: return@mapNotNull null

            val name = normalizeSpaces(row.selectFirst("span.txtTit")?.text().orEmpty())
                .replace("**", "")
                .trim()
            if (name.isBlank()) return@mapNotNull null

            val priceText = row.selectFirst("span.valor")?.text().orEmpty()
            val price = parsePrice(priceText) ?: return@mapNotNull null

            ProductItem(
                itemNumber = itemNumber,
                name = name,
                price = price
            )
        }
    }

    private fun parseByRows(document: Document): List<ProductItem> {
        val products = mutableListOf<ProductItem>()

        val rows = document.select("tr, li, div")
            .map { it.text().trim() }
            .filter { it.isNotBlank() }

        for (row in rows) {
            val normalized = normalizeSpaces(row)
            if (containsForbiddenToken(normalized)) continue

            val match = rowRegex.find(normalized)
            if (match != null) {
                val itemNumber = match.groupValues[1].toIntOrNull()
                val name = match.groupValues[2].trim()
                val price = parsePrice(match.groupValues[3])
                if (itemNumber != null && !name.isBlank() && price != null) {
                    products.add(ProductItem(itemNumber = itemNumber, name = name, price = price))
                }
                continue
            }

            val itemNumber = Regex("^(\\d{1,3})\\b").find(normalized)?.groupValues?.getOrNull(1)?.toIntOrNull()
                ?: continue
            val prices = moneyRegex.findAll(normalized).map { it.groupValues[1] }.toList()
            if (prices.isEmpty()) continue

            val price = parsePrice(prices.last()) ?: continue
            val name = normalized
                .replaceFirst(Regex("^\\d{1,3}\\s+"), "")
                .replace(prices.last(), "")
                .trim()

            if (name.isNotBlank()) {
                products.add(ProductItem(itemNumber = itemNumber, name = name, price = price))
            }
        }

        return products
    }

    private fun parseFallbackFromText(bodyText: String): List<ProductItem> {
        val lines = bodyText
            .split(Regex("\\s{2,}|\\n"))
            .map { normalizeSpaces(it) }
            .filter { it.isNotBlank() }

        return lines.mapNotNull { line ->
            if (containsForbiddenToken(line)) return@mapNotNull null

            val match = rowRegex.find(line) ?: return@mapNotNull null
            val itemNumber = match.groupValues[1].toIntOrNull() ?: return@mapNotNull null
            val name = match.groupValues[2].trim()
            val price = parsePrice(match.groupValues[3]) ?: return@mapNotNull null

            ProductItem(itemNumber = itemNumber, name = name, price = price)
        }
    }

    private fun parsePrice(value: String): Double? {
        return value
            .replace(',', '.')
            .replace(Regex("[^\\d.]"), "")
            .toDoubleOrNull()
    }

    private fun containsForbiddenToken(line: String): Boolean {
        val upper = line.uppercase(Locale.ROOT)
        return forbiddenLineTokens.any { upper.contains(it) }
    }

    private fun normalizeSpaces(value: String): String {
        return value.replace(Regex("\\s+"), " ").trim()
    }

    private fun formatPrice(price: Double): String = "%.2f".format(Locale.US, price)
}

