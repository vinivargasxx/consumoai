package com.example.consumoai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "product_items",
    foreignKeys = [
        ForeignKey(
            entity = ReceiptEntity::class,
            parentColumns = ["id"],
            childColumns = ["receiptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["receiptId"])]
)
data class ProductItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val receiptId: Long,
    val itemNumber: Int?,
    val name: String,
    val price: Double,
    val category: String
)

