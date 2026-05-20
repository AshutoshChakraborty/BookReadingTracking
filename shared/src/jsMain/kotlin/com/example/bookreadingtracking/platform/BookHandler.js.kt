package com.example.bookreadingtracking.platform

import com.example.bookreadingtracking.model.BookMetadata

class JsBookHandler : BookHandler {
    override suspend fun pickBookFile(): String? = null
    override fun openBookFile(filePath: String) {
        // Not implemented for JS
    }
    override suspend fun extractMetadata(filePath: String): BookMetadata? = null
    override fun getReadingProgress(filePath: String): Int? = null
}

actual fun getBookHandler(): BookHandler = JsBookHandler()
