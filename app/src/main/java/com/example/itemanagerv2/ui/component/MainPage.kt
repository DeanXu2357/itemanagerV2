@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.itemanagerv2.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlusOne
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.itemanagerv2.data.local.entity.Image
import com.example.itemanagerv2.data.local.entity.ItemAttributeValue
import com.example.itemanagerv2.data.local.model.ItemCardDetail
import com.example.itemanagerv2.data.local.model.ItemCategoryArg
import com.example.itemanagerv2.ui.theme.BaseTheme
import java.util.Date

@ExperimentalMaterial3Api
@Composable
fun MainPage(
    cardDetails: List<ItemCardDetail>,
    categories: List<ItemCategoryArg>,
    isLoading: Boolean,
    selectedItem: Int,
    onSelectedItemChange: (Int) -> Unit,
    onLoadMore: () -> Unit,
    onEditCard: (ItemCardDetail) -> Unit,
    onManualAdd: () -> Unit,
    onScanAdd: () -> Unit,
    onDeleteCard: (ItemCardDetail) -> Unit,
    onAddCategory: (String) -> Unit
) {
    val gridState = rememberLazyGridState()
    var isFabExpanded by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    Scaffold(topBar = {
        CustomTopAppBar(title = "Asset Inventory", onSearchClick = { /* TODO: 實現搜索功能 */ })
    }, floatingActionButton = {
        InsertFAB(isExpanded = isFabExpanded,
            onExpandedChange = { isFabExpanded = it },
            onManualAdd = onManualAdd,
            onScanAdd = onScanAdd,
            onCategoryManage = { showCategoryDialog = true }
        )
    }) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), state = gridState, modifier = Modifier.fillMaxSize()
            ) {
                items(cardDetails) { item ->
                    ItemCard(cardDetail = item,
                        onEdit = { onEditCard(item) },
                        onCopy = { /* TODO: 實現複製功能 */ },
                        onDelete = { onDeleteCard(item) })
                }
            }

            if (showCategoryDialog) {
                CategoryListDialog(
                    categories = categories,
                    onDismissRequest = { showCategoryDialog = false },
                    onCategorySelected = { category ->
                        showCategoryDialog = false
                    },
                    onAddCategory = onAddCategory
                )
            }
        }
    }
}

@Composable
fun CategoryListDialog(
    categories: List<ItemCategoryArg>, // 改為接收類別列表作為參數
    onDismissRequest: () -> Unit,
    onCategorySelected: (ItemCategoryArg) -> Unit,
    onAddCategory: (String) -> Unit
) {
    val modifier = Modifier.fillMaxWidth()
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
                    textAlign = TextAlign.Center
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
            val editingModifier = Modifier
            Row(
                modifier = editingModifier
                    .clickable { isEditing = true }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
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
            val inputModifier = Modifier
            Row(
                modifier = inputModifier,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            Row {
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun MainPagePreview() {
    val itemCategoryDummy = ItemCategoryArg(
        id = 1,
        name = "Sample Category",
    )
    val imageDummy = Image(
        id = 1,
        filePath = "https://example.com/image1.jpg",
        itemId = 1,
        order = 0,
        content = "Sample image 1",
        createdAt = Date(),
        updatedAt = Date()
    )
    val attributeDummy = ItemAttributeValue(
        id = 1,
        value = "Sample Value",
        itemId = 1,
        createdAt = Date(),
        updatedAt = Date(),
        attributeId = 1
    )
    val previewCardDetails = listOf(
        ItemCardDetail(
            id = 1,
            name = "Sample Item 1",
            coverImage = null,
            categoryId = 1,
            codeType = "QR",
            codeContent = "Sample content 1",
            codeImageId = 0,
            coverImageId = 0,
            createdAt = 0,
            updatedAt = 0,
            category = itemCategoryDummy,
            codeImage = imageDummy,
            images = listOf(imageDummy),
            attributes = listOf(attributeDummy),
        ),
        ItemCardDetail(
            id = 2,
            name = "Sample Item 2",
            coverImage = null,
            categoryId = 2,
            codeType = "Barcode",
            codeContent = "Sample content 2",
            codeImageId = 0,
            coverImageId = 0,
            createdAt = 0,
            updatedAt = 0,
            category = itemCategoryDummy,
            codeImage = imageDummy,
            images = listOf(imageDummy),
            attributes = listOf(attributeDummy),
        ),
        ItemCardDetail(
            id = 3,
            name = "Sample Item 3",
            coverImage = null,
            categoryId = 1,
            codeType = "QR",
            codeContent = "Sample content 3",
            codeImageId = 0,
            coverImageId = 0,
            createdAt = 0,
            updatedAt = 0,
            category = itemCategoryDummy,
            codeImage = imageDummy,
            images = listOf(imageDummy),
            attributes = listOf(attributeDummy),
        ),
        ItemCardDetail(
            id = 4,
            name = "Sample Item 4",
            coverImage = null,
            categoryId = 2,
            codeType = "Barcode",
            codeContent = "Sample content 4",
            codeImageId = 0,
            coverImageId = 0,
            createdAt = 0,
            updatedAt = 0,
            category = itemCategoryDummy,
            codeImage = imageDummy,
            images = listOf(imageDummy),
            attributes = listOf(attributeDummy),
        )
    )


    BaseTheme {
        MainPage(
            cardDetails = previewCardDetails,
            categories = listOf(itemCategoryDummy),
            isLoading = false,
            selectedItem = 0,
            onSelectedItemChange = { },
            onLoadMore = { },
            onEditCard = { },
            onManualAdd = { },
            onScanAdd = { },
            onDeleteCard = { },
            onAddCategory = { }
        )
    }
}
