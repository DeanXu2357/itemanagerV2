package com.example.itemanagerv2

import android.app.Application
import androidx.room.Room
import com.example.itemanagerv2.data.local.AppDatabase

class MyApplication: Application() {
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "itemanager_database"
        ).build()
    }
}