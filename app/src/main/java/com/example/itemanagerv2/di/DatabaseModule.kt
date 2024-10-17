package com.example.itemanagerv2.di

import android.content.Context
import android.util.Log
import androidx.room.Room
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
            .addMigrations(DatabaseMigrations.MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
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
