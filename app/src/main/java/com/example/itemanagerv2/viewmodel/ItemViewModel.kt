package com.example.itemanagerv2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.itemanagerv2.data.local.dao.ItemDao
import com.example.itemanagerv2.data.local.entity.Image
import com.example.itemanagerv2.data.local.entity.Item
import com.example.itemanagerv2.data.manager.ImageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import android.graphics.Bitmap
import com.example.itemanagerv2.data.local.dao.ImageDao
import com.example.itemanagerv2.data.local.dao.ItemAttributeValueDao
import com.example.itemanagerv2.data.local.dao.ItemCategoryDao
import com.example.itemanagerv2.data.local.model.ItemCardDetail

@HiltViewModel
class ItemViewModel @Inject constructor(
    private val itemDao: ItemDao,
    private val imageDao: ImageDao,
    private val itemCategoryDao: ItemCategoryDao,
    private val itemAttributeValueDao: ItemAttributeValueDao,
    private val imageManager: ImageManager
) : ViewModel() {
    private val _itemCardDetails = MutableStateFlow<List<ItemCardDetail>>(emptyList())
    val itemCardDetails: StateFlow<List<ItemCardDetail>> = _itemCardDetails

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var currentPage = 0
    private val pageSize = 20
    private var hasMoreItems = true

    init {
        loadMoreItems()
    }

    private suspend fun getItemCardDetail(item: Item): ItemCardDetail {
        val category = itemCategoryDao.getCategoryById(item.categoryId)
        val codeImage = item.codeImageId?.let { imageDao.getImageById(it) }
        val coverImage = item.coverImageId?.let { imageDao.getImageById(it) }
        val images = imageDao.getImagesForItems(listOf(item.id))
        val attributes = itemAttributeValueDao.getAttributesForItems(listOf(item.id))

        return ItemCardDetail(
            id = item.id,
            name = item.name,
            categoryId = item.categoryId,
            codeType = item.codeType,
            codeContent = item.codeContent,
            codeImageId = item.codeImageId,
            coverImageId = item.coverImageId,
            createdAt = item.createdAt,
            updatedAt = item.updatedAt,
            category = category!!,
            codeImage = codeImage,
            coverImage = coverImage,
            images = images,
            attributes = attributes
        )
    }

    fun loadMoreItems() {
        if (_isLoading.value || !hasMoreItems) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val newItems = itemDao.getPaginatedItems(pageSize, currentPage * pageSize)
                if (newItems.isNotEmpty()) {
                    val itemIds = newItems.map { it.id }

                    // batch get categories
                    val categories = itemCategoryDao.getCategoriesForItems(itemIds)

                    // batch get images
                    val allImages = imageDao.getImagesForItems(itemIds)

                    // batch get item attributes
                    val allAttributes = itemAttributeValueDao.getAttributesForItems(itemIds)

                    val newItemCardDetail = mutableListOf<ItemCardDetail>()

                    for (item in newItems) {
                        val cardDetail = ItemCardDetail(
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
                        newItemCardDetail.add(cardDetail)
                    }

                    _itemCardDetails.value = _itemCardDetails.value + newItemCardDetail
                    currentPage++
                    hasMoreItems = newItems.size == pageSize
                } else {
                    hasMoreItems = false
                }
            } catch (e: Exception) {
                _error.value = "Error loading items: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addItem(item: Item) {
        viewModelScope.launch {
            try {
                itemDao.insert(item)
                refreshItems()
            } catch (e: Exception) {
                _error.value = "Error adding item: ${e.message}"
            }
        }
    }

    fun updateItem(item: Item) {
        viewModelScope.launch {
            try {
                itemDao.updateItem(item)
                refreshItems()
            } catch (e: Exception) {
                _error.value = "Error updating item: ${e.message}"
            }
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            try {
                itemDao.deleteItem(item)
                refreshItems()
            } catch (e: Exception) {
                _error.value = "Error deleting item: ${e.message}"
            }
        }
    }

    fun getItemById(id: Int) {
        viewModelScope.launch {
            try {
                val item = itemDao.getItemById(id)
                // Handle the retrieved item as needed
            } catch (e: Exception) {
                _error.value = "Error retrieving item: ${e.message}"
            }
        }
    }

    private fun refreshItems() {
        currentPage = 0
        hasMoreItems = true
        _itemCardDetails.value =
            emptyList() // TODO: Clear the list of items and get first page data
        loadMoreItems()
    }

    fun getTotalItemCount(callback: (Int) -> Unit) {
        viewModelScope.launch {
            try {
                val count = itemDao.getTotalItemCount()
                callback(count)
            } catch (e: Exception) {
                _error.value = "Error getting item count: ${e.message}"
            }
        }
    }

    fun addImageToItem(itemId: Int, bitmap: Bitmap) {
        viewModelScope.launch {
            val filePath = imageManager.saveImage(bitmap)
            val image = Image(
                filePath = filePath,
                itemId = itemId,
                order = 0, // TODO: Set the correct order
                content = null,
                createdAt = Date(),
                updatedAt = Date()
            )

            var id = imageDao.insertImage(image)
        }
    }
}

