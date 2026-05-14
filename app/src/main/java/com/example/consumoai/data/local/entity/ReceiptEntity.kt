package com.example.consumoai.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "receipts",
    indices = [Index(value = ["accessKeyOrUrl"], unique = true)]
)
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val accessKeyOrUrl: String? = null,
    val date: String,
    val source: String,
    val totalValue: Double
)

