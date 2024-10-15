package com.example.itemanagerv2.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.itemanagerv2.data.local.dao.*
import com.example.itemanagerv2.data.local.entity.*

@Database(
    entities = [
        Item::class,
        ItemCategory::class,
        CategoryAttribute::class,
        ItemAttributeValue::class,
        Image::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun itemCategoryDao(): ItemCategoryDao
    abstract fun categoryAttributeDao(): CategoryAttributeDao
    abstract fun itemAttributeValueDao(): ItemAttributeValueDao
    abstract fun imageDao(): ImageDao

    companion object {
        const val DATABASE_NAME = "item_manager_database"
    }
}
