package com.example.applibros.ui.book

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.applibros.data.UserPreferencesManager
import com.example.applibros.data.firebase.FirestoreService
import com.example.applibros.data.model.Comment
import com.example.applibros.data.model.CommentWithUser
import com.example.applibros.viewmodel.BookViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch



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

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    val isOwnBook = book?.authorId == currentUserId

    val commentText = remember { mutableStateOf("") }
    val replyingTo = remember { mutableStateOf<CommentWithUser?>(null) }
    val comments = remember { mutableStateListOf<CommentWithUser>() }
    val commentListener = remember { mutableStateOf<ListenerRegistration?>(null) }
    val showDeleteDialog = remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val userPrefs = remember(currentUserId) {
        UserPreferencesManager(context, currentUserId ?: "anonymous")
    }



    val fontSizeFlow = userPrefs.fontSizeFlow.collectAsState(initial = null)
    val fontFamilyFlow = userPrefs.fontFamilyFlow.collectAsState(initial = null)
    val savedFontSize = fontSizeFlow.value
    val savedFontFamily = fontFamilyFlow.value


    val availableFonts = mapOf(
        "Predeterminada" to FontFamily.Default,
        "Serif" to FontFamily.Serif,
        "Cursive" to FontFamily.Cursive,
        "Monospace" to FontFamily.Monospace,
    )
    val isFontSettingsLoaded = remember { mutableStateOf(false) }
    var fontSize by remember { mutableStateOf(16.sp) }
    var selectedFontFamily by remember { mutableStateOf<FontFamily>(FontFamily.Default) }
    var showFontDialog by remember { mutableStateOf(false) }
    var showFontSizeDialog by remember { mutableStateOf(false) }


    LaunchedEffect(savedFontSize, savedFontFamily) {
        if (!isFontSettingsLoaded.value && savedFontSize != null && savedFontFamily != null) {
            fontSize = savedFontSize.sp
            selectedFontFamily = fontFamilyFromName(savedFontFamily)
            isFontSettingsLoaded.value = true
        }
    }


    LaunchedEffect(fontSize.value, isFontSettingsLoaded.value) {
        if (isFontSettingsLoaded.value) {
            userPrefs.saveFontSize(fontSize.value)
        }
    }

    LaunchedEffect(selectedFontFamily, isFontSettingsLoaded.value) {
        if (isFontSettingsLoaded.value) {
            userPrefs.saveFontFamily(fontFamilyToName(selectedFontFamily))
        }
    }

    LaunchedEffect(bookId) {
        viewModel.loadChaptersForBook(bookId)
        viewModel.loadBookById(bookId)

        if (startChapter == 0 && currentUserId != null) {
            viewModel.loadReadingProgress(bookId, currentUserId) { savedIndex ->
                currentIndex = savedIndex
            }
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            if (currentUserId != null) {
                viewModel.saveReadingProgress(bookId, currentUserId, currentIndex)
            }
        }
    }

    fun deleteComment(commentId: String) {
        FirestoreService().deleteComment(
            commentId,
            onSuccess = {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Comentario eliminado")
                }
            },
            onFailure = { Log.e("Comments", "Error al eliminar comentario", it) }
        )
    }
    fun startCommentListener() {
        commentListener.value?.remove()
        currentChapter?.let {
            commentListener.value = FirestoreService().listenToCommentsWithUsers(
                bookId = bookId,
                chapterId = it.id,
                onUpdate = { result ->
                    comments.clear()
                    comments.addAll(result)
                },
                onError = {
                    Log.e("Comments", "Error al escuchar comentarios con usuarios", it)
                }
            )
        }
    }

    LaunchedEffect(currentChapter?.id) {
        startCommentListener()
    }

    DisposableEffect(currentChapter?.id) {
        onDispose { commentListener.value?.remove() }
    }

    if (showDeleteDialog.value?.second == true) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = null },
            title = { Text("Eliminar comentario") },
            text = { Text("¿Estás seguro de que deseas eliminar este comentario?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog.value?.first?.let {
                        deleteComment(it)
                    }
                    showDeleteDialog.value = null
                }) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog.value = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },

        topBar = {
            TopAppBar(
                title = {
                    val progress = if (chapters.isNotEmpty()) {
                        ((currentIndex + 1) / chapters.size.toFloat()) * 100
                    } else 0f
                    Text(" ${progress.toInt()}%")
                },
                actions = {
                    IconButton(onClick = { showSheet = true }) {
                        Icon(Icons.Default.MenuBook, contentDescription = "Capítulos")
                    }
                    IconButton(onClick = { showFontSizeDialog = true }) {
                        Icon(Icons.Default.TextFields, contentDescription = "Tamaño de letra")
                    }
                    IconButton(onClick = { showFontDialog = true }) {
                        Icon(Icons.Default.FontDownload, contentDescription = "Tipo de letra")
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
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 📝 Título del capítulo
                Text(
                    text = currentChapter.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = (fontSize.value + 4).sp,
                        fontFamily = selectedFontFamily
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                // 📖 Contenido del capítulo
                Text(
                    text = currentChapter.content,
                    fontSize = fontSize,
                    fontFamily = selectedFontFamily,
                    lineHeight = (fontSize.value * 1.6).sp, // ✅ mejora la legibilidad
                    modifier = Modifier.fillMaxWidth()
                )

                // 👉 Navegación al siguiente capítulo
                if (currentIndex < chapters.lastIndex) {
                    Button(
                        onClick = { currentIndex++ },
                        modifier = Modifier
                            .align(Alignment.End)
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("Capítulo siguiente")
                    }
                } else {
                    Text(
                        text = "Fin del libro",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                // ✏️ Botón de edición (solo autor)
                if (isOwnBook) {
                    OutlinedButton(
                        onClick = {
                            navController.navigate("edit_chapter/${currentChapter.id}")
                        },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editar capítulo")
                    }
                }

                // 💬 Comentarios
                Divider()
                Text(
                    text = "Comentarios",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                @Composable
                fun displayCommentTree(parentId: String? = null, indent: Int = 0) {
                    comments.filter { it.comment.parentId == parentId }.forEach { item ->
                        Column(modifier = Modifier.padding(start = indent.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = item.user?.photoUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.user?.username ?: "Usuario",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(text = item.comment.content)
                                }
                                if (item.comment.userId == currentUserId) {
                                    IconButton(onClick = {
                                        showDeleteDialog.value = item.comment.id to true
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                    }
                                }
                            }
                            TextButton(onClick = { replyingTo.value = item }) {
                                Text("Reply")
                            }
                            displayCommentTree(item.comment.id, indent + 16)
                        }
                    }
                }

                displayCommentTree()

                if (replyingTo.value != null) {
                    Text(
                        text = "Respondiendo a ${replyingTo.value?.user?.username}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                OutlinedTextField(
                    value = commentText.value,
                    onValueChange = { commentText.value = it },
                    label = { Text("Añadir un comentario") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val newComment = Comment(
                            bookId = bookId,
                            chapterId = currentChapter.id,
                            userId = currentUserId ?: "",
                            content = commentText.value,
                            parentId = replyingTo.value?.comment?.id
                        )
                        FirestoreService().addComment(newComment, onSuccess = {
                            commentText.value = ""
                            replyingTo.value = null
                            startCommentListener()
                        }, onFailure = {
                            Log.e("Comments", "Error al añadir comentario", it)
                        })
                    },
                    modifier = Modifier.align(Alignment.End),
                    enabled = commentText.value.isNotBlank()
                ) {
                    Text("Publicar")
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

    // Diálogo para elegir tamaño de letra
    if (showFontSizeDialog) {
        AlertDialog(
            onDismissRequest = { showFontSizeDialog = false },
            title = { Text("Tamaño de letra") },
            text = {
                Column {
                    listOf(14.sp, 16.sp, 18.sp, 20.sp, 24.sp).forEach { size ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    fontSize = size
                                    showFontSizeDialog = false
                                }
                                .padding(8.dp)
                        ) {
                            RadioButton(selected = fontSize == size, onClick = {
                                fontSize = size
                                showFontSizeDialog = false
                            })
                            Text("Tamaño ${size.value.toInt()}sp", fontSize = size)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showFontDialog) {
        AlertDialog(
            onDismissRequest = { showFontDialog = false },
            title = { Text("Elegir tipo de letra") },
            text = {
                Column {
                    availableFonts.forEach { (name, font) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedFontFamily = font
                                    showFontDialog = false
                                }
                                .padding(8.dp)
                        ) {
                            RadioButton(
                                selected = selectedFontFamily == font,
                                onClick = {
                                    selectedFontFamily = font
                                    showFontDialog = false
                                }
                            )
                            Text(name, fontFamily = font, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }



}



fun fontFamilyToName(font: FontFamily): String {
    return when (font) {
        FontFamily.Serif -> "Serif"
        FontFamily.SansSerif -> "SansSerif"
        FontFamily.Monospace -> "Monospace"
        else -> "Default"
    }
}
fun fontFamilyFromName(name: String): FontFamily {
    return when (name) {
        "Serif" -> FontFamily.Serif
        "SansSerif" -> FontFamily.SansSerif
        "Monospace" -> FontFamily.Monospace
        else -> FontFamily.Default
    }
}





