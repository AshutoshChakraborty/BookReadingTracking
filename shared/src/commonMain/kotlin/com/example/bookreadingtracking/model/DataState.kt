package com.example.bookreadingtracking.model

import kotlinx.serialization.Serializable

@Serializable
data class DataState(
    val plans: List<Plan> = emptyList(),
    val books: List<Book> = emptyList()
)
