package com.example.itemanagerv2

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.itemanagerv2.data.local.entity.CategoryAttribute
import com.example.itemanagerv2.data.local.model.ItemCardDetail
import com.example.itemanagerv2.data.local.model.ItemCategoryArg
import com.example.itemanagerv2.ui.component.CategoryListPage
import com.example.itemanagerv2.ui.component.MainPage
import com.example.itemanagerv2.ui.theme.BaseTheme
import com.example.itemanagerv2.viewmodel.ItemViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.graphics.BitmapFactory
import android.content.pm.PackageManager
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import com.example.itemanagerv2.ui.component.ItemEditDialog
import com.example.itemanagerv2.ui.component.ItemViewDialog

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val itemViewModel: ItemViewModel by viewModels()
    private var currentEditingItemId: Int = 0
    private var pendingImagePick: Boolean = false
    private var showAddDialog = mutableStateOf(false)
    private var selectedTab = mutableStateOf(0)
    private var onImageAddedCallback: ((ItemCardDetail) -> Unit)? = null
    private var selectedItem = mutableStateOf<ItemCardDetail?>(null)
    private var showEditDialog = mutableStateOf(false)
    private var isEditFromView = mutableStateOf(false)
    private var forceRecompose = mutableStateOf(0)

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            contentResolver.openInputStream(selectedUri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                itemViewModel.addImageToItem(currentEditingItemId, bitmap) { updatedItem ->
                    updatedItem?.let { item ->
                        onImageAddedCallback?.invoke(item)
                    }
                }
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
                onPermissionGranted()
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            try {
                itemViewModel.refreshItems()
                
                // If we're on the categories tab, also refresh the category attributes
                if (selectedTab.value == 1) {
                    val currentCategory = itemViewModel.categories.value.firstOrNull()
                    currentCategory?.let { category ->
                        itemViewModel.loadCategoryAttributes(category.id)
                    }
                }
                
                // Force a recomposition
                forceRecompose.value = forceRecompose.value + 1
                Log.d("MainActivity", "Data refresh complete")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error refreshing data", e)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    showAddDialog.value -> {
                        showAddDialog.value = false
                    }
                    showEditDialog.value -> {
                        showEditDialog.value = false
                        if (!isEditFromView.value) {
                            selectedItem.value = null
                        }
                        isEditFromView.value = false
                    }
                    selectedItem.value != null -> {
                        selectedItem.value = null
                    }
                    selectedTab.value == 1 -> {
                        selectedTab.value = 0
                    }
                    selectedTab.value == 0 -> {
                        // Only finish() when on main screen with no dialogs or selected items
                        finish()
                    }
                }
            }
        })

        setContent {
            BaseTheme {
                // Use forceRecompose to trigger recomposition
                forceRecompose.value
                
                MainContent(
                    itemViewModel = itemViewModel,
                    onPickImage = { itemId, onImageAdded ->
                        currentEditingItemId = itemId
                        onImageAddedCallback = onImageAdded
                        pendingImagePick = true
                        checkAndRequestPermission {
                            pendingImagePick = false
                            imagePickerLauncher.launch("image/*")
                            itemViewModel.refreshItems()
                        }
                    },
                    showAddDialog = showAddDialog,
                    selectedTab = selectedTab,
                    selectedItem = selectedItem,
                    showEditDialog = showEditDialog,
                    isEditFromView = isEditFromView
                )
            }
        }
    }
}

@Composable
fun MainContent(
    itemViewModel: ItemViewModel,
    onPickImage: (Int, (ItemCardDetail) -> Unit) -> Unit,
    showAddDialog: MutableState<Boolean>,
    selectedTab: MutableState<Int>,
    selectedItem: MutableState<ItemCardDetail?>,
    showEditDialog: MutableState<Boolean>,
    isEditFromView: MutableState<Boolean>
) {
    val cardDetails by itemViewModel.itemCardDetails.collectAsStateWithLifecycle()
    val isLoading by itemViewModel.isLoading.collectAsStateWithLifecycle(initialValue = false)
    val categories by itemViewModel.categories.collectAsStateWithLifecycle()
    val categoryAttributes by itemViewModel.categoryAttributes.collectAsStateWithLifecycle()

    LaunchedEffect(cardDetails, categories, categoryAttributes) {
        Log.d("MainContent", "Data updated - cardDetails: ${cardDetails.size}, categories: ${categories.size}, attributes: ${categoryAttributes.size}")
    }

    val onAddAttribute = { attribute: CategoryAttribute ->
        itemViewModel.addCategoryAttribute(attribute)
    }

    AppScaffold(
        cardDetails = cardDetails,
        categories = categories,
        categoryAttributes = categoryAttributes,
        onSaveEdit = { itemCardDetail ->
            itemViewModel.updateItemCardDetail(itemCardDetail)
            itemViewModel.refreshItems()
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
        onAddImage = { itemId, onImageAdded ->
            onPickImage(itemId, onImageAdded)
        },
        onDeleteImage = { itemId, imageId, isCoverImage ->
            itemViewModel.deleteImage(itemId, imageId, isCoverImage)
            itemViewModel.refreshItems()
        },
        onSetCoverImage = { itemId, imageId ->
            itemViewModel.updateItemCoverImage(itemId, imageId)
        },
        selectedTab = selectedTab,
        onItemSelect = { item -> selectedItem.value = item }
    )

    // View Dialog
    selectedItem.value?.let { item ->
        if (!showEditDialog.value) {
            ItemViewDialog(
                item = item,
                categoryAttributes = categoryAttributes,
                onDismiss = { selectedItem.value = null },
                onEdit = { 
                    isEditFromView.value = true
                    showEditDialog.value = true 
                }
            )
        }
    }

    // Edit Dialog
    if (showEditDialog.value && selectedItem.value != null) {
        ItemEditDialog(
            item = selectedItem.value!!,
            categories = categories,
            categoryAttributes = categoryAttributes,
            onDismiss = {
                showEditDialog.value = false
                if (!isEditFromView.value) {
                    selectedItem.value = null
                }
                isEditFromView.value = false
            },
            onSave = { editedItem ->
                itemViewModel.updateItemCardDetail(editedItem)
                itemViewModel.refreshItems()
                showEditDialog.value = false
                if (!isEditFromView.value) {
                    selectedItem.value = null
                }
                isEditFromView.value = false
            },
            onAddImage = {
                onPickImage(selectedItem.value!!.id) { updatedItem ->
                    selectedItem.value = updatedItem
                }
            },
            onDeleteImage = { imageId -> 
                val isCoverImage = selectedItem.value!!.coverImageId == imageId
                itemViewModel.deleteImage(selectedItem.value!!.id, imageId, isCoverImage)
                itemViewModel.refreshItems()
            },
            onCategorySelected = { categoryId ->
                itemViewModel.loadCategoryAttributes(categoryId)
            },
            onSetCoverImage = { _, imageId -> 
                itemViewModel.updateItemCoverImage(selectedItem.value!!.id, imageId)
            }
        )
    }

    // Add Dialog
    if (showAddDialog.value) {
        itemViewModel.ensureCategoriesLoaded()
        val emptyItem = ItemCardDetail(
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
            onAddImage = {
                onPickImage(0) { updatedItem ->
                    // No need to update state since this is a new item
                }
            },
            onDeleteImage = { imageId ->
                // No need to handle delete for new item
            },
            onCategorySelected = { categoryId ->
                itemViewModel.loadCategoryAttributes(categoryId)
            },
            onSetCoverImage = { itemId, imageId ->
                itemViewModel.updateItemCoverImage(itemId, imageId)
            }
        )
    }
}

@Composable
fun AppScaffold(
    cardDetails: List<ItemCardDetail>,
    categories: List<ItemCategoryArg>,
    categoryAttributes: List<CategoryAttribute>,
    onSaveEdit: (ItemCardDetail) -> Unit,
    onManualAdd: () -> Unit,
    onScanAdd: () -> Unit,
    onDeleteCard: (ItemCardDetail) -> Unit,
    onAddCategory: (String) -> Unit,
    onDeleteCategory: (Int) -> Unit,
    onLoadCategoryAttributes: (Int) -> Unit,
    onDeleteAttribute: (Int) -> Unit,
    onAddAttribute: (CategoryAttribute) -> Unit,
    onAddImage: (Int, (ItemCardDetail) -> Unit) -> Unit,
    onDeleteImage: (Int, Int, Boolean) -> Unit,
    onSetCoverImage: (Int, Int) -> Unit,
    selectedTab: MutableState<Int>,
    onItemSelect: (ItemCardDetail) -> Unit
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
                        categories = categories,
                        categoryAttributes = categoryAttributes,
                        onSaveEdit = onSaveEdit,
                        onManualAdd = onManualAdd,
                        onScanAdd = onScanAdd,
                        onDeleteCard = onDeleteCard,
                        onAddImage = onAddImage,
                        onDeleteImage = { itemId, imageId -> 
                            val isCoverImage = cardDetails.find { it.id == itemId }?.coverImageId == imageId
                            onDeleteImage(itemId, imageId, isCoverImage)
                        },
                        onCategorySelected = onLoadCategoryAttributes,
                        onSetCoverImage = onSetCoverImage,
                        onItemSelect = onItemSelect
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
