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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: Image): Long

    @Update
    suspend fun updateImage(image: Image)

    @Delete
    suspend fun deleteImage(image: Image)
}