package com.example.itemanagerv2.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.itemanagerv2.data.local.AppDatabase
import com.example.itemanagerv2.data.local.DatabaseMigrations
import com.example.itemanagerv2.data.local.dao.*
import com.example.itemanagerv2.data.local.repository.ItemRepository
import com.example.itemanagerv2.data.manager.ImageManager
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
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
    }

    @Provides
    fun provideItemDao(database: AppDatabase): ItemDao = database.itemDao()

    @Provides
    fun provideItemCategoryDao(database: AppDatabase): ItemCategoryDao = database.itemCategoryDao()

    @Provides
    fun provideCategoryAttributeDao(database: AppDatabase): CategoryAttributeDao = database.categoryAttributeDao()

    @Provides
    fun provideItemAttributeValueDao(database: AppDatabase): ItemAttributeValueDao = database.itemAttributeValueDao()

    @Provides
    fun provideImageDao(database: AppDatabase): ImageDao = database.imageDao()

    @Provides
    @Singleton
    fun provideItemRepository(
        itemDao: ItemDao,
        itemCategoryDao: ItemCategoryDao,
        categoryAttributeDao: CategoryAttributeDao,
        itemAttributeValueDao: ItemAttributeValueDao,
        imageDao: ImageDao
    ): ItemRepository {
        return ItemRepository(itemDao, itemCategoryDao, categoryAttributeDao, itemAttributeValueDao, imageDao)
    }

    @Provides
    @Singleton
    fun provideImageManager(@ApplicationContext context: Context): ImageManager {
        return ImageManager(context)
    }
}
