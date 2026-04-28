package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.ConsumptionMetrics
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.Receipt
import java.time.temporal.ChronoUnit

class CalculateConsumptionMetricsUseCase {

    operator fun invoke(receipts: List<Receipt>): ConsumptionMetrics {
        if (receipts.isEmpty()) {
            return ConsumptionMetrics(
                valuePercentageByCategory = emptyCategoryMap(),
                itemPercentageByCategory = emptyCategoryMap(),
                frequencyByCategory = emptyCategoryMap(),
                totalReceipts = 0,
                periodDays = 0,
                totalValue = 0.0,
                averageTicket = 0.0
            )
        }

        val allItems = receipts.flatMap { it.items }
        val totalValue = receipts.sumOf { it.totalValue }
        val totalItems = allItems.size

        val valuePercentageByCategory = ProductCategory.entries.associateWith { category ->
            val categoryValue = allItems
                .filter { it.category == category }
                .sumOf { it.price }

            if (totalValue > 0.0) categoryValue / totalValue else 0.0
        }

        val itemPercentageByCategory = ProductCategory.entries.associateWith { category ->
            val categoryItems = allItems.count { it.category == category }

            if (totalItems > 0) {
                categoryItems.toDouble() / totalItems
            } else {
                0.0
            }
        }

        val frequencyByCategory = ProductCategory.entries.associateWith { category ->
            val receiptsWithCategory = receipts.count { receipt ->
                receipt.items.any { it.category == category }
            }

            receiptsWithCategory.toDouble() / receipts.size
        }

        val firstDate = receipts.minOf { it.date }
        val lastDate = receipts.maxOf { it.date }

        val periodDays = ChronoUnit.DAYS
            .between(firstDate, lastDate)
            .toInt()
            .coerceAtLeast(1)

        val averageTicket = totalValue / receipts.size

        return ConsumptionMetrics(
            valuePercentageByCategory = valuePercentageByCategory,
            itemPercentageByCategory = itemPercentageByCategory,
            frequencyByCategory = frequencyByCategory,
            totalReceipts = receipts.size,
            periodDays = periodDays,
            totalValue = totalValue,
            averageTicket = averageTicket
        )
    }

    private fun emptyCategoryMap(): Map<ProductCategory, Double> {
        return ProductCategory.entries.associateWith { 0.0 }
    }
}

