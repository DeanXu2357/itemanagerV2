package com.example.itemanagerv2.data.local.dao

import androidx.room.*
import com.example.itemanagerv2.data.local.entity.CategoryAttribute
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryAttributeDao {
    @Query("SELECT * FROM category_attributes WHERE categoryId = :categoryId")
    fun getAttributesForCategory(categoryId: Int): Flow<List<CategoryAttribute>>

    @Query("SELECT * FROM category_attributes WHERE id = :id")
    suspend fun getAttributeById(id: Int): CategoryAttribute?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttribute(attribute: CategoryAttribute): Long

    @Update
    suspend fun updateAttribute(attribute: CategoryAttribute)

    @Delete
    suspend fun deleteAttribute(attribute: CategoryAttribute)

    @Query("DELETE FROM category_attributes WHERE id = :attributeId")
    suspend fun deleteById(attributeId: Int)
}
