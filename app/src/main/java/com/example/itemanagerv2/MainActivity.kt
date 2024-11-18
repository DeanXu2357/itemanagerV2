package com.example.itemanagerv2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.itemanagerv2.data.local.model.ItemCardDetail
import com.example.itemanagerv2.ui.component.ItemEditDialog
import com.example.itemanagerv2.ui.component.MainPage
import com.example.itemanagerv2.ui.theme.BaseTheme
import com.example.itemanagerv2.viewmodel.ItemViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val itemViewModel: ItemViewModel by viewModels()

    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent { BaseTheme { MainContent(itemViewModel) } }
    }
}

@ExperimentalMaterial3Api
@Composable
fun MainContent(itemViewModel: ItemViewModel) {
    var selectedItem by remember { mutableStateOf(0) }
    val cardDetails by itemViewModel.itemCardDetails.collectAsStateWithLifecycle()
    val isLoading by itemViewModel.isLoading.collectAsStateWithLifecycle(initialValue = false)
    val categories by itemViewModel.categories.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
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
                    Log.d("ItemCardDetails", "item card details: $cardDetails")
                }
            }
    }

    MainPage(
        cardDetails = cardDetails,
        categories = categories, // 傳入類別列表
        isLoading = isLoading,
        selectedItem = selectedItem,
        onSelectedItemChange = { selectedItem = it },
        onLoadMore = { itemViewModel.loadMoreItems() },
        onEditCard = { cardDetail ->
            itemCardDetailToEdit = cardDetail
            showEditDialog = true
        },
        onManualAdd = { showAddDialog = true },
        onScanAdd = {},
        onDeleteCard = { cardDetail ->
            itemViewModel.deleteItem(cardDetail)
            itemViewModel.refreshItems()
        },
        onAddCategory = { categoryName ->
            itemViewModel.addNewCategory(categoryName)
        }
    )

    if (showEditDialog && itemCardDetailToEdit != null) {
        itemViewModel.ensureCategoriesLoaded()
        ItemEditDialog(
            item = itemCardDetailToEdit!!,
            categories,
            onDismiss = {
                showEditDialog = false
                itemCardDetailToEdit = null
            },
            onSave = { itemCardDetail ->
                itemViewModel.updateItemCardDetail(itemCardDetail)
                itemViewModel.refreshItems()
                showEditDialog = false
                itemCardDetailToEdit = null
            },
            onAddImage = { /*TODO: handle add image*/ }
        ) {}
    }

    if (showAddDialog) {
        itemViewModel.ensureCategoriesLoaded()
        val emptyItem =
            ItemCardDetail(
                id = 0,
                name = "",
                categoryId = 0,
                codeType = null,
                codeContent = null,
                codeImageId = null,
                coverImageId = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                category = null,
                codeImage = null,
                coverImage = null,
                images = emptyList(),
                attributes = emptyList()
            )
        ItemEditDialog(
            item = emptyItem,
            categories,
            onDismiss = { showAddDialog = false },
            onSave = { newItem ->
                itemViewModel.addNewItem(newItem)
                itemViewModel.refreshItems()
                showAddDialog = false
            },
            onAddImage = { /*TODO:  handle add image*/ }
        ) {}
    }
}
