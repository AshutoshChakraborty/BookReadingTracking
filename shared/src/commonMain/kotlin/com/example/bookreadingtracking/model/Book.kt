package com.example.bookreadingtracking.model

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
enum class ReadingStatus {
    TO_READ, READING, PAUSED, READ
}

@Serializable
data class Book(
    val id: String,
    val planId: String, // Associated plan
    val filePath: String,
    val metadata: BookMetadata,
    val currentPage: Int = 0,
    val totalTimeSpentMillis: Long = 0,
    val lastReadTime: Long = 0,
    val status: ReadingStatus = ReadingStatus.TO_READ,
    val notes: String = "" // New field for book notes
)

@Serializable
data class BookMetadata(
    val title: String,
    val author: String,
    val pageCount: Int,
    @Transient
    val thumbnail: ImageBitmap? = null
)
