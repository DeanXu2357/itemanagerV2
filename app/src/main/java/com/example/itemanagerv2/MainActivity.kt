package com.example.itemanagerv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.itemanagerv2.ui.theme.BaseTheme
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : ComponentActivity() {
    private lateinit var objectsRecyclerView: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(
//            ComposeView(this).apply {
//                setContent {
//                    // Add Compose content here
//                    BaseTheme {
//                        Surface(
//                            modifier = Modifier.fillMaxSize(),
//                            color = MaterialTheme.colorScheme.background
//                        ) {
////                            Greeting("Android")
//                        }
//                    }
//                }
//            }
//        )
//        setContentView(R.layout.activity_main)
        setContent{
            BaseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent()
                }
            }
        }

        objectsRecyclerView = findViewById(R.id.objectsRecyclerView)
        bottomNavigation = findViewById(R.id.bottomNavigation)

//        setupRecyclerView()
//        setupBottomNavigation()
    }

//    private fun setupRecyclerView() {
//        val objects = listOf(
//            ObjectItem("Bike", "https://example.com/bike.jpg"),
//            ObjectItem("TV", "https://example.com/tv.jpg"),
//            ObjectItem("Chair", "https://example.com/chair.jpg"),
//            ObjectItem("Laptop", "https://example.com/laptop.jpg")
//        )
//
//        objectsRecyclerView.layoutManager = GridLayoutManager(this, 2)
//        objectsRecyclerView.adapter = ObjectAdapter(objects)
//    }
//
//    private fun setupBottomNavigation() {
//        bottomNavigation.setOnItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.navigation_home -> {
//                    // Handle home navigation
//                    true
//                }
//
//                R.id.navigation_objects -> {
//                    // Handle objects navigation
//                    true
//                }
//
//                R.id.navigation_add -> {
//                    // Handle add new navigation
//                    true
//                }
//
//                R.id.navigation_alerts -> {
//                    // Handle alerts navigation
//                    true
//                }
//
//                R.id.navigation_profile -> {
//                    // Handle profile navigation
//                    true
//                }
//
//                else -> false
//            }
//        }
//    }
}

@Composable
fun MainContent() {
    var selectedItem by remember { mutableStateOf(0) }

    Column {
        // RecyclerView
        AndroidView(
            factory = { context ->
                RecyclerView(context).apply {
                    layoutManager = GridLayoutManager(context, 2)
                    adapter = ObjectAdapter(
                        listOf(
                            ObjectItem("Bike", "https://example.com/bike.jpg"),
                            ObjectItem("TV", "https://example.com/tv.jpg"),
                            ObjectItem("Chair", "https://example.com/chair.jpg"),
                            ObjectItem("Laptop", "https://example.com/laptop.jpg")
                        )
                    )
                }
            },
            modifier = Modifier.weight(1f)
        )

        // Bottom Navigation
        AndroidView(
            factory = { context ->
                BottomNavigationView(context).apply {
                    inflateMenu(R.menu.bottom_nav_menu)
                    setOnItemSelectedListener { item ->
                        selectedItem = item.itemId
                        true
                    }
                }
            }
        )
    }
}