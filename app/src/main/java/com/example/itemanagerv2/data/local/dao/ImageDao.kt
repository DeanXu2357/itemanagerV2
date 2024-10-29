package com.example.itemanagerv2.data.local.dao

import androidx.room.*
import com.example.itemanagerv2.data.local.entity.Image
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    @Query("SELECT * FROM images WHERE itemId = :itemId ORDER BY `order`")
    fun getImagesForItem(itemId: Int): Flow<List<Image>>

    @Query("SELECT * FROM images WHERE id = :id")
    suspend fun getImageById(id: Int): Image?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertImage(image: Image): Long

    @Update
    suspend fun updateImage(image: Image)

    @Delete
    suspend fun deleteImage(image: Image)

    @Query("SELECT * FROM images WHERE itemId IN (:itemIds)")
    suspend fun getImagesForItems(itemIds: List<Int>): List<Image>

    @Query("SELECT * FROM images WHERE itemId = :itemId")
    suspend fun getImagesByItemId(itemId: Int): List<Image>

    @Query("DELETE FROM images WHERE itemId = :itemId")
    suspend fun deleteByItemId(itemId: Int)
}
