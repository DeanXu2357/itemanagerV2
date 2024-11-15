package com.example.itemanagerv2.data.local.model

import android.os.Parcelable
import com.example.itemanagerv2.data.local.entity.ItemCategory
import kotlinx.parcelize.Parcelize

@Parcelize
data class ItemCategoryNavArg(
    val id: Int,
    val name: String
) : Parcelable

fun ItemCategory.toNavArg() = ItemCategoryNavArg(
    id = id,
    name = name
)
