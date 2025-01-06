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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.itemanagerv2.data.local.entity.CategoryAttribute
import com.example.itemanagerv2.data.local.model.ItemCardDetail
import com.example.itemanagerv2.data.local.model.ItemCategoryArg
import com.example.itemanagerv2.ui.component.*
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
    private var selectedTab = mutableStateOf(0)
    private var onImageAddedCallback: ((ItemCardDetail) -> Unit)? = null
    private var selectedItem = mutableStateOf<ItemCardDetail?>(null)
    private var showEditDialog = mutableStateOf(false)
    private var isEditFromView = mutableStateOf(false)
    private var forceRecompose = mutableStateOf(0)
    private var showCreatePage = mutableStateOf(false)
    private var isCreatingItem = mutableStateOf(false)

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            contentResolver.openInputStream(selectedUri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (isCreatingItem.value) {
                    itemViewModel.addPendingImage(bitmap, selectedUri)
                } else {
                    itemViewModel.addImageToItem(currentEditingItemId, bitmap, selectedUri) { updatedItem ->
                        updatedItem?.let { item ->
                            onImageAddedCallback?.invoke(item)
                        }
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
                
                if (selectedTab.value == 1) {
                    val currentCategory = itemViewModel.categories.value.firstOrNull()
                    currentCategory?.let { category ->
                        itemViewModel.loadCategoryAttributes(category.id)
                    }
                }
                
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
                    showCreatePage.value -> {
                        showCreatePage.value = false
                        isCreatingItem.value = false
                        itemViewModel.clearPendingImages()
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
                        finish()
                    }
                }
            }
        })

        setContent {
            BaseTheme {
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
                    onPickImageForCreate = {
                        pendingImagePick = true
                        isCreatingItem.value = true
                        checkAndRequestPermission {
                            pendingImagePick = false
                            imagePickerLauncher.launch("image/*")
                        }
                    },
                    showCreatePage = showCreatePage,
                    selectedTab = selectedTab,
                    selectedItem = selectedItem,
                    showEditDialog = showEditDialog,
                    isEditFromView = isEditFromView,
                    onCreateDismiss = {
                        showCreatePage.value = false
                        isCreatingItem.value = false
                        itemViewModel.clearPendingImages()
                    }
                )
            }
        }
    }
}

@Composable
fun MainContent(
    itemViewModel: ItemViewModel,
    onPickImage: (Int, (ItemCardDetail) -> Unit) -> Unit,
    onPickImageForCreate: () -> Unit,
    showCreatePage: MutableState<Boolean>,
    selectedTab: MutableState<Int>,
    selectedItem: MutableState<ItemCardDetail?>,
    showEditDialog: MutableState<Boolean>,
    isEditFromView: MutableState<Boolean>,
    onCreateDismiss: () -> Unit
) {
    val cardDetails by itemViewModel.itemCardDetails.collectAsStateWithLifecycle()
    val isLoading by itemViewModel.isLoading.collectAsStateWithLifecycle(initialValue = false)
    val categories by itemViewModel.categories.collectAsStateWithLifecycle()
    val categoryAttributes by itemViewModel.categoryAttributes.collectAsStateWithLifecycle()
    val pendingImages by itemViewModel.pendingImages.collectAsStateWithLifecycle()

    LaunchedEffect(cardDetails, categories, categoryAttributes) {
        Log.d("MainContent", "Data updated - cardDetails: ${cardDetails.size}, categories: ${categories.size}, attributes: ${categoryAttributes.size}")
    }

    val onAddAttribute = { attribute: CategoryAttribute ->
        itemViewModel.addCategoryAttribute(attribute)
    }

    if (showCreatePage.value) {
        ItemCreateDialog(
            categories = categories,
            categoryAttributes = categoryAttributes,
            pendingImages = pendingImages,
            onNavigateBack = onCreateDismiss,
            onSave = { newItem, pendingImages ->
                itemViewModel.addNewItemWithImages(newItem, pendingImages)
                itemViewModel.refreshItems()
                onCreateDismiss()
            },
            onCategorySelected = { categoryId ->
                itemViewModel.loadCategoryAttributes(categoryId)
            },
            onAddImage = onPickImageForCreate,
            onRemoveImage = { index -> itemViewModel.removePendingImage(index) },
            onClearImages = { itemViewModel.clearPendingImages() }
        )
    } else {
        AppScaffold(
            cardDetails = cardDetails,
            categories = categories,
            categoryAttributes = categoryAttributes,
            onSaveEdit = { itemCardDetail ->
                itemViewModel.updateItemCardDetail(itemCardDetail)
                itemViewModel.refreshItems()
            },
            onManualAdd = { showCreatePage.value = true },
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
            onItemSelect = { item -> 
                itemViewModel.loadCategoryAttributes(item.categoryId)
                selectedItem.value = item 
            }
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
                        onManualAdd = onManualAdd,
                        onScanAdd = onScanAdd,
                        onDeleteCard = onDeleteCard,
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
