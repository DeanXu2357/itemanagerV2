package com.example.itemanagerv2

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var objectsRecyclerView: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        objectsRecyclerView = findViewById(R.id.objectsRecyclerView)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        setupRecyclerView()
        setupBottomNavigation()
    }

    private fun setupRecyclerView() {
        val objects = listOf(
            ObjectItem("Bike", "https://example.com/bike.jpg"),
            ObjectItem("TV", "https://example.com/tv.jpg"),
            ObjectItem("Chair", "https://example.com/chair.jpg"),
            ObjectItem("Laptop", "https://example.com/laptop.jpg")
        )

        objectsRecyclerView.layoutManager = GridLayoutManager(this, 2)
        objectsRecyclerView.adapter = ObjectAdapter(objects)
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Handle home navigation
                    true
                }

                R.id.navigation_objects -> {
                    // Handle objects navigation
                    true
                }

                R.id.navigation_add -> {
                    // Handle add new navigation
                    true
                }

                R.id.navigation_alerts -> {
                    // Handle alerts navigation
                    true
                }

                R.id.navigation_profile -> {
                    // Handle profile navigation
                    true
                }

                else -> false
            }
        }
    }
}