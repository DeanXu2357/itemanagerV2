package com.example.itemanagerv2.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "images",
    foreignKeys = [
        ForeignKey(
            entity = Item::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["itemId"])]
)
data class Image(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val filePath: String,
    val itemId: Int,
    val order: Int,
    val content: String?,
    val createdAt: Date,
    val updatedAt: Date
)
