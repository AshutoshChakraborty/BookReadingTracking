package com.example.bookreadingtracking.platform

import android.content.Context
import com.example.bookreadingtracking.model.Book
import com.example.bookreadingtracking.model.DataState
import com.example.bookreadingtracking.model.Plan
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AndroidPersistenceHandler(private val context: Context) : PersistenceHandler {
    private val prefs = context.getSharedPreferences("book_tracker_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    override fun saveData(data: DataState) {
        val content = json.encodeToString(data)
        prefs.edit().putString("app_data", content).apply()
    }

    override fun loadData(): DataState {
        val content = prefs.getString("app_data", null)
        if (content != null) {
            return try {
                json.decodeFromString<DataState>(content)
            } catch (e: Exception) {
                DataState()
            }
        }
        
        // Migration check
        val oldBooksContent = prefs.getString("books_list", null)
        if (oldBooksContent != null) {
            return try {
                val oldBooks = json.decodeFromString<List<Book>>(oldBooksContent)
                val defaultPlan = Plan("default", "Initial Plan")
                DataState(
                    plans = listOf(defaultPlan),
                    books = oldBooks.map { it.copy(planId = defaultPlan.id) }
                )
            } catch (e: Exception) {
                DataState()
            }
        }
        
        return DataState()
    }
}

actual fun getPersistenceHandler(): PersistenceHandler = AndroidPersistenceHandler(androidContext)
