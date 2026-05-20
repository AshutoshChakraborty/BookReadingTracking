package com.example.bookreadingtracking.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookreadingtracking.model.Book
import com.example.bookreadingtracking.model.DataState
import com.example.bookreadingtracking.model.Plan
import com.example.bookreadingtracking.platform.getBookHandler
import com.example.bookreadingtracking.platform.getPersistenceHandler
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class BookViewModel : ViewModel() {
    private val bookHandler = getBookHandler()
    private val persistenceHandler = getPersistenceHandler()
    
    val plans = mutableStateListOf<Plan>()
    val allBooks = mutableStateListOf<Book>()
    
    var currentPlanId by mutableStateOf<String?>(null)
    
    val filteredBooks: List<Book>
        get() = allBooks.filter { it.planId == currentPlanId }
    
    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val data = persistenceHandler.loadData()
            plans.clear()
            plans.addAll(data.plans)
            allBooks.clear()
            allBooks.addAll(data.books)
            
            // Re-extract thumbnails
            allBooks.forEachIndexed { index, book ->
                val metadata = bookHandler.extractMetadata(book.filePath)
                if (metadata != null) {
                    allBooks[index] = book.copy(metadata = metadata)
                }
            }
        }
    }

    private fun saveData() {
        persistenceHandler.saveData(DataState(plans.toList(), allBooks.toList()))
    }

    // Track which book is currently being read to calculate time
    var activeBookId by mutableStateOf<String?>(null)
        private set
    private var startTimeMillis: Long = 0

    fun addPlan(title: String) {
        val newPlan = Plan(
            id = Clock.System.now().toEpochMilliseconds().toString(),
            title = title
        )
        plans.add(newPlan)
        saveData()
    }

    fun deletePlan(plan: Plan) {
        plans.remove(plan)
        allBooks.removeAll { it.planId == plan.id }
        if (currentPlanId == plan.id) {
            currentPlanId = null
        }
        saveData()
    }

    fun selectPlan(planId: String?) {
        currentPlanId = planId
    }

    fun addBook() {
        val planId = currentPlanId ?: return
        viewModelScope.launch {
            val path = bookHandler.pickBookFile()
            if (path != null) {
                val metadata = bookHandler.extractMetadata(path)
                if (metadata != null) {
                    val progress = bookHandler.getReadingProgress(path) ?: 0
                    allBooks.add(Book(
                        id = Clock.System.now().toEpochMilliseconds().toString(),
                        planId = planId,
                        filePath = path,
                        metadata = metadata,
                        currentPage = progress
                    ))
                    saveData()
                }
            }
        }
    }

    fun startReading(book: Book) {
        stopReading() // Stop any previous session
        activeBookId = book.id
        startTimeMillis = Clock.System.now().toEpochMilliseconds()
        bookHandler.openBookFile(book.filePath)
    }

    fun stopReading() {
        val id = activeBookId ?: return
        val now = Clock.System.now().toEpochMilliseconds()
        val duration = now - startTimeMillis
        
        val index = allBooks.indexOfFirst { it.id == id }
        if (index != -1) {
            val book = allBooks[index]
            val updatedBook = book.copy(
                totalTimeSpentMillis = book.totalTimeSpentMillis + duration,
                lastReadTime = now
            )
            allBooks[index] = updatedBook
            refreshProgress(updatedBook) // Try to sync progress after reading
            saveData()
        }
        activeBookId = null
    }

    fun refreshProgress(book: Book) {
        val progress = bookHandler.getReadingProgress(book.filePath) ?: return
        val index = allBooks.indexOfFirst { it.id == book.id }
        if (index != -1) {
            allBooks[index] = allBooks[index].copy(currentPage = progress)
            saveData()
        }
    }

    fun refreshAllProgress() {
        allBooks.forEachIndexed { index, book ->
            val progress = bookHandler.getReadingProgress(book.filePath)
            if (progress != null) {
                allBooks[index] = book.copy(currentPage = progress)
            }
        }
        saveData()
    }

    fun removeBook(book: Book) {
        allBooks.remove(book)
        saveData()
    }

    fun updateBookStatus(bookId: String, newStatus: com.example.bookreadingtracking.model.ReadingStatus) {
        val index = allBooks.indexOfFirst { it.id == bookId }
        if (index != -1) {
            allBooks[index] = allBooks[index].copy(status = newStatus)
            saveData()
        }
    }

    fun updateBookNotes(bookId: String, newNotes: String) {
        val index = allBooks.indexOfFirst { it.id == bookId }
        if (index != -1) {
            allBooks[index] = allBooks[index].copy(notes = newNotes)
            saveData()
        }
    }
}
