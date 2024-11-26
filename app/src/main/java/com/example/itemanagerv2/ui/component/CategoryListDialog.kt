package com.example.itemanagerv2.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlusOne
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.itemanagerv2.data.local.model.ItemCategoryArg

@Composable
fun CategoryListDialog(
    categories: List<ItemCategoryArg>, // 改為接收類別列表作為參數
    onDismissRequest: () -> Unit,
    onCategorySelected: (ItemCategoryArg) -> Unit,
    onAddCategory: (String) -> Unit
) {
    val modifier = Modifier.Companion.fillMaxWidth()
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = modifier.padding(16.dp)
            ) {
                Text(
                    text = "Item Categories",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Companion.Center
                )

                LazyColumn(
                    modifier = modifier.weight(1f)
                ) {
                    itemsIndexed(
                        items = categories,
                        key = { _, category -> category }
                    ) { _, category ->
                        CategoryItem(
                            category = category,
                            onClick = {
                                onCategorySelected(category)
                                onDismissRequest()
                            },
                            onEdit = { /* TODO: 實作編輯類別功能 */ },
                            onDelete = { /* TODO: 實作刪除類別功能 */ }
                        )
                    }

                    item {
                        Surface(
                            modifier = modifier,
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            AddCategoryItem(
                                modifier = modifier,
                                onAddCategory = onAddCategory,
                                onCancel = { },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddCategoryItem(
    modifier: Modifier,
    onAddCategory: (String) -> Unit,
    onCancel: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var categoryName by remember { mutableStateOf("") }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface
    ) {
        if (!isEditing) {
            val editingModifier = Modifier.Companion
            Row(
                modifier = editingModifier
                    .clickable { isEditing = true }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Companion.CenterVertically
            ) {
                Icon(
                    modifier = editingModifier,
                    imageVector = Icons.Default.PlusOne,
                    contentDescription = "Add item category"
                )
                Spacer(modifier = editingModifier.width(8.dp))
                Text(
                    modifier = editingModifier,
                    text = "Add Category",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            val inputModifier = Modifier.Companion
            Row(
                modifier = inputModifier,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Companion.CenterVertically
            ) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    modifier = inputModifier.weight(1f),
                    placeholder = { Text("Text here") },
                    singleLine = true
                )

                Row {
                    IconButton(
                        onClick = {
                            if (categoryName.isNotBlank()) {
                                onAddCategory(categoryName)
                                categoryName = ""
                            }
                            isEditing = false
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = "Save")
                    }
                    IconButton(
                        onClick = {
                            isEditing = false
                            categoryName = ""
                            onCancel()
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Cancel, contentDescription = "Cancel")
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: ItemCategoryArg, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.Companion.weight(1f)
            )

            Row {
//                IconButton(onClick = onEdit) {
//                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
//                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}