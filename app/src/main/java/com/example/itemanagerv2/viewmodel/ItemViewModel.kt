// File: app/src/main/java/com/example/itemanagerv2/viewmodel/ItemViewModel.kt

package com.example.itemanagerv2.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.itemanagerv2.data.local.entity.Image
import com.example.itemanagerv2.data.local.entity.Item
import com.example.itemanagerv2.data.local.entity.ItemAttributeValue
import com.example.itemanagerv2.data.local.model.ItemCardDetail
import com.example.itemanagerv2.data.manager.ImageManager
import com.example.itemanagerv2.data.local.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ItemViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val imageManager: ImageManager
) : ViewModel() {
    private val _itemCardDetails = MutableStateFlow<List<ItemCardDetail>>(emptyList())
    val itemCardDetails: StateFlow<List<ItemCardDetail>> = _itemCardDetails

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadMoreItems()
    }

    fun loadMoreItems() {
        if (_isLoading.value) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                itemRepository.getItemCardDetails()
                    .collect { newItems ->
                        _itemCardDetails.value = _itemCardDetails.value + newItems
                    }
            } catch (e: Exception) {
                _error.value = "Error loading items: ${e.message}"
                Log.e("ItemViewModel", "Error loading items", e)
            } finally {
                _isLoading.value = false
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

                refreshItems()
            } catch (e: Exception) {
                _error.value = "Error adding item: ${e.message}"
                Log.e("ItemViewModel", "Error adding item", e)
            }
        }
    }

    private fun refreshItems() {
        viewModelScope.launch {
            _itemCardDetails.value = emptyList()
            itemRepository.resetPagination()
            loadMoreItems()
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
                    filePath = filePath,
                    itemId = itemId,
                    order = 0, // TODO: Set the correct order
                    content = null,
                    createdAt = Date(),
                    updatedAt = Date()
                )
                itemRepository.insertImage(image)
            } catch (e: Exception) {
                _error.value = "Error adding image: ${e.message}"
                Log.e("ItemViewModel", "Error adding image", e)
            }
        }
    }
}
