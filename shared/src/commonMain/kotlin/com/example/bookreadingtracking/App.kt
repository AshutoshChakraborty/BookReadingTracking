package com.example.bookreadingtracking

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bookreadingtracking.model.Book
import com.example.bookreadingtracking.model.BookMetadata
import com.example.bookreadingtracking.model.ReadingStatus
import com.example.bookreadingtracking.viewmodel.BookViewModel
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.material.icons.filled.ArrowBack
import com.example.bookreadingtracking.ui.PlanListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val viewModel: BookViewModel = viewModel { BookViewModel() }
    val currentPlanId = viewModel.currentPlanId
    
    MaterialTheme {
        if (currentPlanId == null) {
            PlanListScreen(
                viewModel = viewModel,
                onPlanClick = { plan -> viewModel.selectPlan(plan.id) }
            )
        } else {
            val currentPlan = viewModel.plans.find { it.id == currentPlanId }
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(currentPlan?.title ?: "Plan Details") },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.selectPlan(null) }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back to Plans")
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.refreshAllProgress() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh All")
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { viewModel.addPdf() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add PDF")
                    }
                },
                bottomBar = {
                    viewModel.activeBookId?.let { activeId ->
                        val book = viewModel.allBooks.find { it.id == activeId }
                        if (book != null) {
                            Surface(
                                tonalElevation = 8.dp,
                                shadowElevation = 8.dp,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Reading: ${book.metadata.title}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "Timer started...",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Button(
                                        onClick = { viewModel.stopReading() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Red,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text("Stop Reading")
                                    }
                                }
                            }
                        }
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                    if (viewModel.filteredBooks.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No books added to this plan yet. Click + to add a PDF.")
                        }
                    } else {
                        KanbanBoard(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun KanbanBoard(viewModel: BookViewModel) {
    var draggedBookId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var currentTargetStatus by remember { mutableStateOf<ReadingStatus?>(null) }
    val columnBounds = remember { mutableStateMapOf<ReadingStatus, androidx.compose.ui.geometry.Rect>() }
    val books = viewModel.filteredBooks

    Row(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ReadingStatus.entries.forEach { status ->
            KanbanColumn(
                status = status,
                books = books.filter { it.status == status },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .onGloballyPositioned { layoutCoordinates ->
                        val position = layoutCoordinates.positionInWindow()
                        val size = layoutCoordinates.size
                        columnBounds[status] = androidx.compose.ui.geometry.Rect(
                            position.x,
                            position.y,
                            position.x + size.width,
                            position.y + size.height
                        )
                    }
                    .background(
                        if (currentTargetStatus == status) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                        RoundedCornerShape(12.dp)
                    ),
                onBookDragStart = { bookId, offset ->
                    draggedBookId = bookId
                    dragOffset = offset
                },
                onBookDrag = { offset ->
                    dragOffset += offset
                    currentTargetStatus = columnBounds.entries.find { it.value.contains(dragOffset) }?.key
                },
                onBookDragEnd = {
                    draggedBookId?.let { id ->
                        currentTargetStatus?.let { status ->
                            viewModel.updateBookStatus(id, status)
                        }
                    }
                    draggedBookId = null
                    dragOffset = Offset.Zero
                    currentTargetStatus = null
                },
                viewModel = viewModel
            )
        }
    }

    // Overlay for the dragged item
    draggedBookId?.let { id ->
        val book = books.find { it.id == id }
        if (book != null) {
            Box(
                modifier = Modifier
                    .offset(
                        x = (dragOffset.x).dp,
                        y = (dragOffset.y).dp
                    )
                    .width(220.dp)
                    .graphicsLayer {
                        alpha = 0.8f
                        scaleX = 1.05f
                        scaleY = 1.05f
                    }
                    .shadow(16.dp)
            ) {
                BookCard(book, {}, {}, {})
            }
        }
    }
}

@Composable
fun KanbanColumn(
    status: ReadingStatus,
    books: List<Book>,
    modifier: Modifier = Modifier,
    onBookDragStart: (String, Offset) -> Unit,
    onBookDrag: (Offset) -> Unit,
    onBookDragEnd: () -> Unit,
    viewModel: BookViewModel
) {
    KanbanColumnContent(
        status = status,
        books = books,
        modifier = modifier,
        onBookDragStart = onBookDragStart,
        onBookDrag = onBookDrag,
        onBookDragEnd = onBookDragEnd,
        onBookClick = { viewModel.startReading(it) },
        onBookRemove = { viewModel.removeBook(it) },
        onBookRefresh = { viewModel.refreshProgress(it) }
    )
}

@Composable
fun KanbanColumnContent(
    status: ReadingStatus,
    books: List<Book>,
    modifier: Modifier = Modifier,
    onBookDragStart: (String, Offset) -> Unit,
    onBookDrag: (Offset) -> Unit,
    onBookDragEnd: () -> Unit,
    onBookClick: (Book) -> Unit,
    onBookRemove: (Book) -> Unit,
    onBookRefresh: (Book) -> Unit
) {
    Column(modifier = modifier.padding(8.dp)) {
        // Updated Header with Chip and Count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = status.name.replace("_", " "),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape,
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = books.size.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        HorizontalDivider(
            modifier = Modifier.padding(bottom = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(books) { book ->
                var itemPosition by remember { mutableStateOf(Offset.Zero) }
                
                Box(
                    modifier = Modifier
                        .onGloballyPositioned { itemPosition = it.positionInWindow() }
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { offset ->
                                    onBookDragStart(book.id, itemPosition + offset)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    onBookDrag(dragAmount)
                                },
                                onDragEnd = { onBookDragEnd() },
                                onDragCancel = { onBookDragEnd() }
                            )
                        }
                ) {
                    BookCard(
                        book = book,
                        onClick = { onBookClick(book) },
                        onRemove = { onBookRemove(book) },
                        onRefresh = { onBookRefresh(book) }
                    )
                }
            }
        }
    }
}

@Composable
fun BookCard(book: Book, onClick: () -> Unit, onRemove: () -> Unit, onRefresh: () -> Unit) {
    val progress = if (book.metadata.pageCount > 0) {
        book.currentPage.toFloat() / book.metadata.pageCount.toFloat()
    } else 0f
    
    val percentage = (progress * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            if (book.metadata.thumbnail != null) {
                Image(
                    bitmap = book.metadata.thumbnail,
                    contentDescription = "Thumbnail",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Preview", style = MaterialTheme.typography.labelMedium)
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = book.metadata.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "By ${book.metadata.author}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$percentage% Complete",
                            style = getLabelTextStyle(10.sp),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Page ${book.currentPage} / ${book.metadata.pageCount}",
                            style = getLabelTextStyle(9.sp)
                        )
                        Text(
                            text = "Time: ${formatTime(book.totalTimeSpentMillis)}",
                            style = getLabelTextStyle(9.sp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Row {
                        IconButton(onClick = onRefresh, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getLabelTextStyle(fontSize: androidx.compose.ui.unit.TextUnit): androidx.compose.ui.text.TextStyle {
    return MaterialTheme.typography.labelSmall.copy(fontSize = fontSize)
}

fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "< 1m"
    }
}

@Preview
@Composable
fun BookCardPreview() {
    val sampleBook = Book(
        id = "1",
        planId = "default",
        filePath = "/path/to/book.pdf",
        metadata = BookMetadata(
            title = "Kotlin Multiplatform in Action",
            author = "John Doe",
            pageCount = 350,
            thumbnail = null
        ),
        currentPage = 45,
        totalTimeSpentMillis = 3600000,
        status = ReadingStatus.READING
    )
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            BookCard(
                book = sampleBook,
                onClick = {},
                onRemove = {},
                onRefresh = {}
            )
        }
    }
}

@Preview
@Composable
fun KanbanColumnPreview() {
    val sampleBooks = listOf(
        Book(
            id = "1",
            planId = "default",
            filePath = "/path/to/book1.pdf",
            metadata = BookMetadata(
                title = "Kotlin Multiplatform in Action",
                author = "John Doe",
                pageCount = 350,
                thumbnail = null
            ),
            currentPage = 45,
            totalTimeSpentMillis = 3600000,
            status = ReadingStatus.READING
        ),
        Book(
            id = "2",
            planId = "default",
            filePath = "/path/to/book2.pdf",
            metadata = BookMetadata(
                title = "Compose Multiplatform for Beginners",
                author = "Jane Smith",
                pageCount = 200,
                thumbnail = null
            ),
            currentPage = 0,
            totalTimeSpentMillis = 0,
            status = ReadingStatus.READING
        )
    )
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxHeight().width(300.dp).padding(16.dp)) {
            KanbanColumnContent(
                status = ReadingStatus.READING,
                books = sampleBooks,
                onBookDragStart = { _, _ -> },
                onBookDrag = { _ -> },
                onBookDragEnd = {},
                onBookClick = {},
                onBookRemove = {},
                onBookRefresh = {}
            )
        }
    }
}
