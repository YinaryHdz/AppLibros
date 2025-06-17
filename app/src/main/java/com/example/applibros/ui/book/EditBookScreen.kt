package com.example.applibros.ui.book

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.applibros.viewmodel.BookViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun EditBookScreen(
    navController: NavController,
    bookId: String,

) {
    val viewModel: BookViewModel = viewModel()
    val context = LocalContext.current
    val book by viewModel.selectedBook.collectAsState()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val scrollState = rememberScrollState()

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> selectedImageUri = uri }

    // Cargar datos del libro solo una vez
    LaunchedEffect(bookId) {
        viewModel.loadBookById(bookId)
    }

    LaunchedEffect(book) {
        book?.let {
            title = it.title
            description = it.description
        }
    }

    if (book != null) {
        val nonNullBook = book // ahora Compose ya no se queja

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Editar libro", style = MaterialTheme.typography.headlineSmall)

            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = null,
                    modifier = Modifier
                        .height(180.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                )
            } else if (nonNullBook != null) {
                if (nonNullBook.coverImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = nonNullBook.coverImageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .height(180.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }

            Button(onClick = { imageLauncher.launch("image/*") }) {
                Text("Cambiar portada")
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = {
                if (title.isBlank()) {
                    Toast.makeText(context, "Título no puede estar vacío", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (selectedImageUri != null) {
                    viewModel.uploadBookCoverAndUpdate(
                        context = context, //
                        bookId = bookId,
                        uri = selectedImageUri!!,
                        title = title,
                        description = description,
                        onComplete = {
                            Toast.makeText(context, "Libro actualizado", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    )
                } else {
                    if (nonNullBook != null) {
                        viewModel.updateBookData(
                            bookId = bookId,
                            coverUrl = nonNullBook.coverImageUrl,
                            title = title,
                            description = description,
                            onComplete = {
                                Toast.makeText(context, "Libro actualizado", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }) {
                Text("Guardar cambios")
            }
        }
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
