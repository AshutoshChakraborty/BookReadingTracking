package com.example.bookreadingtracking.model

import androidx.compose.ui.graphics.ImageBitmap

data class Book(
    val id: String,
    val filePath: String,
    val metadata: BookMetadata
)

data class BookMetadata(
    val title: String,
    val author: String,
    val pageCount: Int,
    val thumbnail: ImageBitmap? = null
)
