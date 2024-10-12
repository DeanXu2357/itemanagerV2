package com.example.itemanagerv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.itemanagerv2.data.local.AppDatabase
import com.example.itemanagerv2.data.local.dao.ItemDao
import com.example.itemanagerv2.data.local.entity.Item
import com.example.itemanagerv2.ui.theme.BaseTheme
import com.example.itemanagerv2.viewmodel.ItemViewModel
import com.example.itemanagerv2.viewmodel.ItemViewModelFactory
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MainActivity : ComponentActivity() {
    private lateinit var itemViewModel: ItemViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext)
        val itemDao = database.itemDao()
        itemViewModel = ViewModelProvider(this, ItemViewModelFactory(itemDao))[ItemViewModel::class.java]

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
    val items by itemViewModel.items.collectAsStateWithLifecycle()
    val isLoading by itemViewModel.isLoading.collectAsStateWithLifecycle(initialValue = false)
    val gridState = rememberLazyGridState()

    LaunchedEffect(gridState) {
        snapshotFlow {
            val layoutInfo = gridState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            lastVisibleItemIndex > (totalItemsNumber - 5) // Start loading when 5 items away from the end
        }.collect { shouldLoadMore ->
            if (shouldLoadMore && !isLoading) {
                itemViewModel.loadMoreItems()
            }
        }
    }

    MainContentUI(
        items = items,
        isLoading = isLoading,
        selectedItem = selectedItem,
        onSelectedItemChange = { selectedItem = it },
        onLoadMore = { itemViewModel.loadMoreItems() }
    )
}

@Composable
fun MainContentUI(
    items: List<Item>,
    isLoading: Boolean,
    selectedItem: Int,
    onSelectedItemChange: (Int) -> Unit,
    onLoadMore: () -> Unit
) {
    val gridState = rememberLazyGridState()

    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = "My Items",
                onSearchClick = { /* TODO: 實現搜索功能 */ }
            )
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
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(items) { item ->
                    ItemCard(
                        item = item,
                        onEdit = { /* TODO: 實現編輯功能 */ },
                        onCopy = { /* TODO: 實現複製功能 */ },
                        onDelete = { /* TODO: 實現刪除功能 */ }
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
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemCard(
    item: Item,
    onEdit: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var cardWidth by remember { mutableStateOf(0.dp) }
    var menuWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(8.dp)
            .onGloballyPositioned { coordinates ->
                cardWidth = with(density) { coordinates.size.width.toDp() }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = item.coverImageId,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Text(
                text = item.name,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )

            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                offset = DpOffset(x = cardWidth - menuWidth, y = 0.dp),
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        menuWidth = with(density) { coordinates.size.width.toDp() }
                    }
            ) {
                DropdownMenuItem(
                    text = { Text("編輯") },
                    onClick = {
                        onEdit()
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("複製") },
                    onClick = {
                        onCopy()
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("刪除") },
                    onClick = {
                        onDelete()
                        showMenu = false
                    }
                )
            }
        }
    }
}

@Composable
fun CustomTopAppBar(title: String, onSearchClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.align(Alignment.Center)
            )
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    val previewItems = listOf(
        Item(1, "Sample Item 1", 1, "QR", "Sample content", 0, 0),
        Item(2, "Sample Item 2", 2, "Barcode", "Sample content 2", 0, 0),
        Item(3, "Sample Item 3", 3, "QR", "Sample content 3", 0, 0),
        Item(4, "Sample Item 4", 4, "Barcode", "Sample content 4", 0, 0)
    )
    
    BaseTheme {
        MainContentUI(
            items = previewItems,
            isLoading = false,
            selectedItem = 0,
            onSelectedItemChange = {},
            onLoadMore = {}
        )
    }
}

// Fake DAO for preview
class FakeItemDao : ItemDao {
    override fun getAll(): Flow<List<Item>> = flowOf(emptyList())
    override suspend fun getPaginatedItems(limit: Int, offset: Int): List<Item> = emptyList()
    override suspend fun getTotalItemCount(): Int = 0
    override suspend fun insert(item: Item) {}
    override suspend fun updateItem(item: Item) {}
    override suspend fun deleteItem(item: Item) {}
    override suspend fun getItemById(id: Int): Item? = null
}
