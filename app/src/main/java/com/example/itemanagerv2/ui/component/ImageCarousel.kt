package com.example.itemanagerv2.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun CustomImageCarousel(
    images: List<String>,
    onAddClick: () -> Unit,
    onDeleteClick: (Int) -> Unit
) {
    var currentPage by remember { mutableStateOf(0) }
    var dragOffset by remember { mutableStateOf(0f) }
    var componentWidth by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { size ->
                componentWidth = size.width
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (componentWidth > 0 && dragOffset.absoluteValue > componentWidth / 3) {
                                if (dragOffset > 0 && currentPage > 0) {
                                    currentPage--
                                } else if (dragOffset < 0 && currentPage < images.size) {
                                    currentPage++
                                }
                            }
                            coroutineScope.launch {
                                dragOffset = 0f
                            }
                        },
                        onDragCancel = {
                            coroutineScope.launch {
                                dragOffset = 0f
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            dragOffset += dragAmount
                            change.consume()
                        }
                    )
                }
        ) {
            for (index in images.indices) {
                val imageUrl = images[index]
                val pageOffset = if (componentWidth > 0) {
                    (index - currentPage - dragOffset / componentWidth).coerceIn(-1f, 1f)
                } else {
                    0f
                }
                val scale = animateFloatAsState(if (pageOffset == 0f) 1f else 0.8f)

                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                            alpha = 1f - pageOffset.absoluteValue
                            translationX = componentWidth * pageOffset
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Image $index",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { onDeleteClick(index) },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }

            // Add button
            if (currentPage == images.size) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        FloatingActionButton(
                            onClick = onAddClick,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Image")
                        }
                    }
                }
            }
        }

        // Carousel indicator
        Row(
            Modifier
                .height(50.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            for (iteration in 0..images.size) {
                val color = if (currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CustomImageCarouselPreview() {
    MaterialTheme {
        val sampleImages = listOf(
            "https://example.com/image1.jpg",
            "https://example.com/image2.jpg",
            "https://example.com/image3.jpg"
        )
        CustomImageCarousel(
            images = sampleImages,
            onAddClick = { /* Preview: Do nothing */ },
            onDeleteClick = { /* Preview: Do nothing */ }
        )
    }
}