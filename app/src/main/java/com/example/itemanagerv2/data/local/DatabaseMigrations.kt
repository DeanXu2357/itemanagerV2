package com.example.itemanagerv2.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
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

            // Add new columns to items table
            database.execSQL("ALTER TABLE `items` ADD COLUMN `categoryId` INTEGER")
            database.execSQL("ALTER TABLE `items` ADD COLUMN `codeType` TEXT")
            database.execSQL("ALTER TABLE `items` ADD COLUMN `codeContent` TEXT")
            database.execSQL("ALTER TABLE `items` ADD COLUMN `codeImageId` INTEGER")
            database.execSQL("ALTER TABLE `items` ADD COLUMN `coverImageId` INTEGER")
        }
    }

    val Migration_mock_data = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Insert sample data
            database.execSQL("""
                INSERT INTO items (name, categoryId, codeType, codeContent, createdAt, updatedAt)
                VALUES 
                ('iPhone 13', 1, 'QR', 'IPHONE13-QR', ${System.currentTimeMillis()}, ${System.currentTimeMillis()}),
                ('MacBook Pro', 1, 'Barcode', 'MACBOOKPRO-BARCODE', ${System.currentTimeMillis()}, ${System.currentTimeMillis()}),
                ('AirPods Pro', 1, 'QR', 'AIRPODSPRO-QR', ${System.currentTimeMillis()}, ${System.currentTimeMillis()}),
                ('iPad Air', 1, 'Barcode', 'IPADAIR-BARCODE', ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
            """.trimIndent())
        }
    }

    val MIGRATION_ADDDEFAULTSETTINGS = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add default ItemCategories
            database.execSQL("""
                INSERT INTO item_categories (name, createdAt, updatedAt)
                VALUES 
                ('Electronics', ${System.currentTimeMillis()}, ${System.currentTimeMillis()}),
                ('Collectibles', ${System.currentTimeMillis()}, ${System.currentTimeMillis()}),
                ('Others', ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
            """.trimIndent())

            // Add CategoryAttributes for Electronics
            database.execSQL("""
                INSERT INTO category_attributes (categoryId, name, isRequired, isEditable, valueType, defaultValue, createdAt, updatedAt)
                VALUES 
                (1, 'name', 1, 1, 'string', NULL, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}),
                (1, 'price', 1, 1, 'number', '0', ${System.currentTimeMillis()}, ${System.currentTimeMillis()}),
                (1, 'quantity', 1, 1, 'number', '1', ${System.currentTimeMillis()}, ${System.currentTimeMillis()}),
                (1, 'brand', 0, 1, 'string', '', ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
            """.trimIndent())

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

            // Add CategoryAttributes for Others
            database.execSQL("""
                INSERT INTO category_attributes (categoryId, name, isRequired, isEditable, valueType, defaultValue, createdAt, updatedAt)
                VALUES 
                (3, 'name', 1, 1, 'string', NULL, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}),
                (3, 'quantity', 1, 1, 'number', '1', ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
            """.trimIndent())
        }
    }
}
