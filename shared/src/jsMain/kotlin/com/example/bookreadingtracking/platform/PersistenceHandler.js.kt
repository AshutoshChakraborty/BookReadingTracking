package com.example.bookreadingtracking.platform

import com.example.bookreadingtracking.model.Book
import com.example.bookreadingtracking.model.DataState
import com.example.bookreadingtracking.model.Plan
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.browser.window

class JsPersistenceHandler : PersistenceHandler {
    private val key = "bookreadingtracker_data"
    private val oldKey = "bookreadingtracker_books"
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    override fun saveData(data: DataState) {
        try {
            val content = json.encodeToString(data)
            window.localStorage.setItem(key, content)
        } catch (e: Exception) {
            println("Error saving data: ${e.message}")
        }
    }

    override fun loadData(): DataState {
        try {
            val content = window.localStorage.getItem(key)
            if (content != null) {
                return json.decodeFromString<DataState>(content)
            }

            // Migration
            val oldContent = window.localStorage.getItem(oldKey)
            if (oldContent != null) {
                val oldBooks = json.decodeFromString<List<Book>>(oldContent)
                val defaultPlan = Plan("default", "Initial Plan")
                return DataState(
                    plans = listOf(defaultPlan),
                    books = oldBooks.map { it.copy(planId = defaultPlan.id) }
                )
            }
        } catch (e: Exception) {
            println("Error loading data: ${e.message}")
        }
        return DataState()
    }
}

actual fun getPersistenceHandler(): PersistenceHandler = JsPersistenceHandler()
