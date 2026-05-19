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

class AndroidPdfHandler(private val context: Context) : PdfHandler {
    
    // In a real app, we'd use ActivityResultLauncher. 
    // For simplicity in this demo/personal app, we'll assume a path is provided or handle it via a common mechanism.
    // However, since pickPdf needs to return a string path/URI, and SAF returns a URI, 
    // we might need to change the interface or handle URIs.
    
    override suspend fun pickPdf(): String? {
        // This is tricky without an Activity context and ResultLauncher.
        // For now, return null and we'll address how to trigger the picker from the UI.
        return null 
    }

    override fun openPdf(filePath: String) {
        val uri = Uri.parse(filePath)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    }

    override suspend fun extractMetadata(filePath: String): BookMetadata? {
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
                title = filePath.substringAfterLast("/"), // Simple title from URI
                author = "Unknown",
                pageCount = pageCount,
                thumbnail = bitmap.asImageBitmap()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// We need a way to provide the context. In KMP Android, this is often done via a singleton or platform initialization.
// For this task, I'll assume a way to set or provide the context.
lateinit var androidContext: Context

actual fun getPdfHandler(): PdfHandler = AndroidPdfHandler(androidContext)
