package com.example.bookreadingtracking.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookreadingtracking.model.Book
import com.example.bookreadingtracking.platform.getPdfHandler
import com.example.bookreadingtracking.platform.getPersistenceHandler
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class BookViewModel : ViewModel() {
    private val pdfHandler = getPdfHandler()
    private val persistenceHandler = getPersistenceHandler()
    val books = mutableStateListOf<Book>()
    
    init {
        loadBooks()
    }

    private fun loadBooks() {
        viewModelScope.launch {
            val loadedBooks = persistenceHandler.loadBooks()
            books.clear()
            books.addAll(loadedBooks)
            
            // Re-extract thumbnails for loaded books
            loadedBooks.forEachIndexed { index, book ->
                val metadata = pdfHandler.extractMetadata(book.filePath)
                if (metadata != null) {
                    books[index] = book.copy(metadata = metadata)
                }
            }
        }
    }

    private fun saveBooks() {
        persistenceHandler.saveBooks(books.toList())
    }

    // Track which book is currently being read to calculate time
    var activeBookId by mutableStateOf<String?>(null)
        private set
    private var startTimeMillis: Long = 0

    fun addPdf() {
        viewModelScope.launch {
            val path = pdfHandler.pickPdf()
            if (path != null) {
                val metadata = pdfHandler.extractMetadata(path)
                if (metadata != null) {
                    val progress = pdfHandler.getReadingProgress(path) ?: 0
                    books.add(Book(
                        id = path,
                        filePath = path,
                        metadata = metadata,
                        currentPage = progress
                    ))
                    saveBooks()
                }
            }
        }
    }

    fun startReading(book: Book) {
        stopReading() // Stop any previous session
        activeBookId = book.id
        startTimeMillis = Clock.System.now().toEpochMilliseconds()
        pdfHandler.openPdf(book.filePath)
    }

    fun stopReading() {
        val id = activeBookId ?: return
        val now = Clock.System.now().toEpochMilliseconds()
        val duration = now - startTimeMillis
        
        val index = books.indexOfFirst { it.id == id }
        if (index != -1) {
            val book = books[index]
            val updatedBook = book.copy(
                totalTimeSpentMillis = book.totalTimeSpentMillis + duration,
                lastReadTime = now
            )
            books[index] = updatedBook
            refreshProgress(updatedBook) // Try to sync progress after reading
            saveBooks()
        }
        activeBookId = null
    }

    fun refreshProgress(book: Book) {
        val progress = pdfHandler.getReadingProgress(book.filePath) ?: return
        val index = books.indexOfFirst { it.id == book.id }
        if (index != -1) {
            books[index] = books[index].copy(currentPage = progress)
            saveBooks()
        }
    }

    fun refreshAllProgress() {
        books.forEachIndexed { index, book ->
            val progress = pdfHandler.getReadingProgress(book.filePath)
            if (progress != null) {
                books[index] = book.copy(currentPage = progress)
            }
        }
        saveBooks()
    }

    fun removeBook(book: Book) {
        books.remove(book)
        saveBooks()
    }
}
