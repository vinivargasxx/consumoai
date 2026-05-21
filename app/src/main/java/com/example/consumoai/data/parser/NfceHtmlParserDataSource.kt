package com.example.consumoai.data.parser

import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.ParsedNfceReceipt
import org.jsoup.nodes.Document
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

class NfceHtmlParserDataSource {

    private val moneyRegex = Regex("(\\d{1,4}[,.]\\d{2})")
    private val rowRegex = Regex("^(\\d{1,3})\\s+(.+?)\\s+(\\d{1,4}[,.]\\d{2})$")
    private val forbiddenLineTokens = setOf(
        "QTD TOTAL", "VALOR TOTAL", "VALOR PAGO", "FORMA DE PAGAMENTO", "CONSUMIDOR", "CHAVE", "PROTOCOLO"
    )

    fun parse(document: Document): ParsedNfceReceipt {
        val products = parseTabResultTable(document)
            .ifEmpty { parseByRows(document) }
            .ifEmpty {
            parseFallbackFromText(document.body().text())
            }
            .distinctBy { "${it.itemNumber}|${it.name}|${formatPrice(it.price)}" }
            .sortedBy { it.itemNumber ?: Int.MAX_VALUE }

        return ParsedNfceReceipt(
            items = products,
            issueDate = extractIssueDate(document)
        )
    }

    private fun extractIssueDate(document: Document): LocalDate? {
        val bodyText = normalizeSpaces(document.body().text())

        val emissionPatterns = listOf(
            Regex("(?:EMISSAO|DATA DE EMISSAO|DATA EMISSAO)\\s*[:\\-]?\\s*(\\d{2}/\\d{2}/\\d{4})(?:\\s+(\\d{2}:\\d{2}:\\d{2}))?"),
            Regex("\\b(\\d{2}/\\d{2}/\\d{4})\\s+(\\d{2}:\\d{2}:\\d{2})\\b"),
            Regex("\\b(\\d{2}/\\d{2}/\\d{4})\\b")
        )

        val candidate = emissionPatterns.firstNotNullOfOrNull { regex ->
            regex.find(bodyText)?.groupValues?.drop(1)?.firstOrNull { it.isNotBlank() }
        }

        return parseDate(candidate)
    }

    private fun parseDate(value: String?): LocalDate? {
        if (value.isNullOrBlank()) return null

        val trimmed = value.trim()
        val dateFormats = listOf(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy")
        )

        dateFormats.forEach { formatter ->
            try {
                return LocalDate.parse(trimmed, formatter)
            } catch (_: DateTimeParseException) {
            }
        }

        val dateTimeFormats = listOf(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("d/M/yyyy H:mm:ss")
        )
        dateTimeFormats.forEach { formatter ->
            try {
                return LocalDateTime.parse(trimmed, formatter).toLocalDate()
            } catch (_: DateTimeParseException) {
            }
        }

        return null
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

