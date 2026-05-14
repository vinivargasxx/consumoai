package com.example.consumoai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.consumoai.data.local.entity.ProductItemEntity
import com.example.consumoai.data.local.entity.ReceiptEntity
import com.example.consumoai.data.local.entity.ReceiptWithItems

@Dao
interface ReceiptDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertReceipt(receipt: ReceiptEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ProductItemEntity>)

    @Transaction
    @Query("SELECT * FROM receipts ORDER BY id DESC")
    suspend fun getAllReceiptsWithItems(): List<ReceiptWithItems>

    @Query("DELETE FROM receipts")
    suspend fun deleteAllReceipts()

    @Query("SELECT EXISTS(SELECT 1 FROM receipts WHERE accessKeyOrUrl = :accessKeyOrUrl LIMIT 1)")
    suspend fun existsByAccessKeyOrUrl(accessKeyOrUrl: String): Boolean
}

