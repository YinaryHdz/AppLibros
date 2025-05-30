package com.example.applibros.ui.home

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
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
                    LazyColumn {
                        items(userResults) { user ->
                            UserListItem(username = user.username) {
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
fun UserListItem(username: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Icon(Icons.Default.Person, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(username, style = MaterialTheme.typography.titleMedium)
        }
    }
}


