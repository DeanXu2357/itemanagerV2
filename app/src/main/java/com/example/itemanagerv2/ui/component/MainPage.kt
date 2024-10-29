package com.example.itemanagerv2.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.itemanagerv2.data.local.entity.Image
import com.example.itemanagerv2.data.local.entity.ItemAttributeValue
import com.example.itemanagerv2.data.local.entity.ItemCategory
import com.example.itemanagerv2.data.local.model.ItemCardDetail
import com.example.itemanagerv2.ui.theme.BaseTheme
import java.util.Date

@Composable
fun MainPage(
    cardDetails: List<ItemCardDetail>,
    isLoading: Boolean,
    selectedItem: Int,
    onSelectedItemChange: (Int) -> Unit,
    onLoadMore: () -> Unit,
    onEditCard: (ItemCardDetail) -> Unit,
    onManualAdd: () -> Unit,
    onScanAdd: () -> Unit,
    onDeleteCard: (ItemCardDetail) -> Unit
) {
    val gridState = rememberLazyGridState()
    var isFabExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CustomTopAppBar(title = "Asset Inventory", onSearchClick = { /* TODO: 實現搜索功能 */ })
        },
        //        bottomBar = {
        //            NavigationBar {
        //                NavigationBarItem(
        //                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
        //                    label = { Text("Home") },
        //                    selected = selectedItem == 0,
        //                    onClick = { onSelectedItemChange(0) }
        //                )
        //                NavigationBarItem(
        //                    icon = { Icon(Icons.Filled.Settings, contentDescription =
        // "Settings") },
        //                    label = { Text("Settings") },
        //                    selected = selectedItem == 1,
        //                    onClick = { onSelectedItemChange(1) }
        //                )
        //            }
        //        },
        floatingActionButton = {
            InsertFAB(
                isExpanded = isFabExpanded,
                onExpandedChange = { isFabExpanded = it },
                onManualAdd = onManualAdd,
                onScanAdd = onScanAdd
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(cardDetails) { item ->
                    ItemCard(
                        cardDetail = item,
                        onEdit = { onEditCard(item) },
                        onCopy = { /* TODO: 實現複製功能 */ },
                        onDelete = { onDeleteCard(item) }
                    )
                }
                item(key = "loading_indicator") {
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPagePreview() {
    val itemCategoryDummy = ItemCategory(
        id = 1,
        name = "Sample Category",
        createdAt = Date(),
        updatedAt = Date()
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
            isLoading = false,
            selectedItem = 0,
            onSelectedItemChange = { },
            onLoadMore = { },
            onEditCard = { },
            onManualAdd = { },
            onScanAdd = { },
            onDeleteCard = { },
        )
    }
}
