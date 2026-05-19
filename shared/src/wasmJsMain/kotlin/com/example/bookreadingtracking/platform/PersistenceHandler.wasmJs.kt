package com.example.bookreadingtracking.platform

import com.example.bookreadingtracking.model.Book
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.browser.window

class WasmPersistenceHandler : PersistenceHandler {
    private val key = "bookreadingtracker_books"
    private val json = Json { ignoreUnknownKeys = true }

    override fun saveBooks(books: List<Book>) {
        try {
            val content = json.encodeToString(books)
            window.localStorage.setItem(key, content)
        } catch (e: Exception) {
            println("Error saving books: ${e.message}")
        }
    }

    override fun loadBooks(): List<Book> {
        return try {
            val content = window.localStorage.getItem(key)
            if (content != null) {
                json.decodeFromString(content)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("Error loading books: ${e.message}")
            emptyList()
        }
    }
}

actual fun getPersistenceHandler(): PersistenceHandler = WasmPersistenceHandler()
