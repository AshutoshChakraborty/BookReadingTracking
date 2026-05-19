package com.example.bookreadingtracking.platform

import com.example.bookreadingtracking.model.DataState

class IosPersistenceHandler : PersistenceHandler {
    override fun saveData(data: DataState) {
        // Not implemented for iOS
    }

    override fun loadData(): DataState {
        return DataState()
    }
}

actual fun getPersistenceHandler(): PersistenceHandler = IosPersistenceHandler()
