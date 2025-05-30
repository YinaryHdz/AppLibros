package com.example.applibros.ui.book

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.applibros.viewmodel.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBookScreen(
    onBookCreated: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: BookViewModel = viewModel()

    val genres = listOf("Ficción", "No Ficción", "Misterio", "Romance", "Fantasía", "Ciencia Ficción")

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var coverImageUri by remember { mutableStateOf<Uri?>(null) }
    val tags = tagsInput
        .split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }
    val scrollState = rememberScrollState()



    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        coverImageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Nuevo libro", style = MaterialTheme.typography.headlineSmall)

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

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = genre,
                onValueChange = { genre = it },
                label = { Text("Género") },
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                genres.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            genre = selectionOption
                            expanded = false
                        }
                    )
                }
            }
        }
        OutlinedTextField(
            value = tagsInput,
            onValueChange = { tagsInput = it },
            label = { Text("Etiquetas (separadas por coma)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Seleccionar portada")
        }

        coverImageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Portada del libro",
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Button(
            onClick = {
                val tags = tagsInput
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && it.any { char -> char.isLetterOrDigit() } }

                val validation = BookValidator.validate(title, description, genre, tags)

                if (!validation.isValid) {
                    Toast.makeText(context, validation.errorMessage, Toast.LENGTH_SHORT).show()
                    return@Button
                }

                viewModel.uploadCoverAndCreateBook(
                    context,
                    title,
                    description,
                    genre,
                    coverImageUri,
                    tags,
                    onSuccess = {
                        Toast.makeText(context, "Libro creado", Toast.LENGTH_SHORT).show()
                        onBookCreated()
                    },
                    onFailure = {
                        Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar libro")
        }





    }
}

