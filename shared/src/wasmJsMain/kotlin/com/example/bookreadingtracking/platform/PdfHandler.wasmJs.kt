package com.example.bookreadingtracking.platform

import com.example.bookreadingtracking.model.BookMetadata

class WasmPdfHandler : PdfHandler {
    override suspend fun pickPdf(): String? = null
    override fun openPdf(filePath: String) {
        // Not implemented for Wasm
    }
    override suspend fun extractMetadata(filePath: String): BookMetadata? = null
    override fun getReadingProgress(filePath: String): Int? = null
}

actual fun getPdfHandler(): PdfHandler = WasmPdfHandler()
