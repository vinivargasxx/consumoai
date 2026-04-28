package com.example.consumoai.domain.model

import java.time.LocalDate

data class Receipt(
    val id: Long = 0L,
    val date: LocalDate,
    val items: List<ProductItem>
) {
    val totalValue: Double
        get() = items.sumOf { it.price }
}
