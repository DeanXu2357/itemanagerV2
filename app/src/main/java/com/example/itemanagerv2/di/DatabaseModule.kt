package com.example.itemanagerv2.di

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.itemanagerv2.data.local.AppDatabase
import com.example.itemanagerv2.data.local.DatabaseMigrations
import com.example.itemanagerv2.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        Log.d("DatabaseModule", "Creating AppDatabase")

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    Log.d("DatabaseModule", "Database created")
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    Log.d("DatabaseModule", "Database opened, version: ${db.version}")
                }
            })
            .addMigrations(DatabaseMigrations.MIGRATION_1_2)
            .build()
            .also { db ->
                Log.d("DatabaseModule", "Database version: ${getCurrentDatabaseVersion(context)}")
            }
    }

    @Provides
    fun provideItemDao(database: AppDatabase): ItemDao = database.itemDao()

    @Provides
    fun provideItemCategoryDao(database: AppDatabase): ItemCategoryDao = database.itemCategoryDao()

    @Provides
    fun provideCategoryAttributeDao(database: AppDatabase): CategoryAttributeDao =
        database.categoryAttributeDao()

    @Provides
    fun provideItemAttributeValueDao(database: AppDatabase): ItemAttributeValueDao =
        database.itemAttributeValueDao()

    @Provides
    fun provideImageDao(database: AppDatabase): ImageDao = database.imageDao()
}

fun getCurrentDatabaseVersion(context: Context): Int {
    val dbPath = context.getDatabasePath(AppDatabase.DATABASE_NAME).absolutePath
    return try {
        SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            db.version
        }
    } catch (e: Exception) {
        Log.e("DatabaseModule", "Error getting database version", e)
        -1 // Return -1 if error
    }
}
