package com.example.consumoai.domain.model

import java.time.LocalDate

data class Receipt(
    val id: Long = 0L,
    val accessKeyOrUrl: String? = null,
    val date: LocalDate = LocalDate.now(),
    val source: ReceiptSource,
    val items: List<ProductItem>
) {
    val totalValue: Double
        get() = items.sumOf { it.price }
}
