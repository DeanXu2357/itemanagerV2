package com.example.itemanagerv2.data.local.repository

import android.util.Log
import androidx.room.withTransaction
import com.example.itemanagerv2.data.local.AppDatabase
import com.example.itemanagerv2.data.local.dao.*
import com.example.itemanagerv2.data.local.entity.*
import com.example.itemanagerv2.data.local.model.ItemCardDetail
import com.example.itemanagerv2.data.local.model.toNavArg
import java.io.File
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

@Singleton
class ItemRepository
@Inject
constructor(
    private val database: AppDatabase,
    private val itemDao: ItemDao,
    private val itemCategoryDao: ItemCategoryDao,
    private val categoryAttributeDao: CategoryAttributeDao,
    private val itemAttributeValueDao: ItemAttributeValueDao,
    private val imageDao: ImageDao
) {
    private var currentPage = 0
    private val pageSize = 20

    fun getItemCardDetails(): Flow<List<ItemCardDetail>> = flow {
        var hasMoreItems = true
        val allItems = mutableListOf<ItemCardDetail>()

        while (hasMoreItems) {
            val newItems = itemDao.getPaginatedItems(pageSize, currentPage * pageSize)
            if (newItems.isNotEmpty()) {
                val itemIds = newItems.map { it.id }
                val categoryIds = newItems.map { it.categoryId }
                val categories =
                    itemCategoryDao.getCategoriesForItems(categoryIds).map { it.toNavArg() }
                val allImages = imageDao.getImagesForItems(itemIds)
                val allAttributes = itemAttributeValueDao.getAttributesForItems(itemIds)

                val newItemCardDetails =
                    newItems.map { item ->
                        ItemCardDetail(
                            id = item.id,
                            name = item.name,
                            categoryId = item.categoryId,
                            codeType = item.codeType,
                            codeContent = item.codeContent,
                            codeImageId = item.codeImageId,
                            coverImageId = item.coverImageId,
                            createdAt = item.createdAt,
                            updatedAt = item.updatedAt,
                            category = categories.find { it.id == item.categoryId },
                            codeImage = allImages.find { it.id == item.codeImageId },
                            coverImage = allImages.find { it.id == item.coverImageId },
                            images = allImages.filter { it.itemId == item.id },
                            attributes = allAttributes.filter { it.itemId == item.id }
                        )
                    }

                allItems.addAll(newItemCardDetails)
                emit(allItems.toList())

                currentPage++
                hasMoreItems = newItems.size == pageSize
            } else {
                hasMoreItems = false
            }
        }
    }

    suspend fun insertItemWithAttributes(item: Item, attributes: List<ItemAttributeValue>): Long {
        return database.withTransaction {
            val itemId = itemDao.insert(item)

            // Update attribute values with the new item ID
            attributes.forEach { attribute ->
                itemAttributeValueDao.insertAttributeValue(attribute.copy(itemId = itemId.toInt()))
            }

            itemId
        }
    }

    suspend fun insertItemAttributeValue(attributeValue: ItemAttributeValue): Long {
        return itemAttributeValueDao.insertAttributeValue(attributeValue)
    }

    suspend fun insertImage(image: Image): Long {
        return imageDao.insertImage(image)
    }

    suspend fun deleteImage(image: Image) {
        imageDao.deleteImage(image)
    }

    fun resetPagination() {
        currentPage = 0
    }

    suspend fun getTotalItemCount(): Int {
        return itemDao.getTotalItemCount()
    }

    suspend fun getAllCategories(): Flow<List<ItemCategory>> {
        return itemCategoryDao.getAllCategories()
    }

    suspend fun updateItem(item: Item) {
        itemDao.updateItem(item)
    }

    suspend fun deleteItemWithRelations(itemId: Int) {
        withContext(Dispatchers.IO) {
            database.withTransaction {
                // 1. Delete all attribute values for this item
                itemAttributeValueDao.deleteByItemId(itemId)

                // 2. Delete all images associated with this item
                // First get all image IDs
                val images = imageDao.getImagesByItemId(itemId)
                // Delete actual image files
                images.forEach { image -> deleteImageFile(image.filePath) }
                // Delete image records from database
                imageDao.deleteByItemId(itemId)

                // 3. Finally delete the item itself
                itemDao.deleteById(itemId)
            }
        }
    }

    private fun deleteImageFile(filePath: String) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            Log.e("ItemRepository", "Error deleting image file: $filePath", e)
        }
    }

    suspend fun deleteItemAttributeValues(itemId: Int) {
        itemAttributeValueDao.deleteByItemId(itemId)
    }

    suspend fun insertCategory(name: String): Long {
        val category = ItemCategory(id = 0, name = name, createdAt = Date(), updatedAt = Date())
        return itemCategoryDao.insertCategory(category)
    }

    suspend fun deleteCategory(categoryId: Int) {
        itemCategoryDao.deleteById(categoryId)
    }

    fun getCategoryAttributes(categoryId: Int): Flow<List<CategoryAttribute>> {
        return categoryAttributeDao.getAttributesForCategory(categoryId)
    }

    suspend fun deleteCategoryAttribute(attributeId: Int) {
        categoryAttributeDao.deleteById(attributeId)
    }

    suspend fun insertCategoryAttribute(attribute: CategoryAttribute): Long {
        return categoryAttributeDao.insertAttribute(attribute)
    }
}
