package com.example.itemanagerv2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.itemanagerv2.data.local.dao.ItemDao
import com.example.itemanagerv2.data.local.entity.Item
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemViewModel @Inject constructor(private val itemDao: ItemDao) : ViewModel() {
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items

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

    fun loadMoreItems() {
        if (_isLoading.value || !hasMoreItems) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val newItems = itemDao.getPaginatedItems(pageSize, currentPage * pageSize)
                if (newItems.isNotEmpty()) {
                    _items.value = _items.value + newItems
                    currentPage++
                    // Check if we've loaded all items
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
        _items.value = emptyList()
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
}
