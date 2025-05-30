package com.example.applibros.ui.book

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.applibros.viewmodel.BookViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    navController: NavController,
    bookId: String,
    currentUserId: String,
) {
    val viewModel: BookViewModel = viewModel()
    val book by viewModel.selectedBook.collectAsState()
    val chapters by viewModel.bookChapters.collectAsState()
    val scrollState = rememberScrollState()

    var isFavorite by remember { mutableStateOf(false) }
    var showListDialog by remember { mutableStateOf(false) }



    LaunchedEffect(bookId) {
        viewModel.loadBookById(bookId)
        viewModel.loadChaptersForBook(bookId)

        if (currentUserId.isNotBlank()) {
            viewModel.isBookFavorite(bookId, currentUserId) { isFav ->
                isFavorite = isFav
            }
        }
    }

    book?.let { bookData ->
        val isOwnBook = bookData.authorId == currentUserId

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
        ) {
            AsyncImage(
                model = bookData.coverImageUrl,
                contentDescription = "Portada",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(bookData.title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(bookData.description)

            Spacer(modifier = Modifier.height(16.dp))

            if (isOwnBook) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = {
                        navController.navigate("edit_book/${bookData.id}")
                    }) {
                        Text("Editar libro")
                    }
                    Button(onClick = {
                        navController.navigate("create_chapter/${bookData.id}")
                    }) {
                        Text("Nuevo capítulo")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            if (chapters.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { navController.navigate("read_book/$bookId") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Leer libro")
                    }

                    if (!isOwnBook) {
                        IconButton(
                            onClick = {
                                viewModel.toggleFavorite(bookData, currentUserId, isFavorite) { updated ->
                                    isFavorite = updated
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isFavorite) "Quitar de favoritos" else "Agregar a favoritos"
                            )
                        }

                        IconButton(onClick = { showListDialog = true }) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Agregar a lista")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Capítulos", style = MaterialTheme.typography.titleMedium)

            if (chapters.isEmpty()) {
                Text("No hay capítulos aún.")
            } else {
                var showChapterList by remember { mutableStateOf(false) }
                Button(onClick = { showChapterList = true }) {
                    Text("Ver capítulos")
                }

                if (showChapterList) {
                    ModalBottomSheet(onDismissRequest = { showChapterList = false }) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Capítulos disponibles", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            chapters.forEachIndexed { index, chapter ->
                                Text(
                                    text = chapter.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showChapterList = false
                                            navController.navigate("read_book/${book!!.id}?startChapter=$index")
                                        }
                                        .padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Dialogo para agregar a lista
            if (showListDialog && !isOwnBook) {
                var lists by remember { mutableStateOf<List<String>>(emptyList()) }
                var newListName by remember { mutableStateOf("") }
                val bookInList = remember { mutableStateMapOf<String, Boolean>() }


                LaunchedEffect(showListDialog) {
                    if (showListDialog) {
                        viewModel.loadReadingLists(currentUserId) { result ->
                            lists = result
                            result.forEach { name ->
                                // Solo consulta si aún no se tiene valor
                                if (bookInList[name] == null) {
                                    viewModel.isBookInList(currentUserId, name, bookData.id) { isIn ->
                                        bookInList[name] = isIn
                                    }
                                }
                            }
                        }
                    }
                }

                AlertDialog(

                    onDismissRequest = { showListDialog = false },
                    confirmButton = {
                        Button(onClick = {
                            val actions = mutableListOf<() -> Unit>()

                            // ✅ Actualizar listas ya existentes con checkbox
                            bookInList.forEach { (name, isChecked) ->
                                if (isChecked) {
                                    actions += {
                                        viewModel.addBookToList(currentUserId, name, bookData) {}
                                    }
                                } else {
                                    actions += {
                                        viewModel.removeBookFromList(currentUserId, name, bookData.id) {}
                                    }
                                }
                            }

                            // ✅ Crear nueva lista si se ha escrito
                            if (newListName.isNotBlank()) {
                                actions += {
                                    viewModel.addBookToList(currentUserId, newListName, bookData) {}
                                }
                            }

                            // Ejecutar todas las acciones
                            actions.forEach { it() }
                            showListDialog = false
                        }) {
                            Text("Confirmar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showListDialog = false }) {
                            Text("Cancelar")
                        }
                    },
                    title = { Text("Agregar a lista") },
                    text = {
                        Column {
                            if (lists.isNotEmpty()) {
                                Text("Seleccionar listas:")
                                Spacer(Modifier.height(8.dp))
                                lists.forEach { name ->
                                    val isChecked = bookInList[name] == true

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                bookInList[name] = !(bookInList[name] ?: false)
                                            }
                                            .padding(4.dp)
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = {
                                                bookInList[name] = it
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(name)
                                    }
                                }
                            }


                            Spacer(modifier = Modifier.height(16.dp))
                            Text("O crear una nueva:")
                            OutlinedTextField(
                                value = newListName,
                                onValueChange = { newListName = it },
                                label = { Text("Nombre de nueva lista") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                    }
                )
            }
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

