package com.example.itemanagerv2.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.itemanagerv2.data.local.entity.Item
import com.example.itemanagerv2.ui.theme.BaseTheme

@Composable
fun ItemEditDialog(
    item: Item,
    onDismiss: () -> Unit,
    onSave: (Item) -> Unit
) {
    var editedItem by remember { mutableStateOf(item ?: Item(0, "", 1, "", "", null, null)) }

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
                .padding(16.dp)
        ) {
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
                    imageVector = Icons.Filled.ArrowBack,
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
