package com.example.bookreadingtracking.platform

import com.example.bookreadingtracking.model.BookMetadata

class WasmBookHandler : BookHandler {
    override suspend fun pickBookFile(): String? = null
    override fun openBookFile(filePath: String) {
        // Not implemented for Wasm
    }
    override suspend fun extractMetadata(filePath: String): BookMetadata? = null
    override fun getReadingProgress(filePath: String): Int? = null
}

actual fun getBookHandler(): BookHandler = WasmBookHandler()
