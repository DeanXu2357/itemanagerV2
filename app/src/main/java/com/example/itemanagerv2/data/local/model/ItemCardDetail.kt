package com.example.itemanagerv2.data.local.model

import com.example.itemanagerv2.data.local.entity.Image
import com.example.itemanagerv2.data.local.entity.ItemAttributeValue
import com.example.itemanagerv2.data.local.entity.ItemCategory


data class ItemCardDetail(
    val id: Int,
    val name: String,
    val categoryId: Int,
    val codeType: String?,
    val codeContent: String?,
    val codeImageId: Int?,
    val coverImageId: Int?,
    val createdAt: Long,
    val updatedAt: Long,
    val category: ItemCategoryNavArg?,
    val codeImage: Image?,
    val coverImage: Image?,
    val images: List<Image>,
    val attributes: List<ItemAttributeValue>
)