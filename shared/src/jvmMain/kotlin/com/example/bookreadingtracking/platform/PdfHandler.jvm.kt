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

class JvmPdfHandler : PdfHandler {
    override suspend fun pickPdf(): String? = withContext(Dispatchers.IO) {
        val dialog = FileDialog(null as Frame?, "Select PDF", FileDialog.LOAD)
        dialog.file = "*.pdf"
        dialog.isVisible = true
        if (dialog.file != null) {
            File(dialog.directory, dialog.file).absolutePath
        } else {
            null
        }
    }

    override fun openPdf(filePath: String) {
        val file = File(filePath)
        if (file.exists() && Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file)
        }
    }

    override suspend fun extractMetadata(filePath: String): BookMetadata? = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            Loader.loadPDF(file).use { document ->
                val info = document.documentInformation
                val title = info.title ?: file.name
                val author = info.author ?: "Unknown"
                val pageCount = document.numberOfPages

                val renderer = PDFRenderer(document)
                val image = renderer.renderImageWithDPI(0, 72f) // Render first page at 72 DPI
                
                // Correct way to convert BufferedImage to ImageBitmap
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
}

actual fun getPdfHandler(): PdfHandler = JvmPdfHandler()
