package com.example.itemanagerv2.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.itemanagerv2.data.local.entity.CategoryAttribute
import com.example.itemanagerv2.data.local.entity.Image
import com.example.itemanagerv2.data.local.entity.ItemAttributeValue
import com.example.itemanagerv2.data.local.model.ItemCardDetail
import com.example.itemanagerv2.data.local.model.ItemCategoryArg
import com.example.itemanagerv2.ui.theme.BaseTheme
import java.util.Date
import kotlinx.coroutines.launch

@Composable
fun ItemEditDialog(
    item: ItemCardDetail,
    categories: List<ItemCategoryArg>,
    categoryAttributes: List<CategoryAttribute>,
    onDismiss: () -> Unit,
    onSave: (ItemCardDetail) -> Unit,
    onAddImage: () -> Unit,
    onDeleteImage: (Int) -> Unit,
    onCategorySelected: (Int) -> Unit = {},
    onSetCoverImage: (Int, Int) -> Unit
) {
    var editedItem by remember { mutableStateOf(item) }
    var isDetailExpanded by remember { mutableStateOf(true) }
    var isQRCodeExpanded by remember { mutableStateOf(true) }
    var selectedCategoryId by remember { mutableIntStateOf(item.categoryId) }
    
    // Initialize attribute values map with existing values
    var attributeValues by remember { 
        mutableStateOf(item.attributes.associate { it.attributeId to it.value })
    }

    // Update editedItem when item changes (e.g., when new images are added)
    LaunchedEffect(item) {
        editedItem = item
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            FullScreenDialogTopBar(
                title = if (item.id == 0) "Add Item" else "Edit Item",
                onDismiss = onDismiss,
                onSave = {
                    if (editedItem.categoryId == 0) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Item name and category are required")
                        }
                        return@FullScreenDialogTopBar
                    }

                    if (editedItem.name.isNotBlank()) {
                        val newAttributes = categoryAttributes
                            .filter { it.categoryId == selectedCategoryId }
                            .map { attribute ->
                                val value = attributeValues[attribute.id] ?: attribute.defaultValue ?: ""
                                val existingAttr = editedItem.attributes.find { it.attributeId == attribute.id }
                                ItemAttributeValue(
                                    id = existingAttr?.id ?: 0,
                                    itemId = editedItem.id,
                                    attributeId = attribute.id,
                                    value = value,
                                    createdAt = existingAttr?.createdAt ?: Date(),
                                    updatedAt = Date()
                                )
                            }
                        onSave(editedItem.copy(attributes = newAttributes))
                        onDismiss()
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Item name cannot be empty")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            MultiPreviewImageCarousel(
                images = editedItem.images,
                onAddClick = onAddImage,
                onDeleteClick = { imageId -> 
                    val isCoverImage = imageId == editedItem.coverImageId
                    val remainingImages = editedItem.images.filter { it.id != imageId }
                    
                    // If we're deleting the cover image and there are remaining images,
                    // set the first remaining image as the new cover
                    if (isCoverImage && remainingImages.isNotEmpty()) {
                        val newCoverImage = remainingImages.first()
                        editedItem = editedItem.copy(
                            images = remainingImages,
                            coverImageId = newCoverImage.id,
                            coverImage = newCoverImage
                        )
                    } else if (isCoverImage) {
                        // If we're deleting the cover image and there are no remaining images
                        editedItem = editedItem.copy(
                            images = remainingImages,
                            coverImageId = null,
                            coverImage = null
                        )
                    } else {
                        // If we're not deleting the cover image
                        editedItem = editedItem.copy(images = remainingImages)
                    }
                    
                    onDeleteImage(imageId)
                },
                onSetCover = { imageId ->
                    // Update local state
                    editedItem = editedItem.copy(
                        coverImageId = imageId,
                        coverImage = editedItem.images.find { it.id == imageId }
                    )
                    // Update database immediately
                    onSetCoverImage(editedItem.id, imageId)
                },
                selectedCoverImageId = editedItem.coverImageId
            )

            ExpandableSection(
                title = "Details",
                isExpanded = isDetailExpanded,
                onExpandToggle = { isDetailExpanded = !isDetailExpanded }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val fieldModifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)

                    OutlinedTextField(
                        value = editedItem.name,
                        onValueChange = { editedItem = editedItem.copy(name = it) },
                        label = { Text("Name") },
                        modifier = fieldModifier
                    )
                    
                    CategoryDropdown(
                        categories = categories,
                        selectedCategoryId = selectedCategoryId,
                        onCategorySelected = { categoryId ->
                            selectedCategoryId = categoryId
                            editedItem = editedItem.copy(categoryId = categoryId)
                            // Clear attribute values when category changes
                            attributeValues = emptyMap()
                            // Load attributes for the selected category
                            onCategorySelected(categoryId)
                        },
                        modifier = fieldModifier
                    )

                    // FIXME: It should display fields dynamically based on ItemAttributeValue belong to the item, not CategoryAttribute
                    // Dynamic attribute fields based on selected category
                    categoryAttributes
                        .filter { it.categoryId == selectedCategoryId }
                        .forEach { attribute ->
                            when (attribute.valueType) {
                                CategoryAttribute.TYPE_STRING, CategoryAttribute.TYPE_NUMBER -> {
                                    OutlinedTextField(
                                        value = attributeValues[attribute.id] ?: attribute.defaultValue ?: "",
                                        onValueChange = { value ->
                                            attributeValues = attributeValues + (attribute.id to value)
                                        },
                                        label = { Text(attribute.name) },
                                        modifier = fieldModifier,
                                        isError = attribute.isRequired && (attributeValues[attribute.id]?.isBlank() ?: true)
                                    )
                                }
                                CategoryAttribute.TYPE_DATE_STRING -> {
                                    // TODO: Implement date picker
                                    OutlinedTextField(
                                        value = attributeValues[attribute.id] ?: attribute.defaultValue ?: "",
                                        onValueChange = { value ->
                                            attributeValues = attributeValues + (attribute.id to value)
                                        },
                                        label = { Text("${attribute.name} (YYYY-MM-DD)") },
                                        modifier = fieldModifier,
                                        isError = attribute.isRequired && (attributeValues[attribute.id]?.isBlank() ?: true)
                                    )
                                }
                            }
                        }
                }
            }

            ExpandableSection(
                title = "QR Code",
                isExpanded = isQRCodeExpanded,
                onExpandToggle = { isQRCodeExpanded = !isQRCodeExpanded }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val fieldModifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                    OutlinedTextField(
                        value = editedItem.codeType ?: "",
                        onValueChange = { editedItem = editedItem.copy(codeType = it) },
                        label = { Text("Barcode Type") },
                        modifier = fieldModifier
                    )
                    OutlinedTextField(
                        value = editedItem.codeContent ?: "",
                        onValueChange = { editedItem = editedItem.copy(codeContent = it) },
                        label = { Text("Barcode Content") },
                        modifier = fieldModifier
                    )
                }
            }
        }
    }
}

@Composable
fun ExpandableSection(
    title: String,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        ListItem(
            headlineContent = { Text(title) },
            trailingContent = {
                IconButton(onClick = onExpandToggle) {
                    Icon(
                        imageVector =
                        if (isExpanded) Icons.Filled.ExpandLess
                        else Icons.Filled.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }
        )
        AnimatedVisibility(visible = isExpanded) { content() }
    }
}

@Composable
fun FullScreenDialogTopBar(title: String, onDismiss: () -> Unit, onSave: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
        ) {
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.align(Alignment.Center)
            )

            TextButton(onClick = onSave, modifier = Modifier.align(Alignment.CenterEnd)) {
                Text("Save", color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ItemEditDialogPreview() {
    val currentDate = Date()
    val sampleCategories = listOf(
        ItemCategoryArg(1, "Electronics"),
        ItemCategoryArg(2, "Furniture"),
        ItemCategoryArg(3, "Books")
    )

    val sampleAttributes = listOf(
        CategoryAttribute(
            id = 1,
            categoryId = 1,
            name = "Brand",
            isRequired = true,
            isEditable = true,
            valueType = CategoryAttribute.TYPE_STRING,
            defaultValue = null,
            createdAt = currentDate,
            updatedAt = currentDate
        ),
        CategoryAttribute(
            id = 2,
            categoryId = 1,
            name = "Model",
            isRequired = false,
            isEditable = true,
            valueType = CategoryAttribute.TYPE_STRING,
            defaultValue = null,
            createdAt = currentDate,
            updatedAt = currentDate
        ),
        CategoryAttribute(
            id = 3,
            categoryId = 1,
            name = "Purchase Date",
            isRequired = false,
            isEditable = true,
            valueType = CategoryAttribute.TYPE_DATE_STRING,
            defaultValue = null,
            createdAt = currentDate,
            updatedAt = currentDate
        )
    )

    BaseTheme {
        val sampleCategory = ItemCategoryArg(1, "Electronics")

        val sampleImages = listOf(
            Image(
                id = 1,
                filePath = "https://example.com/image1.jpg",
                itemId = 1,
                order = 0,
                content = "Sample image 1",
                createdAt = currentDate,
                updatedAt = currentDate
            ),
            Image(
                id = 2,
                filePath = "https://example.com/image2.jpg",
                itemId = 1,
                order = 1,
                content = "Sample image 2",
                createdAt = currentDate,
                updatedAt = currentDate
            )
        )

        val sampleAttributeValues = listOf(
            ItemAttributeValue(
                id = 1,
                itemId = 1,
                attributeId = 1,
                value = "Brand X",
                createdAt = currentDate,
                updatedAt = currentDate
            ),
            ItemAttributeValue(
                id = 2,
                itemId = 1,
                attributeId = 2,
                value = "Model Y",
                createdAt = currentDate,
                updatedAt = currentDate
            )
        )

        val sampleItem = ItemCardDetail(
            id = 1,
            name = "Sample Item",
            categoryId = sampleCategory.id,
            codeType = "QR",
            codeContent = "https://example.com",
            codeImageId = null,
            coverImageId = sampleImages.firstOrNull()?.id,
            createdAt = currentDate.time,
            updatedAt = currentDate.time,
            category = sampleCategory,
            codeImage = null,
            coverImage = sampleImages.firstOrNull(),
            images = sampleImages,
            attributes = sampleAttributeValues
        )

        ItemEditDialog(
            item = sampleItem,
            categories = sampleCategories,
            categoryAttributes = sampleAttributes,
            onDismiss = {},
            onSave = {},
            onAddImage = {},
            onDeleteImage = {},
            onCategorySelected = {},
            onSetCoverImage = { _, _ -> }
        )
    }
}
