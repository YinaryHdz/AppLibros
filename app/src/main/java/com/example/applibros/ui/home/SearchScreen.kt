package com.example.applibros.ui.home

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.applibros.data.firebase.FirestoreService
import com.example.applibros.data.firebase.SimpleUser
import com.example.applibros.data.model.Book
import com.example.applibros.ui.book.BookListItem


@Composable
fun SearchScreen(navController: NavController) {
    var query by remember { mutableStateOf("") }
    var searchMode by remember { mutableStateOf(SearchMode.BOOK) }

    val firestore = remember { FirestoreService() }
    var bookResults by remember { mutableStateOf<List<Book>>(emptyList()) }
    var userResults by remember { mutableStateOf<List<SimpleUser>>(emptyList()) }

    val context = LocalContext.current

    val scrollState = rememberScrollState()

    // Ejecutar búsqueda cuando cambia el query o modo
    LaunchedEffect(query, searchMode) {
        if (query.isBlank()) {
            bookResults = emptyList()
            userResults = emptyList()
        } else {
            if (searchMode == SearchMode.BOOK) {
                firestore.searchBooks(
                    query = query,
                    byAuthor = false,
                    onSuccess = { bookResults = it },
                    onFailure = {
                        Toast.makeText(context, "Error buscando libros", Toast.LENGTH_SHORT).show()
                        bookResults = emptyList()
                    }
                )
            } else {
                firestore.searchUsersByUsername(
                    query = query,
                    onSuccess = { userResults = it },
                    onFailure = {
                        Toast.makeText(context, "Error buscando usuarios", Toast.LENGTH_SHORT).show()
                        userResults = emptyList()
                    }
                )

            }
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        ) {

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Buscar...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { searchMode = SearchMode.BOOK },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (searchMode == SearchMode.BOOK) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text("Buscar libros")
            }

            Button(
                onClick = { searchMode = SearchMode.AUTHOR },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (searchMode == SearchMode.AUTHOR) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text("Buscar autores")
            }
        }

        Spacer(Modifier.height(16.dp))

        if (query.isNotBlank()) {
            if (searchMode == SearchMode.BOOK) {
                if (bookResults.isEmpty()) {
                    Text("No se encontraron libros.")
                } else {
                    LazyColumn {
                        items(bookResults) { book ->
                            BookListItem(book = book) {
                                navController.navigate("book_detail/${book.id}")
                            }
                        }
                    }
                }
            } else {
                if (userResults.isEmpty()) {
                    Text("No se encontraron autores.")
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(userResults) { user ->
                            UserGridItem(
                                username = user.username,
                                photoUrl = user.photoUrl
                            ) {
                                navController.navigate("author_profile/${user.uid}")
                            }
                        }
                    }

                }
            }
        }
    }
}
@Composable
fun UserGridItem(username: String, photoUrl: String?, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(12.dp)
            .clickable { onClick() }
    ) {
        if (!photoUrl.isNullOrEmpty()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Sin foto",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = username,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1
        )

        Text(
            text = "Autor",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}





