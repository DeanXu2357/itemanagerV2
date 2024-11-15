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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlusOne
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onCategorySelected: (ItemCategoryArg) -> Unit
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
                        onCategorySelected(category)
                        showCategoryDialog = false
                    },
                    onAddCategory = { /* TODO: 實現新增類別功能 */ })
            }
        }
    }
}

@Composable
fun CategoryListDialog(
    categories: List<ItemCategoryArg>, // 改為接收類別列表作為參數
    onDismissRequest: () -> Unit,
    onCategorySelected: (ItemCategoryArg) -> Unit,
    onAddCategory: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Item Categories",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f)
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAddCategory() },
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlusOne,
                                    contentDescription = "Add a new category"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Add a new category",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
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
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "編輯")
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "刪除")
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
            onCategorySelected = {}
        )
    }
}
