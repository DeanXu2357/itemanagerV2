package com.example.itemanagerv2.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.itemanagerv2.ui.theme.BaseTheme

@Composable
fun InsertFAB(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onManualAdd: () -> Unit,
    onScanAdd: () -> Unit,
    onCategoryManage: () -> Unit // 新增參數
) {
    val rotation by animateFloatAsState(targetValue = if (isExpanded) 45f else 0f, label = "")

    Column(horizontalAlignment = Alignment.Companion.End) {
        if (isExpanded) {
            SmallFloatingActionButton(
                onClick = {
                    onManualAdd()
                    onExpandedChange(false)
                },
                modifier = Modifier.Companion.padding(bottom = 16.dp)
            ) { Icon(Icons.Filled.Edit, contentDescription = "Manual Add") }
            SmallFloatingActionButton(
                onClick = {
                    onScanAdd()
                    onExpandedChange(false)
                },
                modifier = Modifier.Companion.padding(bottom = 16.dp)
            ) { Icon(Icons.Filled.QrCodeScanner, contentDescription = "Scan Add") }
            SmallFloatingActionButton(
                onClick = {
                    onCategoryManage()
                    onExpandedChange(false)
                },
                modifier = Modifier.Companion.padding(bottom = 16.dp)
            ) { Icon(Icons.Filled.Category, contentDescription = "Manage Categories") }
        }
        FloatingActionButton(onClick = { onExpandedChange(!isExpanded) }) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Expand Speed Dial",
                modifier = Modifier.Companion.rotate(rotation)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InsertFABPreview() {
    BaseTheme {
        Surface {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    // FAB Close
                    InsertFAB(
                        isExpanded = false,
                        onExpandedChange = {},
                        onManualAdd = {},
                        onScanAdd = {},
                        onCategoryManage = {}
                    )

                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))

                    // FAB Extend
                    InsertFAB(
                        isExpanded = true,
                        onExpandedChange = {},
                        onManualAdd = {},
                        onScanAdd = {},
                        onCategoryManage = {}
                    )
                }
            }
        }
    }
}
