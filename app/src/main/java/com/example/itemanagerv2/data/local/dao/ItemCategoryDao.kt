package com.example.itemanagerv2.data.local.dao

import androidx.room.*
import com.example.itemanagerv2.data.local.entity.ItemCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemCategoryDao {
    @Query("SELECT * FROM item_categories")
    fun getAllCategories(): Flow<List<ItemCategory>>

    @Query("SELECT * FROM item_categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): ItemCategory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: ItemCategory): Long

    @Update
    suspend fun updateCategory(category: ItemCategory)

    @Delete
    suspend fun deleteCategory(category: ItemCategory)

    @Query("SELECT * FROM item_categories WHERE id IN (:categoryIds)")
    suspend fun getCategoriesForItems(categoryIds: List<Int>): List<ItemCategory>
}
