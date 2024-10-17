package com.example.itemanagerv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.itemanagerv2.data.local.entity.Image
import com.example.itemanagerv2.data.local.entity.Item
import com.example.itemanagerv2.data.local.entity.ItemAttributeValue
import com.example.itemanagerv2.data.local.entity.ItemCategory
import com.example.itemanagerv2.data.local.model.ItemCardDetail
import com.example.itemanagerv2.ui.component.ItemEditDialog
import com.example.itemanagerv2.ui.component.MainPage
import com.example.itemanagerv2.ui.theme.BaseTheme
import com.example.itemanagerv2.viewmodel.ItemViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val itemViewModel: ItemViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BaseTheme {
                MainContent(itemViewModel)
            }
        }
    }
}

@Composable
fun MainContent(itemViewModel: ItemViewModel) {
    var selectedItem by remember { mutableStateOf(0) }
    val cardDetails by itemViewModel.itemCardDetails.collectAsStateWithLifecycle()
    val isLoading by itemViewModel.isLoading.collectAsStateWithLifecycle(initialValue = false)
    val gridState = rememberLazyGridState()
    var showEditDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<Item?>(null) }
    var itemCardDetailToEdit by remember { mutableStateOf<ItemCardDetail?>(null) }

    LaunchedEffect(gridState) {
        snapshotFlow {
            val layoutInfo = gridState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            lastVisibleItemIndex >
                    (totalItemsNumber - 5) // Start loading when 5 items away from the end
        }
            .collect { shouldLoadMore ->
                if (shouldLoadMore && !isLoading) {
                    itemViewModel.loadMoreItems()
                }
            }
    }

    MainPage(
        cardDetails = cardDetails,
        isLoading = isLoading,
        selectedItem = selectedItem,
        onSelectedItemChange = { selectedItem = it },
        onLoadMore = { itemViewModel.loadMoreItems() },
        onEditCard = { cardDetail ->
            itemCardDetailToEdit = cardDetail
            showEditDialog = true
        }
    )

    if (showEditDialog && itemToEdit != null) {
        ItemEditDialog(
            item = itemCardDetailToEdit!!,
            onDismiss = {
                showEditDialog = false
                itemToEdit = null
            },
            {/*TODO: on save*/ },
            {/*TODO: on delete*/ }
        ) { }
    }
}

@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
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
            codeContent = "Sample content",
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
            categoryId = 3,
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
            categoryId = 4,
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
            onSelectedItemChange = {},
            onLoadMore = {},
            onEditCard = {}
        )
    }
}
