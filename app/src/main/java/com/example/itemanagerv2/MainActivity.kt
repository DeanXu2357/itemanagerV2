package com.example.itemanagerv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FormatListNumberedRtl
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.itemanagerv2.data.local.model.ItemCardDetail
import com.example.itemanagerv2.data.local.model.ItemCategoryArg
import com.example.itemanagerv2.ui.component.CategoryListPage
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(itemViewModel: ItemViewModel) {
    val cardDetails by itemViewModel.itemCardDetails.collectAsStateWithLifecycle()
    val isLoading by itemViewModel.isLoading.collectAsStateWithLifecycle(initialValue = false)
    val categories by itemViewModel.categories.collectAsStateWithLifecycle()

    var showEditDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var itemCardDetailToEdit by remember { mutableStateOf<ItemCardDetail?>(null) }

    AppScaffold(
        cardDetails = cardDetails,
        categories = categories,
        onEditCard = { cardDetail ->
            itemCardDetailToEdit = cardDetail
            showEditDialog = true
        },
        onManualAdd = { showAddDialog = true },
        onScanAdd = {},
        onDeleteCard = { cardDetail ->
            itemViewModel.deleteItem(cardDetail)
            itemViewModel.refreshItems()
        }
    ) { categoryName -> itemViewModel.addNewCategory(categoryName) }

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

@Composable
fun AppScaffold(
    cardDetails: List<ItemCardDetail>,
    categories: List<ItemCategoryArg>,
    onEditCard: (ItemCardDetail) -> Unit,
    onManualAdd: () -> Unit,
    onScanAdd: () -> Unit,
    onDeleteCard: (ItemCardDetail) -> Unit,
    onAddCategory: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.FormatListNumberedRtl, "Items") },
                    label = { Text("Items") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Category, "Categories") },
                    label = { Text("Categories") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 ->
                    MainPage(
                        cardDetails = cardDetails,
                        onEditCard = onEditCard,
                        onManualAdd = onManualAdd,
                        onScanAdd = onScanAdd,
                        onDeleteCard = onDeleteCard
                    )

                1 ->
                    CategoryListPage(
                        categories = categories,
                        onAddCategory = onAddCategory,
                        onEditCategory = { /* TODO: 實現編輯類別功能 */ },
                        onDeleteCategory = { /* TODO: 實現刪除類別功能 */ }
                    )
            }
        }
    }
}
