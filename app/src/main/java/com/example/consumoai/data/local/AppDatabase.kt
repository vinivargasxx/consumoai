package com.example.consumoai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.consumoai.data.local.dao.ReceiptDao
import com.example.consumoai.data.local.entity.ProductItemEntity
import com.example.consumoai.data.local.entity.ReceiptEntity

@Database(
    entities = [ReceiptEntity::class, ProductItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun receiptDao(): ReceiptDao
}

