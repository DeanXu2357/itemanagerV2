package com.example.itemanagerv2.data.local.dao

import androidx.room.*
import com.example.itemanagerv2.data.local.entity.ItemAttributeValue
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemAttributeValueDao {
    @Query("SELECT * FROM item_attribute_values WHERE itemId = :itemId")
    fun getAttributeValuesForItem(itemId: Int): Flow<List<ItemAttributeValue>>

    @Query("SELECT * FROM item_attribute_values WHERE id = :id")
    suspend fun getAttributeValueById(id: Int): ItemAttributeValue?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttributeValue(attributeValue: ItemAttributeValue): Long

    @Update
    suspend fun updateAttributeValue(attributeValue: ItemAttributeValue)

    @Delete
    suspend fun deleteAttributeValue(attributeValue: ItemAttributeValue)

    @Query("SELECT * FROM item_attribute_values WHERE itemId IN (:itemIds)")
    suspend fun getAttributesForItems(itemIds: List<Int>): List<ItemAttributeValue>

    @Query("DELETE FROM item_attribute_values WHERE itemId = :itemId")
    suspend fun deleteByItemId(itemId: Int)
}