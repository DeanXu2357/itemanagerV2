package com.example.itemanagerv2.ui.component

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.itemanagerv2.data.local.entity.CategoryAttribute
import com.example.itemanagerv2.data.local.entity.Image
import com.example.itemanagerv2.data.local.entity.ItemAttributeValue
import com.example.itemanagerv2.data.local.model.ItemCardDetail
import com.example.itemanagerv2.data.local.model.ItemCategoryArg
import kotlinx.coroutines.launch
import java.util.Date

data class PendingImage(
    val bitmap: Bitmap,
    val uri: Uri,
    val order: Int
)

@Composable
fun ItemCreateDialog(
    categories: List<ItemCategoryArg>,
    categoryAttributes: List<CategoryAttribute>,
    pendingImages: List<PendingImage>,
    onNavigateBack: () -> Unit,
    onSave: (ItemCardDetail, List<PendingImage>) -> Unit,
    onCategorySelected: (Int) -> Unit,
    onAddImage: () -> Unit,
    onRemoveImage: (Int) -> Unit,
    onClearImages: () -> Unit
) {
    val emptyItem = ItemCardDetail(
        id = 0,
        name = "",
        categoryId = 0,
        codeType = null,
        codeContent = null,
        codeImageId = null,
        coverImageId = null,
        createdAt = Date().time,
        updatedAt = Date().time,
        category = null,
        codeImage = null,
        coverImage = null,
        images = emptyList(),
        attributes = emptyList()
    )
    
    var editedItem by remember { mutableStateOf(emptyItem) }
    var selectedCategoryId by remember { mutableIntStateOf(0) }
    var attributeValues by remember { mutableStateOf(emptyList<ItemAttributeValue>()) }
    var showNameError by remember { mutableStateOf(false) }
    var showCategoryError by remember { mutableStateOf(false) }
    var attributeErrors by remember { mutableStateOf(mapOf<Int, Boolean>()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Update attribute values when category is selected and attributes are loaded
    LaunchedEffect(selectedCategoryId, categoryAttributes) {
        if (selectedCategoryId != 0) {
            val relevantAttributes = categoryAttributes.filter { it.categoryId == selectedCategoryId }
            attributeValues = relevantAttributes.map { attribute ->
                ItemAttributeValue(
                    id = 0,
                    itemId = 0,
                    attributeId = attribute.id,
                    value = attribute.defaultValue ?: "",
                    createdAt = Date(),
                    updatedAt = Date()
                )
            }
        }
    }

    fun validateFields(): Boolean {
        var isValid = true
        
        // Validate name
        showNameError = editedItem.name.isBlank()
        if (showNameError) isValid = false

        // Validate category
        showCategoryError = editedItem.categoryId == 0
        if (showCategoryError) isValid = false

        // Validate required attributes
        val relevantAttributes = categoryAttributes.filter { it.categoryId == selectedCategoryId }
        attributeErrors = relevantAttributes
            .filter { it.isRequired }
            .associate { attribute -> 
                val value = attributeValues.find { it.attributeId == attribute.id }?.value
                attribute.id to (value?.isBlank() ?: true)
            }
        if (attributeErrors.any { it.value }) isValid = false

        return isValid
    }

    Scaffold(
        topBar = {
            FullScreenDialogTopBar(
                title = "Add Item",
                onDismiss = {
                    editedItem = emptyItem
                    attributeValues = emptyList()
                    onClearImages()
                    onNavigateBack()
                },
                onSave = {
                    if (!validateFields()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please correct the highlighted fields")
                        }
                        return@FullScreenDialogTopBar
                    }

                    val finalItem = editedItem.copy(
                        category = categories.find { it.id == selectedCategoryId },
                        attributes = attributeValues
                    )
                    
                    onSave(finalItem, pendingImages)
                    onClearImages()
                    onNavigateBack()
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
                images = pendingImages.mapIndexed { index, pending ->
                    Image(
                        id = -(index + 1), // Use negative IDs for pending images
                        filePath = pending.uri.toString(),
                        itemId = 0,
                        order = pending.order,
                        content = null,
                        createdAt = Date(),
                        updatedAt = Date()
                    )
                },
                onAddClick = onAddImage,
                onDeleteClick = { tempId -> 
                    val index = -tempId - 1
                    onRemoveImage(index)
                },
                onSetCover = { tempId ->
                    // No need to handle cover image during creation
                    // The first image will automatically become the cover
                },
                selectedCoverImageId = if (pendingImages.isNotEmpty()) -1 else null
            )

            Column(modifier = Modifier.padding(16.dp)) {
                val fieldModifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)

                Column {
                    OutlinedTextField(
                        value = editedItem.name,
                        onValueChange = { 
                            editedItem = editedItem.copy(name = it)
                            showNameError = false
                        },
                        label = { Text("Name *") },
                        modifier = fieldModifier,
                        isError = showNameError
                    )
                    if (showNameError) {
                        Text(
                            text = "Name is required",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
                
                Column {
                    CategoryDropdown(
                        categories = categories,
                        selectedCategoryId = selectedCategoryId,
                        onCategorySelected = { categoryId ->
                            selectedCategoryId = categoryId
                            editedItem = editedItem.copy(categoryId = categoryId)
                            showCategoryError = false
                            onCategorySelected(categoryId)
                        },
                        modifier = fieldModifier,
                        isError = showCategoryError
                    )
                    if (showCategoryError) {
                        Text(
                            text = "Category is required",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }

                // Only show attribute fields if a category is selected
                if (selectedCategoryId != 0) {
                    categoryAttributes
                        .filter { it.categoryId == selectedCategoryId }
                        .forEach { attribute ->
                            Column {
                                when (attribute.valueType) {
                                    CategoryAttribute.TYPE_STRING, CategoryAttribute.TYPE_NUMBER -> {
                                        OutlinedTextField(
                                            value = attributeValues.find { it.attributeId == attribute.id }?.value ?: "",
                                            onValueChange = { value ->
                                                attributeValues = attributeValues.map { 
                                                    if (it.attributeId == attribute.id) it.copy(value = value)
                                                    else it
                                                }
                                                attributeErrors = attributeErrors - attribute.id
                                            },
                                            label = { Text(attribute.name + if (attribute.isRequired) " *" else "") },
                                            modifier = fieldModifier,
                                            isError = attributeErrors[attribute.id] == true
                                        )
                                        if (attributeErrors[attribute.id] == true) {
                                            Text(
                                                text = "${attribute.name} is required",
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(start = 16.dp)
                                            )
                                        }
                                    }
                                    CategoryAttribute.TYPE_DATE_STRING -> {
                                        OutlinedTextField(
                                            value = attributeValues.find { it.attributeId == attribute.id }?.value ?: "",
                                            onValueChange = { value ->
                                                attributeValues = attributeValues.map { 
                                                    if (it.attributeId == attribute.id) it.copy(value = value)
                                                    else it
                                                }
                                                attributeErrors = attributeErrors - attribute.id
                                            },
                                            label = { Text("${attribute.name}${if (attribute.isRequired) " *" else ""} (YYYY-MM-DD)") },
                                            modifier = fieldModifier,
                                            isError = attributeErrors[attribute.id] == true
                                        )
                                        if (attributeErrors[attribute.id] == true) {
                                            Text(
                                                text = "${attribute.name} is required",
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(start = 16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                }

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
