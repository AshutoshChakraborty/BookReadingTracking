package com.example.bookreadingtracking

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform