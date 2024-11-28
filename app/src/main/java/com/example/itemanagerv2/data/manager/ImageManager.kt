package com.example.itemanagerv2.data.manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageManager @Inject constructor(private val context: Context) {
    // Map to store temporary bitmaps before saving to storage
    private val temporaryBitmaps = mutableMapOf<String, Bitmap>()

    // Generate a temporary ID for an image without saving it
    fun generateTemporaryImageId(): String {
        return "temp_${UUID.randomUUID()}"
    }

    // Store bitmap temporarily in memory
    fun storeTemporaryBitmap(tempId: String, bitmap: Bitmap) {
        temporaryBitmaps[tempId] = bitmap
    }

    // Save a temporary bitmap to storage and return its file path
    fun saveTemporaryBitmap(tempId: String): String? {
        val bitmap = temporaryBitmaps[tempId] ?: return null
        val filePath = saveImage(bitmap)
        temporaryBitmaps.remove(tempId)
        return filePath
    }

    // Clear temporary bitmap from memory
    fun clearTemporaryBitmap(tempId: String) {
        temporaryBitmaps.remove(tempId)
    }

    // Get temporary bitmap from memory
    fun getTemporaryBitmap(tempId: String): Bitmap? {
        return temporaryBitmaps[tempId]
    }

    fun saveImage(bitmap: Bitmap): String {
        val filename = "${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        return file.absolutePath
    }

    fun getImageFromPath(path: String): Bitmap? {
        // Check if it's a temporary ID
        if (path.startsWith("temp_")) {
            return temporaryBitmaps[path]
        }
        return BitmapFactory.decodeFile(path)
    }

    fun deleteImage(path: String): Boolean {
        // If it's a temporary image, just remove it from memory
        if (path.startsWith("temp_")) {
            temporaryBitmaps.remove(path)
            return true
        }

        return try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("ImageManager", "Error deleting image file: $path", e)
            false
        }
    }
}
