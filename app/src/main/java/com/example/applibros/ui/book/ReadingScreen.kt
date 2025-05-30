package com.example.applibros.ui.book

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.applibros.viewmodel.BookViewModel
import com.google.firebase.auth.FirebaseAuth
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingScreen(
    navController: NavController,
    bookId: String,
    startChapter: Int = 0,
    viewModel: BookViewModel = viewModel()
) {
    val chapters by viewModel.bookChapters.collectAsState()
    var currentIndex by remember(startChapter) { mutableIntStateOf(startChapter) }
    val currentChapter = chapters.getOrNull(currentIndex)

    val book by viewModel.selectedBook.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    // Cargar datos
    LaunchedEffect(bookId) {
        viewModel.loadChaptersForBook(bookId)
        viewModel.loadBookById(bookId)

        // Cargar progreso solo si no se especificó startChapter
        if (startChapter == 0 && currentUserId != null) {
            viewModel.loadReadingProgress(bookId, currentUserId) { savedIndex ->
                currentIndex = savedIndex
            }
        }
    }

    // Guardar progreso al salir
    DisposableEffect(Unit) {
        onDispose {
            if (currentUserId != null) {
                viewModel.saveReadingProgress(bookId, currentUserId, currentIndex)
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    val isOwnBook = book?.authorId == currentUserId

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val progress = if (chapters.isNotEmpty()) {
                        ((currentIndex + 1) / chapters.size.toFloat()) * 100
                    } else 0f

                    Text("Lectura - ${progress.toInt()}%")
                },
                actions = {
                    IconButton(onClick = { showSheet = true }) {
                        Icon(Icons.Default.MenuBook, contentDescription = "Capítulos")
                    }
                }
            )
        }
    ) { innerPadding ->

        if (currentChapter != null) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(currentChapter.title, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(currentChapter.content)
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (currentIndex < chapters.lastIndex) {
                    Button(
                        onClick = {
                            currentIndex++
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Capítulo siguiente")
                    }
                } else {
                    Text("Fin del libro", style = MaterialTheme.typography.bodyMedium)
                }

                if (isOwnBook) {
                    OutlinedButton(
                        onClick = {
                            navController.navigate("edit_chapter/${currentChapter.id}")
                        },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar capítulo")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editar capítulo")
                    }
                }
            }

        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Capítulos", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                chapters.forEachIndexed { index, chapter ->
                    Text(
                        text = chapter.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                currentIndex = index
                                showSheet = false
                            }
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

