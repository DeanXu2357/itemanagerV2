package com.example.itemanagerv2.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.itemanagerv2.data.local.entity.Image
import com.example.itemanagerv2.data.local.entity.ItemAttributeValue
import com.example.itemanagerv2.data.local.model.ItemCardDetail
import com.example.itemanagerv2.data.local.model.ItemCategoryArg
import com.example.itemanagerv2.ui.theme.BaseTheme
import java.util.Date

@Composable
fun MainPage(
    cardDetails: List<ItemCardDetail>,
    onManualAdd: () -> Unit,
    onScanAdd: () -> Unit,
    onDeleteCard: (ItemCardDetail) -> Unit,
    onItemSelect: (ItemCardDetail) -> Unit
) {
    val gridState = rememberLazyGridState()
    var isFabExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = "Asset Inventory",
                onSearchClick = { /* TODO: 實現搜索功能 */ }
            )
        },
        floatingActionButton = {
            InsertFAB(
                isExpanded = isFabExpanded,
                onExpandedChange = { isFabExpanded = it },
                onManualAdd = onManualAdd,
                onScanAdd = onScanAdd,
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
                        onEdit = { onItemSelect(item) },
                        onDelete = { onDeleteCard(item) }
                    )
                }
            }
        }
    }
}

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
        )
    )

    BaseTheme {
        MainPage(
            cardDetails = previewCardDetails,
            onManualAdd = { },
            onScanAdd = { },
            onDeleteCard = { },
            onItemSelect = { }
        )
    }
}
