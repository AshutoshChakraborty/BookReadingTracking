package com.example.bookreadingtracking.platform

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.ui.graphics.asImageBitmap
import com.example.bookreadingtracking.model.BookMetadata
import java.io.File
import java.io.FileOutputStream

class AndroidBookHandler(private val context: Context) : BookHandler {
    
    override suspend fun pickBookFile(): String? {
        // Still returns null as we need Activity context to launch picker.
        // In a real app, this would be handled by the UI layer or a specialized class.
        return null 
    }

    override fun openBookFile(filePath: String) {
        val uri = Uri.parse(filePath)
        val type = if (filePath.lowercase().contains(".epub")) "application/epub+zip" else "application/pdf"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, type)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    }

    override suspend fun extractMetadata(filePath: String): BookMetadata? {
        if (filePath.lowercase().contains(".pdf")) {
            return extractPdfMetadata(filePath)
        } else if (filePath.lowercase().contains(".epub")) {
            return extractEpubMetadata(filePath)
        }
        return null
    }

    private fun extractPdfMetadata(filePath: String): BookMetadata? {
        return try {
            val uri = Uri.parse(filePath)
            val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return null
            val renderer = PdfRenderer(pfd)
            val pageCount = renderer.pageCount
            
            val page = renderer.openPage(0)
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            renderer.close()

            BookMetadata(
                title = filePath.substringAfterLast("/"),
                author = "Unknown",
                pageCount = pageCount,
                thumbnail = bitmap.asImageBitmap()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun extractEpubMetadata(filePath: String): BookMetadata? {
        return BookMetadata(
            title = filePath.substringAfterLast("/"),
            author = "Unknown",
            pageCount = 0,
            thumbnail = null
        )
    }

    override fun getReadingProgress(filePath: String): Int? {
        return null 
    }
}

lateinit var androidContext: Context

actual fun getBookHandler(): BookHandler = AndroidBookHandler(androidContext)
