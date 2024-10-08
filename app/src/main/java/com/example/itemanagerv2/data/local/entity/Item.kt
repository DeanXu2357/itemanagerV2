package com.example.itemanagerv2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val categoryId: Int,
    val codeType: String?,
    val codeContent: String?,
    val codeImageId: Int?,
    val coverImageId: Int?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)