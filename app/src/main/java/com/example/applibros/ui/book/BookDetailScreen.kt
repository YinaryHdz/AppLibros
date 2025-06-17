package com.example.applibros.ui.book

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.applibros.ui.theme.DancingScript
import com.example.applibros.viewmodel.BookViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
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
    var showDeleteOptions by remember { mutableStateOf(false) }

    val author by viewModel.author.collectAsState()

    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(book?.authorId) {
        book?.authorId?.let { viewModel.loadAuthorById(it) }
    }

    LaunchedEffect(bookId) {
        viewModel.loadBookById(bookId)
        viewModel.loadChaptersForBook(bookId)
        if (currentUserId.isNotBlank()) {
            viewModel.isBookFavorite(bookId, currentUserId) { isFav -> isFavorite = isFav }
        }
    }

    book?.let { bookData ->
        val isOwnBook = bookData.authorId == currentUserId

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .padding(top = 16.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)), // ✅ fondo detrás
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = bookData.coverImageUrl,
                    contentDescription = "Portada del libro",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .shadow(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 📖 Título
            Text(
                text = bookData.title,
                style = MaterialTheme.typography.headlineLarge.copy( // Aumentamos el tamaño
                    fontFamily = DancingScript,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 👤 Autor con imagen + nombre centrado
            author?.let { user ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("profile/${user.uid}")
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = "Foto de perfil de ${user.username}",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                    Text(
                        text = user.username,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // 👁️ Vistas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "Vistas",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${bookData.views} vistas",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            // 📚 Descripción
            Text(
                text = bookData.description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )


            Spacer(modifier = Modifier.height(16.dp))

// 📝 Si el libro es propio: mostrar botones de edición
            if (isOwnBook) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
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
                    if (isOwnBook) {
                        IconButton(
                            onClick = { showDeleteOptions = true },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.Red, shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar libro",
                                tint = Color.White
                            )
                        }
                    }

                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (chapters.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    // 📖 Leer libro
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val bookRef = Firebase.firestore.collection("books").document(bookId)
                                    bookRef.update("views", FieldValue.increment(1))
                                } catch (e: Exception) {
                                    Log.e("BookView", "Error al incrementar vistas", e)
                                }
                                navController.navigate("read_book/$bookId")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Leer libro")
                    }


                    // ❤️ Favorito y lista (solo si no es del usuario)
                    if (!isOwnBook) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
            }


            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Capítulos",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (chapters.isEmpty()) {
                Text(
                    text = "No hay capítulos aún.",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                var showChapterList by remember { mutableStateOf(false) }

                // 🟦 Botón "Ver capítulos" estilo pill
                Button(
                    onClick = { showChapterList = true },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {
                    Text("Ver capítulos")
                }

                // ⬇️ BottomSheet con la lista de capítulos
                if (showChapterList) {
                    ModalBottomSheet(
                        onDismissRequest = { showChapterList = false },
                        modifier = Modifier.fillMaxHeight(0.6f)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = "Capítulos disponibles",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            chapters.forEachIndexed { index, chapter ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showChapterList = false
                                            navController.navigate("read_book/${book!!.id}?startChapter=$index")
                                        }
                                        .padding(vertical = 4.dp),
                                    tonalElevation = 2.dp,
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Text(
                                        text = chapter.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }


            if (showListDialog && !isOwnBook) {
                var lists by remember { mutableStateOf<List<String>>(emptyList()) }
                var newListName by remember { mutableStateOf("") }
                val bookInList = remember { mutableStateMapOf<String, Boolean>() }

                LaunchedEffect(showListDialog) {
                    if (showListDialog) {
                        viewModel.loadReadingLists(currentUserId) { result ->
                            lists = result
                            result.forEach { name ->
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

                            bookInList.forEach { (name, isChecked) ->
                                if (isChecked) {
                                    actions += { viewModel.addBookToList(currentUserId, name, bookData) {} }
                                } else {
                                    actions += { viewModel.removeBookFromList(currentUserId, name, bookData.id) {} }
                                }
                            }

                            if (newListName.isNotBlank()) {
                                actions += { viewModel.addBookToList(currentUserId, newListName.trim(), bookData) {} }
                            }

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
                    title = { Text("Agregar a listas de lectura") },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (lists.isNotEmpty()) {
                                Text("Selecciona listas existentes:", style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.height(8.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    lists.forEach { name ->
                                        val isChecked = bookInList[name] == true

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 4.dp)
                                        ) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = { bookInList[name] = it }
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = name)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Crear nueva lista:", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newListName,
                                onValueChange = { newListName = it },
                                label = { Text("Nombre de nueva lista") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

        }
        if (showDeleteOptions) {
            AlertDialog(
                onDismissRequest = { showDeleteOptions = false },
                title = { Text("¿Qué deseas hacer?") },
                text = {
                    Text("Puedes archivar el libro para ocultarlo o eliminarlo permanentemente.")
                },
                confirmButton = {
                    Column {
                        Button(
                            onClick = {
                                viewModel.archiveBook(bookId = bookId)
                                showDeleteOptions = false
                                navController.popBackStack()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Archivar")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                viewModel.deleteBook(bookId = bookId)
                                showDeleteOptions = false
                                navController.popBackStack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Eliminar permanentemente", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Mover el botón "Cancelar" aquí
                        TextButton(
                            onClick = { showDeleteOptions = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancelar")
                        }
                    }
                },
                // Elimina el dismissButton para evitar superposición
                dismissButton = {}
            )
        }



    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

