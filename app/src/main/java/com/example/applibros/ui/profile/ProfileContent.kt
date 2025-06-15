package com.example.applibros.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.applibros.data.model.Book
import com.example.applibros.data.model.User
import com.example.applibros.ui.book.BookListItem
import com.example.applibros.viewmodel.BookViewModel

@Composable
fun ProfileContent(
    navController: NavController,
    user: User,
    isOwnProfile: Boolean,
    userBooks: List<Book>,
    favoriteBooks: List<Book>,
    readingLists: List<String>,
    booksByList: Map<String, List<Book>>,
    archivedBooks: List<Book>,
    bookViewModel: BookViewModel,
    onLogout: () -> Unit,
    onEdit: () -> Unit,
    onBookClick: (Book) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Imagen de perfil
            if (!user.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = "Imagen de perfil",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Imagen de perfil por defecto",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nombre de usuario y biografía
            Text(user.username, style = MaterialTheme.typography.titleMedium)
            Text(user.bio, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            // Botones si es su propio perfil
            if (isOwnProfile) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = onEdit) {
                        Text("Editar perfil")
                    }
                    Button(onClick = onLogout) {
                        Text("Cerrar sesión")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Historias", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (userBooks.isEmpty()) {
            item {
                Text("Aún no hay historias publicadas.")
            }
        } else {
            item {
                val visibleBooks = userBooks.filter { !it.archived && !it.deleted }
                HorizontalBookSection(
                    title = "Historias",
                    books = visibleBooks,
                    onBookClick = onBookClick,
                    onShowMoreClick = {
                        navController.navigate("user_books")
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

        }

        if (isOwnProfile) {
            item {
                HorizontalBookSection(
                    title = "Favoritos",
                    books = favoriteBooks,
                    onBookClick = onBookClick,
                    onShowMoreClick = { navController.navigate("favorites_list") }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }


        // Esta parte sigue dentro del LazyColumn
        item {
            Text("Listas de lectura públicas", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (readingLists.isEmpty()) {
            item {
                Text("No hay listas públicas.")
            }
        } else {
            readingLists.forEach { listName ->
                val books = booksByList[listName].orEmpty()

                item {
                    HorizontalBookSection(
                        title = listName,
                        books = books,
                        onBookClick = onBookClick,
                        onShowMoreClick = {
                            navController.navigate("reading_list_detail/${listName}")
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
        if (isOwnProfile && archivedBooks.isNotEmpty()) {
            item {
                HorizontalBookSection(
                    title = "Archivados",
                    books = archivedBooks,
                    onBookClick = onBookClick,
                    onShowMoreClick = { navController.navigate("archived_books") }, // Si haces una vista aparte
                    actionButton = { book ->
                        Button(onClick = {
                            bookViewModel.unarchiveBook(book.id) {
                                bookViewModel.loadArchivedBooks(user.uid) // Recarga
                                bookViewModel.loadBooksByUser(user.uid)   // Por si lo devuelve al listado activo
                            }
                        }) {
                            Text("Desarchivar")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

        }


    }
}
@Composable
fun HorizontalBookSection(
    title: String,
    books: List<Book>,
    onBookClick: (Book) -> Unit,
    onShowMoreClick: () -> Unit,
    actionButton: @Composable ((Book) -> Unit)? = null
) {
    Column {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))

        if (books.isEmpty()) {
            Text("No hay libros disponibles.")
        } else {
            val maxItemsToShow = 5
            val showMore = books.size > maxItemsToShow

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start)
            ) {
                items(books.take(maxItemsToShow)) { book ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        BookCardCompact(book = book, onClick = { onBookClick(book) })
                        actionButton?.invoke(book)
                    }
                }

                if (showMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .height(120.dp)
                                .wrapContentHeight(align = Alignment.CenterVertically)
                        ) {
                            Button(onClick = onShowMoreClick) {
                                Text("Mostrar más")
                            }
                        }
                    }
                }
            }
        }
    }
}





