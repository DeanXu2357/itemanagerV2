package com.example.itemanagerv2.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import com.example.itemanagerv2.data.local.entity.ItemCategory
import com.example.itemanagerv2.data.local.model.ItemCardDetail
import com.example.itemanagerv2.ui.theme.BaseTheme
import java.util.Date

@ExperimentalMaterial3Api
@Composable
fun MainPage(
    cardDetails: List<ItemCardDetail>,
    categories: List<ItemCategory>, // 新增類別列表參數
    isLoading: Boolean,
    selectedItem: Int,
    onSelectedItemChange: (Int) -> Unit,
    onLoadMore: () -> Unit,
    onEditCard: (ItemCardDetail) -> Unit,
    onManualAdd: () -> Unit,
    onScanAdd: () -> Unit,
    onDeleteCard: (ItemCardDetail) -> Unit,
    onCategorySelected: (ItemCategory) -> Unit // 新增類別選擇回調
) {
    val gridState = rememberLazyGridState()
    var isFabExpanded by remember { mutableStateOf(false) }
    // 新增 dialog 控制狀態
    var showCategoryDialog by remember { mutableStateOf(false) }

    Scaffold(topBar = {
        CustomTopAppBar(title = "Asset Inventory", onSearchClick = { /* TODO: 實現搜索功能 */ })
    }, floatingActionButton = {
        InsertFAB(isExpanded = isFabExpanded,
            onExpandedChange = { isFabExpanded = it },
            onManualAdd = onManualAdd,
            onScanAdd = onScanAdd,
            onCategoryManage = { showCategoryDialog = true } // 新增類別管理點擊處理
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

            // 新增 CategoryDialog
            if (showCategoryDialog) {
                CategoryListDialog(categories = categories, // 傳入類別列表
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
    categories: List<ItemCategory>, // 改為接收類別列表作為參數
    onDismissRequest: () -> Unit,
    onCategorySelected: (ItemCategory) -> Unit,
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
                    items(
                        itemContent = { category: ItemCategory ->
                            CategoryItem(
                                category = category,
                                onClick = {
                                    onCategorySelected(category)
                                    onDismissRequest()
                                },
                                onEdit = { /* TODO */ },
                                onDelete = { /* TODO */ }
                            )
                            ) {}
                        }

                                Surface (
                                modifier = Modifier
                                    .fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlusOne,
                                contentDescription = "Add Category"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("新增類別")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun CategoryItem(
        category: ItemCategory, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit
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

    @Preview(showBackground = true)
    @Composable
    fun MainPagePreview() {
        val itemCategoryDummy =
            ItemCategory(id = 1, name = "Sample Category", createdAt = Date(), updatedAt = Date())
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
            ), ItemCardDetail(
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
            ), ItemCardDetail(
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
            ), ItemCardDetail(
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
            MainPage(cardDetails = previewCardDetails,
                categories = listOf(itemCategoryDummy), // 傳入類別列表
                isLoading = false,
                selectedItem = 0,
                onSelectedItemChange = {},
                onLoadMore = {},
                onEditCard = {},
                onManualAdd = {},
                onScanAdd = {},
                onDeleteCard = {},
                onCategorySelected = {} // 新增類別選擇回調
            )
        }
    }
