package com.example.bookreadingtracking.platform

import com.example.bookreadingtracking.model.Book
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class JvmPersistenceHandler : PersistenceHandler {
    private val saveFile = File(System.getProperty("user.home"), ".bookreadingtracker_books.json")
    private val json = Json { ignoreUnknownKeys = true }

    override fun saveBooks(books: List<Book>) {
        try {
            val content = json.encodeToString(books)
            saveFile.writeText(content)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun loadBooks(): List<Book> {
        return try {
            if (saveFile.exists()) {
                val content = saveFile.readText()
                json.decodeFromString(content)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

actual fun getPersistenceHandler(): PersistenceHandler = JvmPersistenceHandler()
