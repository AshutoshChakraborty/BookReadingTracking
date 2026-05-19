package com.example.bookreadingtracking.model

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Book(
    val id: String,
    val filePath: String,
    val metadata: BookMetadata,
    val currentPage: Int = 0,
    val totalTimeSpentMillis: Long = 0,
    val lastReadTime: Long = 0
)

@Serializable
data class BookMetadata(
    val title: String,
    val author: String,
    val pageCount: Int,
    @Transient
    val thumbnail: ImageBitmap? = null
)
