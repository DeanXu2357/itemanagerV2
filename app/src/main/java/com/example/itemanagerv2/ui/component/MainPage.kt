package com.example.itemanagerv2.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.itemanagerv2.data.local.model.ItemCardDetail

@Composable
fun MainPage(
    cardDetails: List<ItemCardDetail>,
    isLoading: Boolean,
    selectedItem: Int,
    onSelectedItemChange: (Int) -> Unit,
    onLoadMore: () -> Unit,
    onEditCard: (ItemCardDetail) -> Unit
) {
    val gridState = rememberLazyGridState()

    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = "My Items",
                onSearchClick = { /* TODO: 實現搜索功能 */ })
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = selectedItem == 0,
                    onClick = { onSelectedItemChange(0) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = selectedItem == 1,
                    onClick = { onSelectedItemChange(1) }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: 新增物品 */ }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Item")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                modifier = Modifier.Companion.fillMaxSize()
            ) {
                items(cardDetails) { item ->
                    ItemCard(
                        cardDetail = item,
                        onEdit = { onEditCard(item) },
                        onCopy = { /* TODO: 實現複製功能 */ },
                        onDelete = { /* TODO: 實現刪除功能 */ }
                    )
                }
                item(key = "loading_indicator") {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.Companion
                                .fillMaxWidth()
                                .height(100.dp)
                                .padding(16.dp),
                            contentAlignment = Alignment.Companion.Center
                        ) { CircularProgressIndicator() }
                    }
                }
            }
        }
    }
}