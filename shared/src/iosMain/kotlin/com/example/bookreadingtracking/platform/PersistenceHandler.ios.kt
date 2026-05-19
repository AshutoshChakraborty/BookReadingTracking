package com.example.bookreadingtracking.platform

import com.example.bookreadingtracking.model.Book

class IosPersistenceHandler : PersistenceHandler {
    override fun saveBooks(books: List<Book>) {
        // Not implemented for iOS
    }

    override fun loadBooks(): List<Book> {
        return emptyList()
    }
}

actual fun getPersistenceHandler(): PersistenceHandler = IosPersistenceHandler()
