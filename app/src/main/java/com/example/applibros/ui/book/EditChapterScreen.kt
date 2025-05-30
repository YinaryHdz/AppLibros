package com.example.applibros.ui.book

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.applibros.viewmodel.BookViewModel
import androidx.compose.runtime.collectAsState


@Composable
fun EditChapterScreen(
    navController: NavController,
    chapterId: String,
    viewModel: BookViewModel = viewModel()
) {
    val context = LocalContext.current
    val chapter by viewModel.selectedChapter.collectAsState()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Cargar el capítulo una vez
    LaunchedEffect(chapterId) {
        viewModel.loadChapterById(chapterId)
    }

    // Prellenar campos cuando llega el capítulo
    LaunchedEffect(chapter) {
        chapter?.let {
            title = it.title
            content = it.content
        }
    }
    Log.d("EditChapterScreen", "Capítulo recibido: $chapter")

    if (chapter != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Editar Capítulo", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Contenido") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                maxLines = Int.MAX_VALUE
            )

            Button(
                onClick = {
                    if (title.isBlank() || content.isBlank()) {
                        Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSaving = true
                    viewModel.updateChapter(
                        chapterId = chapterId,
                        title = title,
                        content = content,
                        onComplete = {
                            isSaving = false
                            Toast.makeText(context, "Capítulo actualizado", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    )
                },
                enabled = !isSaving
            ) {
                Text(if (isSaving) "Guardando..." else "Guardar cambios")
            }
        }
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
