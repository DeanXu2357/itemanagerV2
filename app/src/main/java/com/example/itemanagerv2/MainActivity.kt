package com.example.itemanagerv2

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.itemanagerv2.data.local.entity.CategoryAttribute
import com.example.itemanagerv2.data.local.model.ItemCardDetail
import com.example.itemanagerv2.data.local.model.ItemCategoryArg
import com.example.itemanagerv2.ui.component.CategoryListPage
import com.example.itemanagerv2.ui.component.ItemEditDialog
import com.example.itemanagerv2.ui.component.MainPage
import com.example.itemanagerv2.ui.theme.BaseTheme
import com.example.itemanagerv2.viewmodel.ItemViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.graphics.BitmapFactory
import android.content.pm.PackageManager
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val itemViewModel: ItemViewModel by viewModels()
    private var currentEditingItemId: Int = 0
    private var pendingImagePick: Boolean = false
    private var showEditDialog = mutableStateOf(false)
    private var showAddDialog = mutableStateOf(false)
    private var selectedTab = mutableStateOf(0)

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            // Convert URI to Bitmap
            contentResolver.openInputStream(selectedUri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                itemViewModel.addImageToItem(currentEditingItemId, bitmap)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted && pendingImagePick) {
            pendingImagePick = false
            imagePickerLauncher.launch("image/*")
        }
    }

    private fun checkAndRequestPermission(onPermissionGranted: () -> Unit) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted, proceed with the operation
                onPermissionGranted()
            }
            else -> {
                // Request the permission
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle back press with unified logic
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    // If dialog is showing, close it first
                    showEditDialog.value -> showEditDialog.value = false
                    showAddDialog.value -> showAddDialog.value = false
                    // If on CategoryListPage, return to MainPage
                    selectedTab.value == 1 -> selectedTab.value = 0
                    // If on MainPage, allow app exit
                    else -> isEnabled = false
                }
            }
        })

        setContent { 
            BaseTheme { 
                MainContent(
                    itemViewModel = itemViewModel,
                    onPickImage = { itemId -> 
                        currentEditingItemId = itemId
                        pendingImagePick = true
                        checkAndRequestPermission {
                            pendingImagePick = false
                            imagePickerLauncher.launch("image/*")
                        }
                    },
                    showEditDialog = showEditDialog,
                    showAddDialog = showAddDialog,
                    selectedTab = selectedTab
                ) 
            } 
        }
    }
}

@Composable
fun MainContent(
    itemViewModel: ItemViewModel,
    onPickImage: (Int) -> Unit,
    showEditDialog: MutableState<Boolean>,
    showAddDialog: MutableState<Boolean>,
    selectedTab: MutableState<Int>
) {
    val cardDetails by itemViewModel.itemCardDetails.collectAsStateWithLifecycle()
    val isLoading by itemViewModel.isLoading.collectAsStateWithLifecycle(initialValue = false)
    val categories by itemViewModel.categories.collectAsStateWithLifecycle()
    val categoryAttributes by itemViewModel.categoryAttributes.collectAsStateWithLifecycle()
    var itemCardDetailToEdit by remember { mutableStateOf<ItemCardDetail?>(null) }

    val onAddAttribute = { attribute: CategoryAttribute ->
        itemViewModel.addCategoryAttribute(attribute)
    }

    AppScaffold(
        cardDetails = cardDetails,
        categories = categories,
        categoryAttributes = categoryAttributes,
        onEditCard = { item ->
            itemCardDetailToEdit = item
            showEditDialog.value = true
            // Load attributes for the item's category
            itemViewModel.loadCategoryAttributes(item.categoryId)
        },
        onManualAdd = { showAddDialog.value = true },
        onScanAdd = { /* TODO: */ },
        onDeleteCard = { itemCardDetail -> itemViewModel.deleteItem(itemCardDetail) },
        onAddCategory = { categoryName -> itemViewModel.addNewCategory(categoryName) },
        onDeleteCategory = { categoryId -> itemViewModel.deleteCategory(categoryId) },
        onLoadCategoryAttributes = { categoryId ->
            itemViewModel.loadCategoryAttributes(categoryId)
        },
        onDeleteAttribute = { attributeId ->
            itemViewModel.deleteCategoryAttribute(attributeId)
        },
        onAddAttribute = onAddAttribute,
        selectedTab = selectedTab
    )

    if (showEditDialog.value && itemCardDetailToEdit != null) {
        itemViewModel.ensureCategoriesLoaded()
        ItemEditDialog(
            item = itemCardDetailToEdit!!,
            categories = categories,
            categoryAttributes = categoryAttributes,
            onDismiss = {
                showEditDialog.value = false
                itemCardDetailToEdit = null
            },
            onSave = { itemCardDetail ->
                itemViewModel.updateItemCardDetail(itemCardDetail)
                itemViewModel.refreshItems()
                showEditDialog.value = false
                itemCardDetailToEdit = null
            },
            onAddImage = { onPickImage(itemCardDetailToEdit!!.id) },
            onDeleteImage = { imageId -> 
                itemCardDetailToEdit?.let { item ->
                    val isCoverImage = item.coverImageId == imageId
                    itemViewModel.deleteImage(item.id, imageId, isCoverImage)
                }
            },
            onCategorySelected = { categoryId ->
                itemViewModel.loadCategoryAttributes(categoryId)
            }
        )
    }

    if (showAddDialog.value) {
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
            categories = categories,
            categoryAttributes = categoryAttributes,
            onDismiss = { showAddDialog.value = false },
            onSave = { newItem ->
                itemViewModel.addNewItem(newItem)
                itemViewModel.refreshItems()
                showAddDialog.value = false
            },
            onAddImage = { onPickImage(0) }, // 0 for new items
            onDeleteImage = { imageId -> 
                itemCardDetailToEdit?.let { item ->
                    val isCoverImage = item.coverImageId == imageId
                    itemViewModel.deleteImage(item.id, imageId, isCoverImage)
                }
            },
            onCategorySelected = { categoryId ->
                itemViewModel.loadCategoryAttributes(categoryId)
            }
        )
    }
}

@Composable
fun AppScaffold(
    cardDetails: List<ItemCardDetail>,
    categories: List<ItemCategoryArg>,
    categoryAttributes: List<CategoryAttribute>,
    onEditCard: (ItemCardDetail) -> Unit,
    onManualAdd: () -> Unit,
    onScanAdd: () -> Unit,
    onDeleteCard: (ItemCardDetail) -> Unit,
    onAddCategory: (String) -> Unit,
    onDeleteCategory: (Int) -> Unit,
    onLoadCategoryAttributes: (Int) -> Unit,
    onDeleteAttribute: (Int) -> Unit,
    onAddAttribute: (CategoryAttribute) -> Unit,
    selectedTab: MutableState<Int>
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.FormatListNumberedRtl, "Items") },
                    label = { Text("Items") },
                    selected = selectedTab.value == 0,
                    onClick = { selectedTab.value = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Category, "Categories") },
                    label = { Text("Categories") },
                    selected = selectedTab.value == 1,
                    onClick = { selectedTab.value = 1 }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab.value) {
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
                        categoryAttributes = categoryAttributes,
                        onAddCategory = onAddCategory,
                        onEditCategory = { /* TODO: 實現編輯類別功能 */ },
                        onDeleteCategory = onDeleteCategory,
                        onLoadCategoryAttributes = onLoadCategoryAttributes,
                        onDeleteAttribute = onDeleteAttribute,
                        onAddAttribute = onAddAttribute
                    )
            }
        }
    }
}
