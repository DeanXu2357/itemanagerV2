package com.example.itemanagerv2.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "item_attribute_values",
    foreignKeys = [
        ForeignKey(
            entity = Item::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryAttribute::class,
            parentColumns = ["id"],
            childColumns = ["attributeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["itemId"]),
        Index(value = ["attributeId"])
    ]
)
data class ItemAttributeValue(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemId: Int,
    val attributeId: Int,
    val value: String,
    val createdAt: Date,
    val updatedAt: Date
)
