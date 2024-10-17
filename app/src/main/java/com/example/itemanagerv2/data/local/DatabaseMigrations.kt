package com.example.itemanagerv2.data.local

import android.util.Log
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            Log.d("DatabaseMigration", "Starting migration from version 1 to 2")

            // Create item_categories table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `item_categories` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL
                )
            """.trimIndent())

            // Create category_attributes table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `category_attributes` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `categoryId` INTEGER NOT NULL,
                    `name` TEXT NOT NULL,
                    `isRequired` INTEGER NOT NULL,
                    `isEditable` INTEGER NOT NULL,
                    `valueType` TEXT NOT NULL,
                    `defaultValue` TEXT,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    FOREIGN KEY(`categoryId`) REFERENCES `item_categories`(`id`) ON DELETE CASCADE
                )
            """.trimIndent())

            Log.d("DatabaseMigration", "Migration Create category_attributes table completed")

            // Create item_attribute_values table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `item_attribute_values` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `itemId` INTEGER NOT NULL,
                    `attributeId` INTEGER NOT NULL,
                    `value` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    FOREIGN KEY(`itemId`) REFERENCES `items`(`id`) ON DELETE CASCADE,
                    FOREIGN KEY(`attributeId`) REFERENCES `category_attributes`(`id`) ON DELETE CASCADE
                )
            """.trimIndent())

            Log.d("DatabaseMigration", "Migration Create item_attribute_values table completed")

            // Create images table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `images` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `filePath` TEXT NOT NULL,
                    `itemId` INTEGER NOT NULL,
                    `order` INTEGER NOT NULL,
                    `content` TEXT,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    FOREIGN KEY(`itemId`) REFERENCES `items`(`id`) ON DELETE CASCADE
                )
            """.trimIndent())

            Log.d("DatabaseMigration", "Migration Create images table completed")

            // Add new columns to items table
            database.execSQL("ALTER TABLE `items` ADD COLUMN `categoryId` INTEGER")
            database.execSQL("ALTER TABLE `items` ADD COLUMN `codeType` TEXT")
            database.execSQL("ALTER TABLE `items` ADD COLUMN `codeContent` TEXT")
            database.execSQL("ALTER TABLE `items` ADD COLUMN `codeImageId` INTEGER")
            database.execSQL("ALTER TABLE `items` ADD COLUMN `coverImageId` INTEGER")

            Log.d("DatabaseMigration", "Migration Add new columns to items table completed")

            // Add default ItemCategories
            database.execSQL("""
                INSERT INTO item_categories (name, createdAt, updatedAt)
                VALUES 
                ('Electronics', ${System.currentTimeMillis()}, ${System.currentTimeMillis()}),
                ('Collectibles', ${System.currentTimeMillis()}, ${System.currentTimeMillis()}),
                ('Others', ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
            """.trimIndent())

            Log.d("DatabaseMigration", "Migration Add default ItemCategories completed")

            // Add CategoryAttributes for Electronics
            database.execSQL("""
                INSERT INTO category_attributes (categoryId, name, isRequired, isEditable, valueType, defaultValue, createdAt, updatedAt)
                VALUES 
                (1, 'name', 1, 1, 'string', NULL, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}),
                (1, 'price', 1, 1, 'number', '0', ${System.currentTimeMillis()}, ${System.currentTimeMillis()}),
                (1, 'quantity', 1, 1, 'number', '1', ${System.currentTimeMillis()}, ${System.currentTimeMillis()}),
                (1, 'brand', 0, 1, 'string', '', ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
            """.trimIndent())

            Log.d("DatabaseMigration", "Migration Add CategoryAttributes for Electronics completed")

            // Add CategoryAttributes for Collectibles
            database.execSQL("""
                INSERT INTO category_attributes (categoryId, name, isRequired, isEditable, valueType, defaultValue, createdAt, updatedAt)
                VALUES 
                (2, 'name', 1, 1, 'string', NULL, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}),
                (2, 'price', 1, 1, 'number', '0', ${System.currentTimeMillis()}, ${System.currentTimeMillis()}),
                (2, 'quantity', 1, 1, 'number', '1', ${System.currentTimeMillis()}, ${System.currentTimeMillis()}),
                (2, 'brand', 0, 1, 'string', '', ${System.currentTimeMillis()}, ${System.currentTimeMillis()}),
                (2, 'isUnpack', 0, 1, 'boolean', '1', ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
            """.trimIndent())

            Log.d("DatabaseMigration", "Migration Add CategoryAttributes for Collectibles completed")

            // Add CategoryAttributes for Others
            database.execSQL("""
                INSERT INTO category_attributes (categoryId, name, isRequired, isEditable, valueType, defaultValue, createdAt, updatedAt)
                VALUES 
                (3, 'name', 1, 1, 'string', NULL, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}),
                (3, 'quantity', 1, 1, 'number', '1', ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
            """.trimIndent())

            Log.d("DatabaseMigration", "Migration Add CategoryAttributes for Others completed")

            // Insert sample data
            database.execSQL("""
                INSERT INTO items (name, categoryId, codeType, codeContent, createdAt, updatedAt)
                VALUES 
                ('iPhone 13', 1, 'QR', 'IPHONE13-QR', ${System.currentTimeMillis()}, ${System.currentTimeMillis()}),
                ('MacBook Pro', 1, 'Barcode', 'MACBOOKPRO-BARCODE', ${System.currentTimeMillis()}, ${System.currentTimeMillis()}),
                ('AirPods Pro', 1, 'QR', 'AIRPODSPRO-QR', ${System.currentTimeMillis()}, ${System.currentTimeMillis()}),
                ('iPad Air', 1, 'Barcode', 'IPADAIR-BARCODE', ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
            """.trimIndent())

            Log.d("DatabaseMigration", "Migration from version 1 to 2 completed")
        }
    }
}
