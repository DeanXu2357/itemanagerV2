package com.example.itemanagerv2.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.itemanagerv2.data.local.entity.Image
import java.util.Date
import kotlin.math.absoluteValue
import kotlinx.coroutines.launch

@Composable
fun ImageCarousel(
    images: List<Image>,
    selectedCoverImageId: Int?,
    modifier: Modifier = Modifier
) {
    if (images.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { images.size })
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                val pageOffset = (page - pagerState.currentPage).toFloat()
                val scale = animateFloatAsState(
                    targetValue = if (pageOffset == 0f) 1f else 0.8f,
                    label = "scale"
                )

                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                            alpha = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    AsyncImage(
                        model = images[page].filePath,
                        contentDescription = "Image ${page + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // Carousel indicator
        if (images.size > 1) {
            Row(
                Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    }
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(iteration)
                                }
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun MultiPreviewImageCarousel(
    images: List<Image>,
    onAddClick: () -> Unit,
    onDeleteClick: (Int) -> Unit,
    onSetCover: (Int) -> Unit,
    selectedCoverImageId: Int?
) {
    // Local state for preview display
    var displayedImages by remember(images) { mutableStateOf(images) }
    var displayedCoverImageId by remember(selectedCoverImageId) { mutableStateOf(selectedCoverImageId) }

    val pagerState = rememberPagerState(pageCount = { displayedImages.size + 1 })
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                if (page < displayedImages.size) {
                    val pageOffset = (page - pagerState.currentPage).toFloat()
                    val scale = animateFloatAsState(
                        targetValue = if (pageOffset == 0f) 1f else 0.8f,
                        label = "scale"
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale.value
                                scaleY = scale.value
                                alpha = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = displayedImages[page].filePath,
                                contentDescription = "Image $page",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Action buttons row
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Set as cover button
                                IconButton(
                                    onClick = {
                                        val imageId = displayedImages[page].id
                                        displayedCoverImageId = imageId
                                        onSetCover(imageId)
                                    },
                                    modifier = Modifier.background(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                        shape = CircleShape
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Image,
                                        contentDescription = "Set as cover",
                                        tint = if (displayedImages[page].id == displayedCoverImageId)
                                            MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Delete button
                                IconButton(
                                    onClick = {
                                        val imageId = displayedImages[page].id
                                        // Update preview display first
                                        displayedImages = displayedImages.filter { it.id != imageId }
                                        if (imageId == displayedCoverImageId) {
                                            displayedCoverImageId = null
                                        }
                                        // Then notify parent
                                        onDeleteClick(imageId)
                                    },
                                    modifier = Modifier.background(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                        shape = CircleShape
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Add button
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            FloatingActionButton(
                                onClick = onAddClick,
                                modifier = Modifier.size(56.dp)
                            ) { Icon(Icons.Default.Add, contentDescription = "Add Image") }
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
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                }
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(iteration)
                            }
                        }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ImageCarouselPreview() {
    MaterialTheme {
        val currentDate = Date()
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

        ImageCarousel(
            images = sampleImages,
            selectedCoverImageId = sampleImages.firstOrNull()?.id
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MultiPreviewImageCarouselPreview() {
    MaterialTheme {
        val currentDate = Date()
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

        MultiPreviewImageCarousel(
            images = sampleImages,
            onAddClick = { /* Preview: Do nothing */ },
            onDeleteClick = { /* Preview: Do nothing */ },
            onSetCover = { /* Preview: Do nothing */ },
            selectedCoverImageId = sampleImages.firstOrNull()?.id
        )
    }
}
