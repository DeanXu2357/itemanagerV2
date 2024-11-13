package com.example.itemanagerv2.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.itemanagerv2.data.local.model.ItemCardDetail

@Composable
fun ItemCard(
    cardDetail: ItemCardDetail,
    onEdit: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var cardWidth by remember { mutableStateOf(0.dp) }
    var menuWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Card(
        modifier =
        Modifier.Companion
            .fillMaxWidth()
            .height(200.dp)
            .padding(8.dp)
            .onGloballyPositioned { coordinates ->
                cardWidth = with(density) { coordinates.size.width.toDp() }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.Companion.fillMaxSize()) {
            AsyncImage(
                model = cardDetail.coverImage?.filePath ?: "",
                contentDescription = cardDetail.name,
                contentScale = ContentScale.Companion.Crop,
                modifier = Modifier.Companion.fillMaxSize()
            )

            Text(
                text = cardDetail.name,
                modifier = Modifier.Companion
                    .align(Alignment.Companion.BottomStart)
                    .padding(16.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )

            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.Companion.align(Alignment.Companion.BottomEnd)
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                offset = DpOffset(x = cardWidth - menuWidth, y = 0.dp),
                modifier =
                Modifier.Companion.onGloballyPositioned { coordinates ->
                    menuWidth = with(density) { coordinates.size.width.toDp() }
                }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        onEdit()
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Duplicate") },
                    onClick = {
                        onCopy()
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        onDelete()
                        showMenu = false
                    }
                )
            }
        }
    }
}