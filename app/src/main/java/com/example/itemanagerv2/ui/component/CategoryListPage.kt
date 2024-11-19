package com.example.itemanagerv2.ui.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.itemanagerv2.data.local.model.ItemCategoryArg

@Composable
fun CategoryListPage(
    categories: List<ItemCategoryArg>,
    onAddCategory: (String) -> Unit,
    onEditCategory: (ItemCategoryArg) -> Unit,
    onDeleteCategory: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            CustomTopAppBar(title = "Categories", onSearchClick = { /* TODO: 實現搜索功能 */ })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            itemsIndexed(
                items = categories,
                key = { _, category -> category }
            ) { _, category ->
                CategoryItem(
                    category = category,
                    onClick = { /* TODO: 實現點擊詳情功能 */ },
                    onEdit = { onEditCategory(category) },
                    onDelete = { onDeleteCategory(category.id) }
                )
            }
            item {
                AddCategoryItem(
                    modifier = Modifier.fillMaxWidth(),
                    onAddCategory = onAddCategory,
                    onCancel = {}
                )
            }
        }
    }
}
