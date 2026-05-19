package com.example.bookreadingtracking.platform

import com.example.bookreadingtracking.model.BookMetadata

interface PdfHandler {
    suspend fun pickPdf(): String?
    fun openPdf(filePath: String)
    suspend fun extractMetadata(filePath: String): BookMetadata?
}

expect fun getPdfHandler(): PdfHandler
