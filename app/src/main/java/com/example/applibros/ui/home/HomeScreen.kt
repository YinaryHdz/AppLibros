package com.example.applibros.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.applibros.data.model.Book
import com.example.applibros.navigation.Screen
import com.example.applibros.viewmodel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = viewModel()
    val user by viewModel.user.collectAsState()
    val books by viewModel.allBooks.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "AppLibros") },
                actions = {
                    // 🔍 Botón de búsqueda
                    IconButton(onClick = {
                        navController.navigate(Screen.Search.route)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar libros"
                        )
                    }

                    // ➕ Botón de agregar libro
                    IconButton(onClick = {
                        navController.navigate(Screen.CreateBook.route)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.AddCircle,
                            contentDescription = "Agregar Libro"
                        )
                    }

                    // 👤 Botón de perfil (imagen o ícono)
                    IconButton(onClick = {
                        navController.navigate(Screen.Profile.route)
                    }) {
                        if (!user?.photoUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = user?.photoUrl,
                                contentDescription = "Perfil",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Perfil"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (user != null) {
                Spacer(modifier = Modifier.height(32.dp))
                Text("Libros recientes", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                if (books.isEmpty()) {
                    Text("No hay libros publicados.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(books) { book ->
                            BookListItem(book = book) {
                                navController.navigate("book_detail/${book.id}")
                            }
                        }
                    }
                }

                Button(onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screen.Start.route) {
                        popUpTo(0)
                    }
                }) {
                    Text("Cerrar sesión")
                }
            } else {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun BookListItem(book: Book, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            if (!book.coverImageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = book.coverImageUrl,
                    contentDescription = book.title,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = book.description,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
