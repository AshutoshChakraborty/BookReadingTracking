package com.example.bookreadingtracking.platform

import com.example.bookreadingtracking.model.DataState

interface PersistenceHandler {
    fun saveData(data: DataState)
    fun loadData(): DataState
}

expect fun getPersistenceHandler(): PersistenceHandler
