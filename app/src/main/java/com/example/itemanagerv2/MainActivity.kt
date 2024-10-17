package com.example.itemanagerv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.itemanagerv2.data.local.entity.Image
import com.example.itemanagerv2.data.local.entity.Item
import com.example.itemanagerv2.data.local.entity.ItemAttributeValue
import com.example.itemanagerv2.data.local.entity.ItemCategory
import com.example.itemanagerv2.data.local.model.ItemCardDetail
import com.example.itemanagerv2.ui.component.ItemEditDialog
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

    MainContentUI(
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

@Composable
fun MainContentUI(
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
                        ) { CircularProgressIndicator() }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemCard(
    cardDetail: ItemCardDetail,
    onEdit: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var cardWidth by remember { mutableStateOf(0.dp) }
    var menuWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Card(
        modifier =
        Modifier
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
                model = cardDetail.coverImage?.filePath ?: "",
                contentDescription = cardDetail.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Text(
                text = cardDetail.name,
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
                modifier =
                Modifier.onGloballyPositioned { coordinates ->
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
            IconButton(onClick = onSearchClick, modifier = Modifier.align(Alignment.CenterEnd)) {
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
        MainContentUI(
            cardDetails = previewCardDetails,
            isLoading = false,
            selectedItem = 0,
            onSelectedItemChange = {},
            onLoadMore = {},
            onEditCard = {}
        )
    }
}
