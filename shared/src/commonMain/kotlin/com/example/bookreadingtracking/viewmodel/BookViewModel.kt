package com.example.bookreadingtracking.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookreadingtracking.model.Book
import com.example.bookreadingtracking.platform.getPdfHandler
import kotlinx.coroutines.launch

class BookViewModel : ViewModel() {
    private val pdfHandler = getPdfHandler()
    val books = mutableStateListOf<Book>()

    fun addPdf() {
        viewModelScope.launch {
            val path = pdfHandler.pickPdf()
            if (path != null) {
                val metadata = pdfHandler.extractMetadata(path)
                if (metadata != null) {
                    books.add(Book(
                        id = path, // Using path as ID for simplicity
                        filePath = path,
                        metadata = metadata
                    ))
                }
            }
        }
    }

    fun openBook(book: Book) {
        pdfHandler.openPdf(book.filePath)
    }

    fun removeBook(book: Book) {
        books.remove(book)
    }
}
