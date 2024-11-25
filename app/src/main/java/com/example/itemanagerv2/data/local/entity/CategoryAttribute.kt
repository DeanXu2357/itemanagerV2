package com.example.itemanagerv2.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "category_attributes",
    foreignKeys = [
        ForeignKey(
            entity = ItemCategory::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
data class CategoryAttribute(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryId: Int,
    val name: String,
    val isRequired: Boolean,
    val isEditable: Boolean,
    val valueType: String,
    val defaultValue: String?,
    val createdAt: Date,
    val updatedAt: Date
) {
    companion object {
        const val TYPE_STRING = "string"
        const val TYPE_NUMBER = "number" 
        const val TYPE_DATE_STRING = "date_string"

        val AVAILABLE_TYPES = listOf(
            TYPE_STRING,
            TYPE_NUMBER,
            TYPE_DATE_STRING
        )
    }
}
