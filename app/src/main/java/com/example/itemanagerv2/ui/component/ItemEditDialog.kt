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
import com.example.itemanagerv2.data.local.entity.Image
import com.example.itemanagerv2.data.local.entity.ItemAttributeValue
import com.example.itemanagerv2.data.local.entity.ItemCategory
import com.example.itemanagerv2.data.local.model.ItemCardDetail
import com.example.itemanagerv2.ui.theme.BaseTheme
import java.util.Date

@Composable
fun ItemEditDialog(
    item: ItemCardDetail,
    onDismiss: () -> Unit,
    onSave: (ItemCardDetail) -> Unit,
    onAddImage: () -> Unit,
    onDeleteImage: (Int) -> Unit
) {
    var editedItem by remember { mutableStateOf(item) } // TODO: handle if item is null
    var isDetailExpanded by remember { mutableStateOf(true) }
    var isQRCodeExpanded by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            FullScreenDialogTopBar(
                title = if (item.id == 0) "Add Item" else "Edit Item",
                onDismiss = onDismiss,
                onSave = { onSave(editedItem) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
//                .padding(16.dp)
        ) {
            MultiPreviewImageCarousel(
                images = editedItem.images,
                onAddClick = onAddImage,
                onDeleteClick = onDeleteImage
            )

            ExpandableSection(
                title = "Details",
                isExpanded = isDetailExpanded,
                onExpandToggle = { isDetailExpanded = !isDetailExpanded }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = editedItem.name,
                        onValueChange = { editedItem = editedItem.copy(name = it) },
                        label = { Text("Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = editedItem.codeType ?: "",
                        onValueChange = { editedItem = editedItem.copy(codeType = it) },
                        label = { Text("Barcode Type") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                }
            }

            ExpandableSection(
                title = "QR Code",
                isExpanded = isQRCodeExpanded,
                onExpandToggle = { isQRCodeExpanded = !isQRCodeExpanded }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = editedItem.codeType ?: "",
                        onValueChange = { editedItem = editedItem.copy(codeType = it) },
                        label = { Text("Barcode Type") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = editedItem.codeContent ?: "",
                        onValueChange = { editedItem = editedItem.copy(codeContent = it) },
                        label = { Text("Barcode Content") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                    // Here you can add a composable to display the QR code image
                    // For example:
                    // QRCodeImage(data = editedItem.codeContent)
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
                        imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }
        )
        AnimatedVisibility(visible = isExpanded) {
            content()
        }
    }
}

@Composable
fun FullScreenDialogTopBar(
    title: String,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
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
            // 返回按鈕
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }

            // 標題
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.align(Alignment.Center)
            )

            // 保存按鈕
            TextButton(
                onClick = onSave,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text(
                    "Save",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ItemEditDialogPreview() {
    BaseTheme {
        val currentDate = Date()

        val sampleCategory = ItemCategory(
            id = 1,
            name = "Electronics",
            createdAt = currentDate,
            updatedAt = currentDate
        )

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

        val sampleAttributes = listOf(
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
            attributes = sampleAttributes
        )

        ItemEditDialog(
            item = sampleItem,
            onDismiss = { /* 預覽中不執行任何操作 */ },
            onSave = { /* 預覽中不執行任何操作 */ },
            onAddImage = { /* 預覽中不執行任何操作 */ },
            onDeleteImage = { /* 預覽中不執行任何操作 */ }
        )
    }
}
