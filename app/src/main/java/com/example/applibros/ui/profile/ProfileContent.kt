package com.example.applibros.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    profileUserId: String,
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
            Spacer(modifier = Modifier.height(12.dp))

            // Imagen de perfil con borde
            if (!user.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = "Imagen de perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Imagen de perfil por defecto",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nombre y biografía
            Text(
                text = user.username,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = user.bio.ifBlank { "Sin biografía" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isOwnProfile) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Button(
                        onClick = onEdit,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Editar perfil")
                    }

                    Button(
                        onClick = onLogout,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Cerrar sesión")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            SectionHeader(
                title = "Historias",
                icon = Icons.Default.Book
            )
        }

        if (userBooks.isEmpty()) {
            item {
                EmptySection(
                    icon = Icons.Default.Book,
                    title = "Aún no hay historias publicadas.",
                    message = "Cuando este usuario publique historias, aparecerán aquí."
                )
            }
        } else {
            item {
                val visibleBooks = userBooks.filter { !it.archived && !it.deleted }
                HorizontalBookSection(
                    title = "Historias",
                    books = visibleBooks,
                    onBookClick = onBookClick,
                    onShowMoreClick = {
                        navController.navigate("user_books/$profileUserId")
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
            SectionHeader(
                title = "Listas de lectura públicas",
                icon = Icons.Default.List
            )
        }

        if (readingLists.isEmpty()) {
            item {
                EmptySection(
                    icon = Icons.Default.List,
                    title = "No hay listas públicas.",
                    message = "Cuando haya listas de lectura públicas, las verás aquí."
                )
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
                            navController.navigate("reading_list_detail/$profileUserId/$listName")
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        SectionHeader(title = title) // Usa el SectionHeader estilizado

        if (books.isEmpty()) {
            EmptySection(
                icon = Icons.Default.Book,
                title = "No hay libros disponibles.",
                message = "Los libros aparecerán aquí cuando estén disponibles."
            )
        } else {
            val maxItemsToShow = 5
            val showMore = books.size > maxItemsToShow

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(books.take(maxItemsToShow)) { book ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(140.dp)
                    ) {
                        BookCardCompact(book = book, onClick = { onBookClick(book) })
                        Spacer(modifier = Modifier.height(4.dp))
                        actionButton?.invoke(book)
                    }
                }

                if (showMore) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .width(140.dp)
                                .height(200.dp)
                        ) {
                            OutlinedButton(
                                onClick = onShowMoreClick,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Mostrar más")
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun EmptySection(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}







