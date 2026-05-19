package com.example.bookreadingtracking.platform

import com.example.bookreadingtracking.model.BookMetadata

class IosPdfHandler : PdfHandler {
    override suspend fun pickPdf(): String? = null
    override fun openPdf(filePath: String) {
        // Not implemented for iOS
    }
    override suspend fun extractMetadata(filePath: String): BookMetadata? = null
    override fun getReadingProgress(filePath: String): Int? = null
}

actual fun getPdfHandler(): PdfHandler = IosPdfHandler()
