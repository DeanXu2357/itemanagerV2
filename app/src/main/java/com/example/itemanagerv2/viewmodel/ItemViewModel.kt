package com.example.itemanagerv2.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.itemanagerv2.data.local.entity.Image
import com.example.itemanagerv2.data.local.entity.Item
import com.example.itemanagerv2.data.local.entity.ItemAttributeValue
import com.example.itemanagerv2.data.local.model.ItemCardDetail
import com.example.itemanagerv2.data.local.model.ItemCategoryArg
import com.example.itemanagerv2.data.local.model.toNavArg
import com.example.itemanagerv2.data.local.repository.ItemRepository
import com.example.itemanagerv2.data.manager.ImageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ItemViewModel
@Inject
constructor(private val itemRepository: ItemRepository, private val imageManager: ImageManager) :
    ViewModel() {
    private val _itemCardDetails = MutableStateFlow<List<ItemCardDetail>>(emptyList())
    val itemCardDetails: StateFlow<List<ItemCardDetail>> = _itemCardDetails.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _categories = MutableStateFlow<List<ItemCategoryArg>>(emptyList())
    val categories: StateFlow<List<ItemCategoryArg>> = _categories.asStateFlow()

    private var hasLoadedCategories = false

    private val _deleteStatus = MutableStateFlow<DeleteStatus>(DeleteStatus.Idle)
    val deleteStatus: StateFlow<DeleteStatus> = _deleteStatus.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadMoreItems()
        loadCategories()
    }

    sealed class DeleteStatus {
        object Idle : DeleteStatus()
        object Loading : DeleteStatus()
        object Success : DeleteStatus()
        data class Error(val message: String) : DeleteStatus()
    }

    fun loadMoreItems() {
        if (_isLoading.value) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("ItemViewModel", "Start loading items")

                itemRepository.getItemCardDetails().collect { newItems ->
                    Log.d("ItemViewModel", "Get new items, counts: ${newItems.size}")
                    _itemCardDetails.value = newItems
                }
            } catch (e: Exception) {
                _error.value = "Error loading items: ${e.message}"
                Log.e("ItemViewModel", "Loading items failed: ", e)
            } finally {
                _isLoading.value = false
                Log.d("ItemViewModel", "Loading complete，counts: ${_itemCardDetails.value.size}")
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            itemRepository.getAllCategories().collect { categoriesList ->
                _categories.value = categoriesList.map {
                    it.toNavArg()
                }
            }
        }
    }

    fun addNewItem(newItem: ItemCardDetail) {
        viewModelScope.launch {
            try {
                val item = Item(
                    id = 0,
                    name = newItem.name,
                    categoryId = newItem.categoryId,
                    codeType = newItem.codeType,
                    codeContent = newItem.codeContent,
                    codeImageId = newItem.codeImageId,
                    coverImageId = newItem.coverImageId,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                val newItemId = itemRepository.insertItem(item)

                newItem.attributes.forEach { attribute ->
                    itemRepository.insertItemAttributeValue(
                        ItemAttributeValue(
                            id = 0,
                            itemId = newItemId.toInt(),
                            attributeId = attribute.attributeId,
                            value = attribute.value,
                            createdAt = Date(),
                            updatedAt = Date()
                        )
                    )
                }

                newItem.images.forEach { image ->
                    itemRepository.insertImage(
                        Image(
                            id = 0,
                            filePath = image.filePath,
                            itemId = newItemId.toInt(),
                            order = image.order,
                            content = image.content,
                            createdAt = Date(),
                            updatedAt = Date()
                        )
                    )
                }
            } catch (e: Exception) {
                _error.value = "Error adding item: ${e.message}"
                Log.e("ItemViewModel", "Error adding item", e)
            }
        }
    }

    fun refreshItems() {
        if (_isLoading.value) return

        _isLoading.value = true
        Log.println(Log.INFO, "ItemViewModel", "isLoading: ${_isLoading.value}")

        viewModelScope.launch {
            try {
                delay(500)
                _error.value = null
                _itemCardDetails.value = emptyList()
                itemRepository.resetPagination()

                Log.println(Log.INFO, "ItemViewModel", "Refreshing items")
                itemRepository.getItemCardDetails().collect { newItems ->
                    _itemCardDetails.value = newItems
                }
            } catch (e: Exception) {
                _error.value = "Error refreshing items: ${e.message}"
                Log.e("ItemViewModel", "Error refreshing items", e)
            } finally {
                _isLoading.value = false
                Log.println(Log.INFO, "ItemViewModel", "Items refreshed")
                Log.println(Log.INFO, "ItemViewModel", "isLoading: ${_isLoading.value}")
            }
        }
    }

    fun getTotalItemCount(callback: (Int) -> Unit) {
        viewModelScope.launch {
            try {
                val count = itemRepository.getTotalItemCount()
                callback(count)
            } catch (e: Exception) {
                _error.value = "Error getting item count: ${e.message}"
                Log.e("ItemViewModel", "Error getting item count", e)
            }
        }
    }

    fun addImageToItem(itemId: Int, bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                val filePath = imageManager.saveImage(bitmap)
                val image = Image(
                    filePath = filePath, itemId = itemId, order = 0, // TODO: Set the correct order
                    content = null, createdAt = Date(), updatedAt = Date()
                )
                itemRepository.insertImage(image)
            } catch (e: Exception) {
                _error.value = "Error adding image: ${e.message}"
                Log.e("ItemViewModel", "Error adding image", e)
            }
        }
    }

    fun ensureCategoriesLoaded() {
        if (!hasLoadedCategories) {
            viewModelScope.launch {
                try {
                    itemRepository.getAllCategories().collect { categoriesList ->
                        hasLoadedCategories = true
                    }
                } catch (e: Exception) {
                    _error.value = "Error loading categories: ${e.message}"
                    Log.e("ItemViewModel", "Error loading categories", e)
                }
            }
        }
    }

    fun updateItemCardDetail(updatedItem: ItemCardDetail) {
        viewModelScope.launch {
            try {
                val item = Item(
                    id = updatedItem.id,
                    name = updatedItem.name,
                    categoryId = updatedItem.categoryId,
                    codeType = updatedItem.codeType,
                    codeContent = updatedItem.codeContent,
                    codeImageId = updatedItem.codeImageId,
                    coverImageId = updatedItem.coverImageId,
                    createdAt = updatedItem.createdAt,
                    updatedAt = System.currentTimeMillis()
                )

                itemRepository.updateItem(item)

                // delete all attribute values for the item
                itemRepository.deleteItemAttributeValues(updatedItem.id)

                updatedItem.attributes.forEach { attribute ->
                    itemRepository.insertItemAttributeValue(
                        ItemAttributeValue(
                            id = 0,
                            itemId = updatedItem.id,
                            attributeId = attribute.attributeId,
                            value = attribute.value,
                            createdAt = Date(),
                            updatedAt = Date()
                        )
                    )
                }
            } catch (e: Exception) {
                _error.value = "Error updating the item：${e.message}"
                Log.e("ItemViewModel", "Error updating item", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteItem(itemCardDetail: ItemCardDetail) {
        viewModelScope.launch {
            try {
                _deleteStatus.value = DeleteStatus.Loading
                itemRepository.deleteItemWithRelations(itemCardDetail.id)
                _deleteStatus.value = DeleteStatus.Success
            } catch (e: Exception) {
                // Handle error case
                Log.e("ItemViewModel", "Error deleting item", e)
                _deleteStatus.value = DeleteStatus.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun addNewCategory(categoryName: String) {
        viewModelScope.launch {
            try {
                itemRepository.insertCategory(categoryName)
                loadCategories()
            } catch (e: Exception) {
                _error.value = "Error adding category: ${e.message}"
                Log.e("ItemViewModel", "Error adding category", e)
            }
        }
    }
}
