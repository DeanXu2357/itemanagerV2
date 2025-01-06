package com.example.itemanagerv2.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.itemanagerv2.data.local.entity.CategoryAttribute
import com.example.itemanagerv2.data.local.entity.Image
import com.example.itemanagerv2.data.local.entity.ItemAttributeValue
import com.example.itemanagerv2.data.local.model.ItemCardDetail
import com.example.itemanagerv2.data.local.model.ItemCategoryArg
import com.example.itemanagerv2.ui.theme.BaseTheme
import java.util.Date

@Composable
fun ItemViewDialog(
        item: ItemCardDetail,
        categoryAttributes: List<CategoryAttribute>,
        onDismiss: () -> Unit,
        onEdit: () -> Unit
) {
    var isDetailExpanded by remember { mutableStateOf(true) }
    var isQRCodeExpanded by remember { mutableStateOf(true) }

    Scaffold(
            topBar = {
                Surface(
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        color = MaterialTheme.colorScheme.surface,
                ) {
                    Box(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                    ) {
                        IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.align(Alignment.Center),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                        )

                        IconButton(
                                onClick = onEdit,
                                modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit",
                                    tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).verticalScroll(rememberScrollState())) {
            if (item.images.isNotEmpty()) {
                ImageCarousel(images = item.images, selectedCoverImageId = item.coverImageId)
            }

            // Details Section
            ExpandableSection(
                    title = "Details",
                    isExpanded = isDetailExpanded,
                    onExpandToggle = { isDetailExpanded = !isDetailExpanded }
            ) {
                Column(
                        modifier =
                                Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()
                ) {
                    // Category
                    Text(
                            text = "Category: ${item.category!!.name}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Display attributes
                    categoryAttributes.filter { it.categoryId == item.categoryId }.forEach {
                            attribute ->
                        val value =
                                item.attributes.find { it.attributeId == attribute.id }?.value
                                        ?: attribute.defaultValue ?: "-"

                        Text(
                                text = "${attribute.name}: $value",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }

            // QR Code Section
            if (!item.codeType.isNullOrBlank() || !item.codeContent.isNullOrBlank()) {
                ExpandableSection(
                        title = "QR Code",
                        isExpanded = isQRCodeExpanded,
                        onExpandToggle = { isQRCodeExpanded = !isQRCodeExpanded }
                ) {
                    Column(
                            modifier =
                                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                            .fillMaxWidth()
                    ) {
                        if (!item.codeType.isNullOrBlank()) {
                            Text(
                                    text = "Barcode Type: ${item.codeType}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        if (!item.codeContent.isNullOrBlank()) {
                            Text(
                                    text = "Barcode Content: ${item.codeContent}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ItemViewDialogPreview() {
    val currentDate = Date()
    val sampleCategory = ItemCategoryArg(1, "Electronics")

    val sampleImages =
            listOf(
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

    val sampleAttributes =
            listOf(
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
                    )
            )

    val sampleAttributeValues =
            listOf(
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

    val sampleItem =
            ItemCardDetail(
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

    BaseTheme {
        ItemViewDialog(
                item = sampleItem,
                categoryAttributes = sampleAttributes,
                onDismiss = {},
                onEdit = {}
        )
    }
}
