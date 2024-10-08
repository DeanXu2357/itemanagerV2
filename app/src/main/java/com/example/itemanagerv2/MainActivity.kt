package com.example.itemanagerv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.gestures.detectTapGestures
import com.example.itemanagerv2.ui.theme.BaseTheme
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BaseTheme {
                MainContent()
            }
        }
    }
}


@Composable
fun MainContent() {
    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = "My Items",
                onSearchClick = { /* TODO: 實現搜索功能 */ }
            )
        },
        bottomBar = {
            var selectedItem by remember { mutableIntStateOf(0) }
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    selected = selectedItem == 0,
                    onClick = { selectedItem = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Add, contentDescription = "Insert") },
                    selected = selectedItem == 1,
                    onClick = { selectedItem = 1 }
                )
            }
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = innerPadding,
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                listOf(
                    ObjectItem("Bike", "https://example.com/bike.jpg"),
                    ObjectItem("TV", "https://example.com/tv.jpg"),
                    ObjectItem("Chair", "https://example.com/chair.jpg"),
                    ObjectItem("Laptop", "https://example.com/laptop.jpg")
                )
            ) { item ->
                ItemCard(
                    item = item,
                    onEdit = { /* TODO: 實現編輯功能 */ },
                    onCopy = { /* TODO: 實現複製功能 */ },
                    onDelete = { /* TODO: 實現刪除功能 */ },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    BaseTheme {
        MainContent()
    }
}


@Composable
fun ItemCard(
    item: ObjectItem,
    onEdit: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var longPressActive by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .aspectRatio(1.618f)
            .padding(4.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        longPressActive = true
                        showMenu = true
                    },
                    onTap = { /* 一般點擊的處理 */ }
                )
            },
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Text(
                text = item.name,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("編輯") },
                    onClick = {
                        onEdit()
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("複製") },
                    onClick = {
                        onCopy()
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("刪除") },
                    onClick = {
                        onDelete()
                        showMenu = false
                    }
                )
            }
        }
    }
}

@Composable
fun CustomTopAppBar(title: String, onSearchClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.align(Alignment.Center)
            )
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}