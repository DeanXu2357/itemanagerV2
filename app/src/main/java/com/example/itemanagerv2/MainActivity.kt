package com.example.itemanagerv2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.itemanagerv2.data.local.entity.Item
import com.example.itemanagerv2.data.local.model.ItemCardDetail
import com.example.itemanagerv2.ui.component.ItemEditDialog
import com.example.itemanagerv2.ui.component.MainPage
import com.example.itemanagerv2.ui.theme.BaseTheme
import com.example.itemanagerv2.viewmodel.ItemViewModel
import dagger.hilt.android.AndroidEntryPoint

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
//        itemViewModel.loadMoreItems()
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
        isLoading = isLoading,
        selectedItem = selectedItem,
        onSelectedItemChange = { selectedItem = it },
        onLoadMore = { itemViewModel.loadMoreItems() },
        onEditCard = { cardDetail ->
            itemCardDetailToEdit = cardDetail
            showEditDialog = true
        },
        {},
        {}
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
