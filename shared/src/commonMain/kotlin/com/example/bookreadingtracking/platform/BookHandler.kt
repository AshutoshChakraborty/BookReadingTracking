package com.example.bookreadingtracking.platform

import com.example.bookreadingtracking.model.BookMetadata

interface BookHandler {
    suspend fun pickBookFile(): String?
    fun openBookFile(filePath: String)
    suspend fun extractMetadata(filePath: String): BookMetadata?
    fun getReadingProgress(filePath: String): Int?
}

expect fun getBookHandler(): BookHandler
