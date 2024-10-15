package com.example.itemanagerv2.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.itemanagerv2.data.local.entity.Item
import com.example.itemanagerv2.ui.theme.BaseTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEditDialog(
    item: Item,
    onDismiss: () -> Unit,
    onSave: (Item) -> Unit
) {
    var editedItem by remember { mutableStateOf(item) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Item") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(onClick = { onSave(editedItem) }) {
                        Text("Save")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = editedItem.name,
                onValueChange = { editedItem = editedItem.copy(name = it) },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = "",
                onValueChange = { /* TODO:  */ },
                label = { Text("Content") },
                modifier = Modifier.fillMaxWidth()
            )
            // Add more fields as needed
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ItemEditDialogPreview() {
    BaseTheme {
        ItemEditDialog(
            item = Item(
                id = 1,
                name = "Sample Item",
                coverImageId = 1,
                categoryId = 1,
                codeType = "1",
                codeContent = "test",
                codeImageId = 2,
                createdAt = 3,
                updatedAt = 4,
            ),
            onDismiss = {},
            onSave = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenEditDialogWithImages(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    images: List<String>,
    onAddImage: () -> Unit,
    onRemoveImage: (String) -> Unit,
    onSetMainImage: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Item") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(onClick = onSave) {
                        Text("Save")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            ImageManagementSection(
                images = images,
                onAddImage = onAddImage,
                onRemoveImage = onRemoveImage,
                onSetMainImage = onSetMainImage
            )

            DetailEditSection()
        }
    }
}

@Composable
fun ImageManagementSection(
    images: List<String>,
    onAddImage: () -> Unit,
    onRemoveImage: (String) -> Unit,
    onSetMainImage: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Images", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    AddImageButton(onAddImage)
                }
                items(images) { image ->
                    ImageThumbnail(
                        imageUrl = image,
                        onRemove = { onRemoveImage(image) },
                        onSetMain = { onSetMainImage(image) }
                    )
                }
            }
        }
    }
}

@Composable
fun AddImageButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .padding(4.dp)
    ) {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Image")
        }
    }
}

@Composable
fun ImageThumbnail(
    imageUrl: String,
    onRemove: () -> Unit,
    onSetMain: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .padding(4.dp)
    ) {
        Image(
            painter = rememberImagePainter(imageUrl),
            contentDescription = "Item Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        ) {
            IconButton(
                onClick = onSetMain,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Set as Main",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove Image",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun DetailEditSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Details", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = "",
                onValueChange = { /* Update name */ },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = "",
                onValueChange = { /* Update brand */ },
                label = { Text("Brand") },
                modifier = Modifier.fillMaxWidth()
            )
            // Add more fields as needed
        }
    }
}

// Usage example
@Composable
fun MainScreen() {
    var showEditDialog by remember { mutableStateOf(false) }
    var images by remember { mutableStateOf(listOf<String>()) }

    Button(onClick = { showEditDialog = true }) {
        Text("Edit Item")
    }

    if (showEditDialog) {
        FullScreenEditDialogWithImages(
            onDismiss = { showEditDialog = false },
            onSave = {
                // Handle save logic
                showEditDialog = false
            },
            images = images,
            onAddImage = {
                // In a real app, you'd launch an image picker here
                images = images + "https://example.com/placeholder.jpg"
            },
            onRemoveImage = { imageUrl ->
                images = images.filter { it != imageUrl }
            },
            onSetMainImage = { imageUrl ->
                // Move the selected image to the front of the list
                images = listOf(imageUrl) + (images.filter { it != imageUrl })
            }
        )
    }
}