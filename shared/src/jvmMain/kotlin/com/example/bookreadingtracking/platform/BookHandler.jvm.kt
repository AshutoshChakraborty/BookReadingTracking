package com.example.bookreadingtracking.platform

import com.example.bookreadingtracking.model.BookMetadata
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.apache.pdfbox.Loader
import org.apache.pdfbox.rendering.PDFRenderer
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.imageio.ImageIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JvmBookHandler : BookHandler {
    override suspend fun pickBookFile(): String? = withContext(Dispatchers.IO) {
        val dialog = FileDialog(null as Frame?, "Select Book", FileDialog.LOAD)
        // Allow both PDF and EPUB
        dialog.setFilenameFilter { _, name -> 
            name.lowercase().endsWith(".pdf") || name.lowercase().endsWith(".epub") 
        }
        dialog.isVisible = true
        if (dialog.file != null) {
            File(dialog.directory, dialog.file).absolutePath
        } else {
            null
        }
    }

    override fun openBookFile(filePath: String) {
        val file = File(filePath)
        if (file.exists() && Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file)
        }
    }

    override suspend fun extractMetadata(filePath: String): BookMetadata? = withContext(Dispatchers.IO) {
        if (filePath.lowercase().endsWith(".pdf")) {
            extractPdfMetadata(filePath)
        } else if (filePath.lowercase().endsWith(".epub")) {
            extractEpubMetadata(filePath)
        } else {
            null
        }
    }

    private fun extractPdfMetadata(filePath: String): BookMetadata? {
        return try {
            val file = File(filePath)
            Loader.loadPDF(file).use { document ->
                val info = document.documentInformation
                val title = info.title ?: file.name
                val author = info.author ?: "Unknown"
                val pageCount = document.numberOfPages

                val renderer = PDFRenderer(document)
                val image = renderer.renderImageWithDPI(0, 72f) // Render first page at 72 DPI
                
                val composeBitmap = image.toComposeImageBitmap()

                BookMetadata(
                    title = title,
                    author = author,
                    pageCount = pageCount,
                    thumbnail = composeBitmap
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun extractEpubMetadata(filePath: String): BookMetadata? {
        // Basic implementation for EPUB metadata without an external library
        // In a real app, we'd use an EPUB library.
        // For now, we'll just return basic info from the filename.
        val file = File(filePath)
        return BookMetadata(
            title = file.nameWithoutExtension,
            author = "Unknown",
            pageCount = 0, // Difficult to determine for EPUB without library
            thumbnail = null
        )
    }

    override fun getReadingProgress(filePath: String): Int? {
        if (!filePath.lowercase().endsWith(".pdf")) return null
        
        val appData = System.getenv("LOCALAPPDATA") ?: return null
        val settingsFile = File(appData, "SumatraPDF/SumatraPDF-settings.txt")
        if (!settingsFile.exists()) return null

        try {
            val lines = settingsFile.readLines()
            var foundFile = false
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.startsWith("FilePath = $filePath", ignoreCase = true)) {
                    foundFile = true
                }
                if (foundFile && trimmed.startsWith("PageNo =", ignoreCase = true)) {
                    return trimmed.substringAfter("=").trim().toIntOrNull()
                }
                if (foundFile && trimmed.startsWith("FilePath =", ignoreCase = true) && !trimmed.contains(filePath, ignoreCase = true)) {
                    foundFile = false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}

actual fun getBookHandler(): BookHandler = JvmBookHandler()
