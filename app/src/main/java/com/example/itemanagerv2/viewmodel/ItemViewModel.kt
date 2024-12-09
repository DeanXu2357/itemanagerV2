package com.example.itemanagerv2.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.itemanagerv2.data.local.entity.CategoryAttribute
import com.example.itemanagerv2.data.local.entity.Image
import com.example.itemanagerv2.data.local.entity.Item
import com.example.itemanagerv2.data.local.entity.ItemAttributeValue
import com.example.itemanagerv2.data.local.model.ItemCardDetail
import com.example.itemanagerv2.data.local.model.ItemCategoryArg
import com.example.itemanagerv2.data.local.model.toNavArg
import com.example.itemanagerv2.data.local.repository.ItemRepository
import com.example.itemanagerv2.data.manager.ImageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private val _categoryAttributes = MutableStateFlow<List<CategoryAttribute>>(emptyList())
    val categoryAttributes: StateFlow<List<CategoryAttribute>> = _categoryAttributes.asStateFlow()

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
                _categories.value = categoriesList.map { it.toNavArg() }
            }
        }
    }

    fun addNewItem(newItem: ItemCardDetail) {
        viewModelScope.launch {
            try {
                val item =
                    Item(
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

    suspend fun getItemDetails(itemId: Int): ItemCardDetail? {
        return try {
            itemRepository.getItemCardDetails().first().find { it.id == itemId }
        } catch (e: Exception) {
            _error.value = "Error getting item details: ${e.message}"
            Log.e("ItemViewModel", "Error getting item details", e)
            null
        }
    }

    fun addImageToItem(itemId: Int, bitmap: Bitmap, onImageAdded: (ItemCardDetail?) -> Unit) {
        viewModelScope.launch {
            try {
                val filePath = imageManager.saveImage(bitmap)
                val image =
                    Image(
                        filePath = filePath,
                        itemId = itemId,
                        order = 0, // TODO: Set the correct order
                        content = null,
                        createdAt = Date(),
                        updatedAt = Date()
                    )
                val imageId = itemRepository.insertImage(image)
                
                // Update the image with its new ID
                val savedImage = image.copy(id = imageId.toInt())
                
                // Find the current item and update its images list
                val currentItem = _itemCardDetails.value.find { it.id == itemId }
                currentItem?.let { item ->
                    val updatedItem = item.copy(
                        images = item.images + savedImage,
                        // If this is the first image, set it as cover image
                        coverImageId = if (item.coverImageId == null) savedImage.id else item.coverImageId,
                        coverImage = if (item.coverImageId == null) savedImage else item.coverImage
                    )
                    
                    // Update the items list with the modified item
                    _itemCardDetails.value = _itemCardDetails.value.map { 
                        if (it.id == itemId) updatedItem else it 
                    }
                    
                    onImageAdded(updatedItem)
                }
            } catch (e: Exception) {
                _error.value = "Error adding image: ${e.message}"
                Log.e("ItemViewModel", "Error adding image", e)
                onImageAdded(null)
            }
        }
    }

    fun deleteImage(itemId: Int, imageId: Int, isCoverImage: Boolean) {
        viewModelScope.launch {
            try {
                // Delete the image file and database record
                val currentItem = _itemCardDetails.value.find { it.id == itemId }
                currentItem?.let { item ->
                    val imageToDelete = item.images.find { it.id == imageId }
                    imageToDelete?.let {
                        withContext(Dispatchers.IO) {
                            imageManager.deleteImage(it.filePath)
                            itemRepository.deleteImage(it)
                        }
                    }
                    
                    // Update the item with remaining images
                    val remainingImages = item.images.filter { it.id != imageId }
                    val updatedItem = if (isCoverImage && remainingImages.isNotEmpty()) {
                        // If we deleted the cover image and there are remaining images,
                        // set the first remaining image as the cover
                        val newCoverImage = remainingImages.first()
                        item.copy(
                            images = remainingImages,
                            coverImageId = newCoverImage.id,
                            coverImage = newCoverImage
                        )
                    } else if (isCoverImage) {
                        // If we deleted the cover image and there are no remaining images
                        item.copy(
                            images = remainingImages,
                            coverImageId = null,
                            coverImage = null
                        )
                    } else {
                        // If we didn't delete the cover image
                        item.copy(images = remainingImages)
                    }

                    // Update the database if we changed the cover image
                    if (isCoverImage) {
                        val updatedDbItem = Item(
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
                        itemRepository.updateItem(updatedDbItem)
                    }
                    
                    // Update the UI state
                    _itemCardDetails.value = _itemCardDetails.value.map { 
                        if (it.id == itemId) updatedItem else it 
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error deleting image: ${e.message}"
                Log.e("ItemViewModel", "Error deleting image", e)
            }
        }
    }

    fun updateItemCoverImage(itemId: Int, imageId: Int) {
        viewModelScope.launch {
            try {
                // Find the current item
                val currentItem = _itemCardDetails.value.find { it.id == itemId }
                currentItem?.let { item ->
                    // Update the database immediately
                    val updatedItem = Item(
                        id = item.id,
                        name = item.name,
                        categoryId = item.categoryId,
                        codeType = item.codeType,
                        codeContent = item.codeContent,
                        codeImageId = item.codeImageId,
                        coverImageId = imageId,
                        createdAt = item.createdAt,
                        updatedAt = System.currentTimeMillis()
                    )
                    itemRepository.updateItem(updatedItem)

                    // Update the UI state
                    val updatedItemCardDetail = item.copy(
                        coverImageId = imageId,
                        coverImage = item.images.find { it.id == imageId }
                    )
                    _itemCardDetails.value = _itemCardDetails.value.map { 
                        if (it.id == itemId) updatedItemCardDetail else it 
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error updating cover image: ${e.message}"
                Log.e("ItemViewModel", "Error updating cover image", e)
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
                val item =
                    Item(
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
                
                // Update the items list
                _itemCardDetails.value = _itemCardDetails.value.map { 
                    if (it.id == updatedItem.id) updatedItem else it 
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

    fun deleteCategory(categoryId: Int) {
        viewModelScope.launch {
            try {
                itemRepository.deleteCategory(categoryId)
                loadCategories()
            } catch (e: Exception) {
                _error.value = "Error deleting category: ${e.message}"
                Log.e("ItemViewModel", "Error deleting category", e)
            }
        }
    }

    fun loadCategoryAttributes(categoryId: Int) {
        viewModelScope.launch {
            try {
                itemRepository.getCategoryAttributes(categoryId).collect { attributesList ->
                    _categoryAttributes.value = attributesList
                }
            } catch (e: Exception) {
                _error.value = "Error loading category attributes: ${e.message}"
                Log.e("ItemViewModel", "Error loading category attributes", e)
            }
        }
    }

    fun deleteCategoryAttribute(attributeId: Int) {
        viewModelScope.launch {
            try {
                itemRepository.deleteCategoryAttribute(attributeId)
                // 刷新當前屬性列表
                _categoryAttributes.value =
                    _categoryAttributes.value.filter { it.id != attributeId }
            } catch (e: Exception) {
                _error.value = "Error deleting category attribute: ${e.message}"
                Log.e("ItemViewModel", "Error deleting category attribute", e)
            }
        }
    }

    fun addCategoryAttribute(categoryAttribute: CategoryAttribute) {
        viewModelScope.launch {
            try {
                itemRepository.insertCategoryAttribute(categoryAttribute)
                loadCategoryAttributes(categoryAttribute.categoryId)
            } catch (e: Exception) {
                _error.value = "Error adding category attribute: ${e.message}"
                Log.e("ItemViewModel", "Error adding category attribute", e)
            }
        }
    }
}
