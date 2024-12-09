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
import com.example.itemanagerv2.data.local.entity.ItemAttributeValue
import com.example.itemanagerv2.data.local.model.ItemCardDetail
import com.example.itemanagerv2.data.local.model.ItemCategoryArg
import com.example.itemanagerv2.ui.theme.BaseTheme
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun ItemCreateDialog(
    categories: List<ItemCategoryArg>,
    categoryAttributes: List<CategoryAttribute>,
    onDismiss: () -> Unit,
    onSave: (ItemCardDetail) -> Unit,
    onCategorySelected: (Int) -> Unit = {}
) {
    // Initialize with empty item
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
    var isDetailExpanded by remember { mutableStateOf(true) }
    var isQRCodeExpanded by remember { mutableStateOf(true) }
    var selectedCategoryId by remember { mutableIntStateOf(0) }
    
    // Initialize attribute values map
    var attributeValues by remember { mutableStateOf(emptyMap<Int, String>()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            FullScreenDialogTopBar(
                title = "Add Item",
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
                                ItemAttributeValue(
                                    id = 0,
                                    itemId = 0,
                                    attributeId = attribute.id,
                                    value = value,
                                    createdAt = Date(),
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

@Preview(showBackground = true)
@Composable
fun ItemCreateDialogPreview() {
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
        ItemCreateDialog(
            categories = sampleCategories,
            categoryAttributes = sampleAttributes,
            onDismiss = {},
            onSave = {},
            onCategorySelected = {}
        )
    }
}
