package com.example.itemanagerv2.data.local.repository

import com.example.itemanagerv2.data.local.dao.*
import com.example.itemanagerv2.data.local.entity.*
import com.example.itemanagerv2.data.local.model.ItemCardDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepository @Inject constructor(
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
        while (hasMoreItems) {
            val newItems = itemDao.getPaginatedItems(pageSize, currentPage * pageSize)
            if (newItems.isNotEmpty()) {
                val itemIds = newItems.map { it.id }
                val categories = itemCategoryDao.getCategoriesForItems(itemIds)
                val allImages = imageDao.getImagesForItems(itemIds)
                val allAttributes = itemAttributeValueDao.getAttributesForItems(itemIds)

                val newItemCardDetails = newItems.map { item ->
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

                emit(newItemCardDetails)
                currentPage++
                hasMoreItems = newItems.size == pageSize
            } else {
                hasMoreItems = false
            }
        }
    }

    suspend fun insertItem(item: Item): Long {
        itemDao.insert(item)
        return item.id.toLong()
    }

    suspend fun insertItemAttributeValue(attributeValue: ItemAttributeValue): Long {
        return itemAttributeValueDao.insertAttributeValue(attributeValue)
    }

    suspend fun insertImage(image: Image): Long {
        return imageDao.insertImage(image)
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
}
