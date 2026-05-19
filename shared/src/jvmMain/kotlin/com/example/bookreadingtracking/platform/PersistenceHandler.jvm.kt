package com.example.bookreadingtracking.platform

import com.example.bookreadingtracking.model.Book
import com.example.bookreadingtracking.model.DataState
import com.example.bookreadingtracking.model.Plan
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class JvmPersistenceHandler : PersistenceHandler {
    private val saveFile = File(System.getProperty("user.home"), ".bookreadingtracker_data.json")
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    override fun saveData(data: DataState) {
        try {
            val content = json.encodeToString(data)
            saveFile.writeText(content)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun loadData(): DataState {
        return try {
            if (saveFile.exists()) {
                val content = saveFile.readText()
                try {
                    // Try loading as DataState
                    json.decodeFromString<DataState>(content)
                } catch (e: Exception) {
                    // Migration: Try loading as List<Book>
                    val oldBooks = json.decodeFromString<List<Book>>(content)
                    val defaultPlan = Plan("default", "Initial Plan")
                    DataState(
                        plans = listOf(defaultPlan),
                        books = oldBooks.map { it.copy(planId = defaultPlan.id) }
                    )
                }
            } else {
                DataState()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            DataState()
        }
    }
}

actual fun getPersistenceHandler(): PersistenceHandler = JvmPersistenceHandler()
