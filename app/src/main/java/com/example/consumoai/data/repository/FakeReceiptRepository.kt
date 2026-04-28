package com.example.consumoai.data.repository
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.repository.ReceiptRepository
import java.time.LocalDate
class FakeReceiptRepository : ReceiptRepository {
    private val receipts = mutableListOf(
        Receipt(
            id = 1L,
            date = LocalDate.now().minusDays(3),
            items = listOf(
                ProductItem("Arroz 5kg", 25.90, ProductCategory.BASIC_FOOD),
                ProductItem("Feijao 1kg", 8.90, ProductCategory.BASIC_FOOD),
                ProductItem("Refrigerante 2L", 9.50, ProductCategory.BEVERAGES),
                ProductItem("Bolacha recheada", 6.99, ProductCategory.INDUSTRIALIZED)
            )
        ),
        Receipt(
            id = 2L,
            date = LocalDate.now().minusDays(10),
            items = listOf(
                ProductItem("Detergente", 3.50, ProductCategory.CLEANING),
                ProductItem("Sabao em po", 18.90, ProductCategory.CLEANING),
                ProductItem("Shampoo", 15.90, ProductCategory.HYGIENE),
                ProductItem("Sabonete", 4.50, ProductCategory.HYGIENE)
            )
        ),
        Receipt(
            id = 3L,
            date = LocalDate.now().minusDays(20),
            items = listOf(
                ProductItem("Leite integral", 6.20, ProductCategory.BASIC_FOOD),
                ProductItem("Pao frances", 12.00, ProductCategory.BASIC_FOOD),
                ProductItem("Suco industrializado", 7.90, ProductCategory.BEVERAGES)
            )
        )
    )
    override suspend fun getReceiptsByPeriod(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Receipt> {
        return receipts.filter { receipt ->
            receipt.date >= startDate && receipt.date <= endDate
        }
    }
    override suspend fun saveReceipt(receipt: Receipt) {
        receipts.add(receipt)
    }
}
