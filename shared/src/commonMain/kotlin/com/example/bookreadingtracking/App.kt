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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bookreadingtracking.model.Book
import com.example.bookreadingtracking.model.BookMetadata
import com.example.bookreadingtracking.model.ReadingStatus
import com.example.bookreadingtracking.viewmodel.BookViewModel
import androidx.compose.ui.tooling.preview.Preview
import com.example.bookreadingtracking.ui.PlanListScreen
import com.example.bookreadingtracking.ui.theme.*
import bookreadingtracking.shared.generated.resources.Res
import bookreadingtracking.shared.generated.resources.app_icon
import org.jetbrains.compose.resources.painterResource

@Composable
fun getAppIcon() = painterResource(Res.drawable.app_icon)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val viewModel: BookViewModel = viewModel { BookViewModel() }
    val currentPlanId = viewModel.currentPlanId
    
    var editingBookId by remember { mutableStateOf<String?>(null) }
    var viewingCoverBookId by remember { mutableStateOf<String?>(null) }
    
    AppTheme {
        if (currentPlanId == null) {
            PlanListScreen(
                viewModel = viewModel,
                onPlanClick = { plan -> viewModel.selectPlan(plan.id) }
            )
        } else {
            val currentPlan = viewModel.plans.find { it.id == currentPlanId }
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { 
                            Text(
                                currentPlan?.title ?: "Plan Details",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            ) 
                        },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.selectPlan(null) }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back to Plans")
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.refreshAllProgress() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh All")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                floatingActionButton = {
                    LargeFloatingActionButton(
                        onClick = { viewModel.addBook() },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Book")
                    }
                },
                bottomBar = {
                    viewModel.activeBookId?.let { activeId ->
                        val book = viewModel.allBooks.find { it.id == activeId }
                        if (book != null) {
                            Surface(
                                tonalElevation = 8.dp,
                                shadowElevation = 12.dp,
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(48.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.MenuBook,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                book.metadata.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                "Tracking progress...",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                    Button(
                                        onClick = { viewModel.stopReading() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Stop Reading")
                                    }
                                }
                            }
                        }
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    if (viewModel.filteredBooks.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.LibraryBooks,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No books in this plan",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Add a book to start tracking",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        KanbanBoard(
                            viewModel = viewModel,
                            onEditNotes = { editingBookId = it },
                            onShowCover = { viewingCoverBookId = it }
                        )
                    }
                }
            }
        }
    }

    editingBookId?.let { bookId ->
        val book = viewModel.allBooks.find { it.id == bookId }
        if (book != null) {
            NotesDialog(
                initialNotes = book.notes,
                bookTitle = book.metadata.title,
                onDismiss = { editingBookId = null },
                onSave = { notes ->
                    viewModel.updateBookNotes(bookId, notes)
                    editingBookId = null
                }
            )
        }
    }

    viewingCoverBookId?.let { bookId ->
        val book = viewModel.allBooks.find { it.id == bookId }
        if (book != null && book.metadata.thumbnail != null) {
            FullScreenCover(
                thumbnail = book.metadata.thumbnail,
                onDismiss = { viewingCoverBookId = null }
            )
        }
    }
}

@Composable
fun NotesDialog(
    initialNotes: String,
    bookTitle: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var notes by remember { mutableStateOf(initialNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Column {
                Text("Book Notes", style = MaterialTheme.typography.headlineSmall)
                Text(
                    bookTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth().height(250.dp),
                placeholder = { Text("What's on your mind about this book?") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(notes) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Notes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun KanbanBoard(
    viewModel: BookViewModel,
    onEditNotes: (String) -> Unit,
    onShowCover: (String) -> Unit
) {
    var draggedBookId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var currentTargetStatus by remember { mutableStateOf<ReadingStatus?>(null) }
    val columnBounds = remember { mutableStateMapOf<ReadingStatus, androidx.compose.ui.geometry.Rect>() }
    val books = viewModel.filteredBooks

    Row(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ReadingStatus.entries.forEach { status ->
            val (bg, titleColor) = when (status) {
                ReadingStatus.TO_READ -> ToReadBg to ToReadTitle
                ReadingStatus.READING -> ReadingBg to ReadingTitle
                ReadingStatus.PAUSED -> PausedBg to PausedTitle
                ReadingStatus.READ -> ReadBg to ReadTitle
            }

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
                        if (currentTargetStatus == status) bg.copy(alpha = 0.8f) else bg,
                        RoundedCornerShape(16.dp)
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
                onEditNotes = onEditNotes,
                onShowCover = onShowCover,
                viewModel = viewModel,
                headerColor = titleColor
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
                    .width(240.dp)
                    .graphicsLayer {
                        alpha = 0.9f
                        scaleX = 1.05f
                        scaleY = 1.05f
                    }
                    .shadow(24.dp, RoundedCornerShape(16.dp))
            ) {
                BookCard(book, {}, {}, {}, {}, {})
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
    onEditNotes: (String) -> Unit,
    onShowCover: (String) -> Unit,
    viewModel: BookViewModel,
    headerColor: androidx.compose.ui.graphics.Color
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
        onBookRefresh = { viewModel.refreshProgress(it) },
        onEditNotes = onEditNotes,
        onShowCover = onShowCover,
        headerColor = headerColor
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
    onBookRefresh: (Book) -> Unit,
    onEditNotes: (String) -> Unit,
    onShowCover: (String) -> Unit,
    headerColor: androidx.compose.ui.graphics.Color
) {
    Column(modifier = modifier.padding(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when(status) {
                    ReadingStatus.TO_READ -> Icons.Default.BookmarkBorder
                    ReadingStatus.READING -> Icons.Default.PlayArrow
                    ReadingStatus.PAUSED -> Icons.Default.Pause
                    ReadingStatus.READ -> Icons.Default.CheckCircle
                }
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = headerColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = status.name.replace("_", " "),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = headerColor
                )
            }
            
            Surface(
                color = headerColor.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(26.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = books.size.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = headerColor
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(books, key = { it.id }) { book ->
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
                        onRefresh = { onBookRefresh(book) },
                        onEditNotes = { onEditNotes(book.id) },
                        onShowCover = { onShowCover(book.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun BookCard(
    book: Book,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onRefresh: () -> Unit,
    onEditNotes: () -> Unit,
    onShowCover: () -> Unit
) {
    val progress = if (book.metadata.pageCount > 0) {
        book.currentPage.toFloat() / book.metadata.pageCount.toFloat()
    } else 0f
    
    val percentage = (progress * 100).toInt()

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            if (book.metadata.thumbnail != null) {
                Image(
                    bitmap = book.metadata.thumbnail,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = book.metadata.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = book.metadata.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$percentage%",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Page ${book.currentPage}/${book.metadata.pageCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(book.totalTimeSpentMillis),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Row {
                        if (book.metadata.thumbnail != null) {
                            IconButton(onClick = onShowCover, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "Show Cover",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        IconButton(onClick = onEditNotes, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Default.EditNote,
                                contentDescription = "Notes",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        /*IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }*/
                        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Remove",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
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

@Composable
fun FullScreenCover(
    thumbnail: androidx.compose.ui.graphics.ImageBitmap,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.9f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = thumbnail,
                contentDescription = "Full Book Cover",
                modifier = Modifier
                    .fillMaxSize(0.85f)
                    .shadow(16.dp)
                    .clickable(enabled = false) { },
                contentScale = ContentScale.Fit
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
                    .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = androidx.compose.ui.graphics.Color.White
                )
            }
        }
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
    AppTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            BookCard(
                book = sampleBook,
                onClick = {},
                onRemove = {},
                onRefresh = {},
                onEditNotes = {},
                onShowCover = {}
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
    AppTheme {
        Surface(modifier = Modifier.fillMaxHeight().width(300.dp).padding(16.dp)) {
            KanbanColumnContent(
                status = ReadingStatus.READING,
                books = sampleBooks,
                onBookDragStart = { _, _ -> },
                onBookDrag = { _ -> },
                onBookDragEnd = {},
                onBookClick = {},
                onBookRemove = {},
                onBookRefresh = {},
                onEditNotes = {},
                onShowCover = {},
                headerColor = ReadingTitle
            )
        }
    }
}
