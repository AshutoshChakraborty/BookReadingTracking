package com.example.bookreadingtracking.platform

import com.example.bookreadingtracking.model.Book

interface PersistenceHandler {
    fun saveBooks(books: List<Book>)
    fun loadBooks(): List<Book>
}

expect fun getPersistenceHandler(): PersistenceHandler
