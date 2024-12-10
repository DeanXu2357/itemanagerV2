package com.example.itemanagerv2.data.local.dao

import androidx.room.*
import com.example.itemanagerv2.data.local.entity.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items") fun getAll(): Flow<List<Item>>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(item: Item): Long

    @Update suspend fun updateItem(item: Item)

    @Delete suspend fun deleteItem(item: Item)

    @Query("DELETE FROM items WHERE id = :itemId")
    suspend fun deleteById(itemId: Int)

    @Query("SELECT * FROM items WHERE id = :id") suspend fun getItemById(id: Int): Item?

    @Query("SELECT * FROM items ORDER BY id DESC LIMIT :limit OFFSET :offset")
    suspend fun getPaginatedItems(limit: Int, offset: Int): List<Item>

    @Query("SELECT COUNT(*) FROM items") suspend fun getTotalItemCount(): Int
}
