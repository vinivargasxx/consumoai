package com.example.consumoai.data.mapper

import com.example.consumoai.data.local.entity.ProductItemEntity
import com.example.consumoai.data.local.entity.ReceiptEntity
import com.example.consumoai.data.local.entity.ReceiptWithItems
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource
import java.time.LocalDate

fun Receipt.toEntity(): ReceiptEntity = ReceiptEntity(
    id = id,
    accessKeyOrUrl = accessKeyOrUrl,
    date = date.toString(),
    source = source.name,
    totalValue = totalValue
)

fun ProductItem.toEntity(receiptId: Long): ProductItemEntity = ProductItemEntity(
    id = id,
    receiptId = receiptId,
    itemNumber = itemNumber,
    name = name,
    price = price,
    category = category.name
)

fun ReceiptWithItems.toDomain(): Receipt = Receipt(
    id = receipt.id,
    accessKeyOrUrl = receipt.accessKeyOrUrl,
    date = LocalDate.parse(receipt.date),
    source = ReceiptSource.valueOf(receipt.source),
    items = items.map { item ->
        ProductItem(
            id = item.id,
            receiptId = item.receiptId,
            itemNumber = item.itemNumber,
            name = item.name,
            price = item.price,
            category = item.category.toCategory()
        )
    }
)

private fun String.toCategory(): ProductCategory {
    return ProductCategory.entries.firstOrNull { it.name == this } ?: ProductCategory.OTHER
}

