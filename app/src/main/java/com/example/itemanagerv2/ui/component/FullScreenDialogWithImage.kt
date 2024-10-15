package com.example.itemanagerv2.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.itemanagerv2.ui.theme.BaseTheme


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

@Preview
@Composable
fun FullScreenEditDialogWithImagesPreview() {
    BaseTheme {
        FullScreenEditDialogWithImages(
            onDismiss = {},
            onSave = {},
            images = listOf(
                "https://example.com/image1.jpg",
                "https://example.com/image2.jpg",
                "https://example.com/image3.jpg"
            ),
            onAddImage = {},
            onRemoveImage = {},
            onSetMainImage = {}
        )
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
