package com.example.itemanagerv2.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.itemanagerv2.data.local.entity.CategoryAttribute
import com.example.itemanagerv2.data.local.model.ItemCategoryArg

@Composable
fun CategoryListPage(
    categories: List<ItemCategoryArg>,
    categoryAttributes: List<CategoryAttribute>,
    onAddCategory: (String) -> Unit,
    onEditCategory: (ItemCategoryArg) -> Unit,
    onDeleteCategory: (Int) -> Unit,
    onLoadCategoryAttributes: (Int) -> Unit,
    onDeleteAttribute: (Int) -> Unit, // 新增callback
) {
    var selectedCategory by remember { mutableStateOf<ItemCategoryArg?>(null) }
    var expandedAttributeId by remember { mutableStateOf<Int?>(null) }
    var showAttributeListDialog by remember { mutableStateOf(false) }

    if (showAttributeListDialog && selectedCategory != null) {
        AttributeListDialog(
            category = selectedCategory!!,
            attributes = categoryAttributes,
            onDismiss = { selectedCategory = null },
            onDeleteAttribute = onDeleteAttribute,
            expandedAttributeId = expandedAttributeId,
            onToggleExpand = { id ->
                expandedAttributeId = if (expandedAttributeId == id) null else id
            }
        )
    }

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
                    onClick = {
                        onLoadCategoryAttributes(category.id)
                        selectedCategory = category
                        showAttributeListDialog = true
                    },
//                    onEdit = { onEditCategory(category) },
                    onEdit = {},
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

@Composable
private fun AttributeListDialog(
    category: ItemCategoryArg,
    attributes: List<CategoryAttribute>, // 新增參數
    onDismiss: () -> Unit,
    onDeleteAttribute: (Int) -> Unit,
    expandedAttributeId: Int?,
    onToggleExpand: (Int) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "${category.name} Attributes",
                    style = MaterialTheme.typography.titleLarge
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = attributes,
                        key = { _, attribute -> attribute.id }
                    ) { index, attribute ->
                        AttributeListItem(
                            attribute = attribute,
                            isExpanded = attribute.id == expandedAttributeId,
                            onToggleExpand = { onToggleExpand(attribute.id) },
                            onDelete = { onDeleteAttribute(attribute.id) }
                        )
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun AttributeListItem(
    attribute: CategoryAttribute,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onToggleExpand)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = attribute.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (attribute.isEditable) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Value type: ${attribute.valueType}")
                    Text("Required: ${if (attribute.isRequired) "yes" else "no"}")
                    Text("Default: ${attribute.defaultValue}")
                }
            }
        }
    }
}
