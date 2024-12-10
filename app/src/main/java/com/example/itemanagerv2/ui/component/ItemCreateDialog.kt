package com.example.itemanagerv2.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.itemanagerv2.data.local.entity.CategoryAttribute
import com.example.itemanagerv2.data.local.entity.ItemAttributeValue
import com.example.itemanagerv2.data.local.model.ItemCardDetail
import com.example.itemanagerv2.data.local.model.ItemCategoryArg
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun ItemCreateDialog(
    categories: List<ItemCategoryArg>,
    categoryAttributes: List<CategoryAttribute>,
    onNavigateBack: () -> Unit,
    onSave: (ItemCardDetail) -> Unit,
    onCategorySelected: (Int) -> Unit
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

    Scaffold(
        topBar = {
            FullScreenDialogTopBar(
                title = "Add Item",
                onDismiss = {
                    editedItem = emptyItem
                    attributeValues = emptyList()
                    onNavigateBack()
                },
                onSave = {
                    if (editedItem.categoryId == 0) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Item name and category are required")
                        }
                        return@FullScreenDialogTopBar
                    }

                    if (editedItem.name.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Item name cannot be empty")
                        }
                        return@FullScreenDialogTopBar
                    }

                    val relevantAttributes = categoryAttributes.filter { it.categoryId == selectedCategoryId }
                    val hasEmptyRequired = relevantAttributes
                        .filter { it.isRequired }
                        .any { attribute -> 
                            attributeValues.find { it.attributeId == attribute.id }?.value?.isBlank() ?: true
                        }

                    if (hasEmptyRequired) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please fill in all required fields")
                        }
                        return@FullScreenDialogTopBar
                    }

                    val finalItem = editedItem.copy(
                        category = categories.find { it.id == selectedCategoryId },
                        attributes = attributeValues
                    )
                    
                    onSave(finalItem)
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
            Column(modifier = Modifier.padding(16.dp)) {
                val fieldModifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)

                OutlinedTextField(
                    value = editedItem.name,
                    onValueChange = { editedItem = editedItem.copy(name = it) },
                    label = { Text("Name") },
                    modifier = fieldModifier,
                    isError = editedItem.name.isBlank()
                )
                
                CategoryDropdown(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelected = { categoryId ->
                        selectedCategoryId = categoryId
                        editedItem = editedItem.copy(categoryId = categoryId)
                        onCategorySelected(categoryId)
                    },
                    modifier = fieldModifier
                )

                // Only show attribute fields if a category is selected
                if (selectedCategoryId != 0) {
                    categoryAttributes
                        .filter { it.categoryId == selectedCategoryId }
                        .forEach { attribute ->
                            when (attribute.valueType) {
                                CategoryAttribute.TYPE_STRING, CategoryAttribute.TYPE_NUMBER -> {
                                    OutlinedTextField(
                                        value = attributeValues.find { it.attributeId == attribute.id }?.value ?: "",
                                        onValueChange = { value ->
                                            attributeValues = attributeValues.map { 
                                                if (it.attributeId == attribute.id) it.copy(value = value)
                                                else it
                                            }
                                        },
                                        label = { Text(attribute.name + if (attribute.isRequired) " *" else "") },
                                        modifier = fieldModifier,
                                        isError = attribute.isRequired && 
                                                (attributeValues.find { it.attributeId == attribute.id }?.value?.isBlank() ?: true)
                                    )
                                }
                                CategoryAttribute.TYPE_DATE_STRING -> {
                                    OutlinedTextField(
                                        value = attributeValues.find { it.attributeId == attribute.id }?.value ?: "",
                                        onValueChange = { value ->
                                            attributeValues = attributeValues.map { 
                                                if (it.attributeId == attribute.id) it.copy(value = value)
                                                else it
                                            }
                                        },
                                        label = { Text("${attribute.name}${if (attribute.isRequired) " *" else ""} (YYYY-MM-DD)") },
                                        modifier = fieldModifier,
                                        isError = attribute.isRequired && 
                                                (attributeValues.find { it.attributeId == attribute.id }?.value?.isBlank() ?: true)
                                    )
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
