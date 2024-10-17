package com.example.itemanagerv2.data.manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageManager @Inject constructor(private val context: Context) {

    fun saveImage(bitmap: Bitmap): String {
        val filename = "${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        return file.absolutePath
    }

    fun getImageFromPath(path: String): Bitmap? {
        return BitmapFactory.decodeFile(path)
    }
}