package com.example.itemanagerv2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide


class ObjectAdapter(private val objects: List<ObjectItem>) :
    RecyclerView.Adapter<ObjectAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val objectImage: ImageView = view.findViewById(R.id.objectImage)
        val objectName: TextView = view.findViewById(R.id.objectName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_object, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val objectItem = objects[position]
        holder.objectName.text = objectItem.name
        Glide.with(holder.itemView.context)
            .load(objectItem.imageUrl)
            .centerCrop()
            .into(holder.objectImage)
    }

    override fun getItemCount() = objects.size
}

data class ObjectItem(
    val name: String,
    val imageUrl: String
)