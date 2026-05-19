package com.example.bookreadingtracking.platform

import android.content.Context
import com.example.bookreadingtracking.model.Book
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AndroidPersistenceHandler(private val context: Context) : PersistenceHandler {
    private val prefs = context.getSharedPreferences("book_tracker_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    override fun saveBooks(books: List<Book>) {
        val content = json.encodeToString(books)
        prefs.edit().putString("books_list", content).apply()
    }

    override fun loadBooks(): List<Book> {
        val content = prefs.getString("books_list", null) ?: return emptyList()
        return try {
            json.decodeFromString(content)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

actual fun getPersistenceHandler(): PersistenceHandler = AndroidPersistenceHandler(androidContext)
