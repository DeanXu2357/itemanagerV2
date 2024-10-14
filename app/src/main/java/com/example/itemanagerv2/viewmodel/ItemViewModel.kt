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

    private var currentPage = 0
    private val pageSize = 20

    init {
        loadMoreItems()
    }

    fun loadMoreItems() {
        viewModelScope.launch {
            _isLoading.value = true
            val newItems = itemDao.getPaginatedItems(pageSize, currentPage * pageSize)
            _items.value = _items.value + newItems
            currentPage++
            _isLoading.value = false
        }
    }
}
