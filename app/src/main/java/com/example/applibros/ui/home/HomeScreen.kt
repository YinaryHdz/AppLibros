package com.example.applibros.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.example.applibros.R
import com.example.applibros.data.model.Book
import com.example.applibros.navigation.Screen
import com.example.applibros.viewmodel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = viewModel()
    val user by viewModel.user.collectAsState()
    val books by viewModel.allBooks.collectAsState()

    val listState = rememberLazyListState()
    val selectedIndex = remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val recentlyUpdatedBooks = books.sortedByDescending { it.updatedAt }.take(10)
    val recentListState = rememberLazyListState()
    val selectedRecentIndex = remember { mutableStateOf(0) }
    val hasRecentManualSelection = remember { mutableStateOf(false) }

    val oldBooks = books.sortedBy { it.createdAt }.take(5)
    val popularBooks = books.sortedByDescending { it.views }.take(5)
    val recentBooks = books.sortedByDescending { it.createdAt }.take(5)

    val discoveredBooks = (oldBooks + popularBooks + recentBooks)
        .distinctBy { it.id }
        .shuffled()


    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        viewModel.loadUserData()
    }


    LaunchedEffect(recentListState.firstVisibleItemScrollOffset, recentListState.firstVisibleItemIndex) {
        val layoutInfo = recentListState.layoutInfo
        val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset / 2
        val centered = layoutInfo.visibleItemsInfo.minByOrNull {
            kotlin.math.abs((it.offset + it.size / 2) - viewportCenter)
        }
        centered?.let {
            if (hasRecentManualSelection.value) {
                hasRecentManualSelection.value = false
            } else {
                selectedRecentIndex.value = it.index
            }
        }
    }

    LaunchedEffect(listState.firstVisibleItemScrollOffset, listState.firstVisibleItemIndex) {
        val layoutInfo = listState.layoutInfo
        val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset / 2
        val centered = layoutInfo.visibleItemsInfo.minByOrNull {
            kotlin.math.abs((it.offset + it.size / 2) - viewportCenter)
        }
        centered?.let {
            selectedIndex.value = it.index
        }
    }

    val selectedBook = books.getOrNull(selectedIndex.value)

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "LibroLibre",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = FontFamily(Font(R.font.dancing_script_bold)), // Paso 2
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                color = Color(0xFF2196F3) // Azul del logo
                            )
                        )
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color(0xFF2196F3))
                        }
                        IconButton(onClick = { navController.navigate(Screen.CreateBook.route) }) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Agregar", tint = Color(0xFF2196F3))
                        }
                        IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                            if (!user?.photoUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = user?.photoUrl,
                                    contentDescription = "Perfil",
                                    modifier = Modifier.size(32.dp).clip(CircleShape)
                                )
                            } else {
                                Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Color(0xFF2196F3))
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFE6F6FF) // Fondo celeste claro
                    )
                )
            }


        ) { paddingValues ->
            if (user == null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues)
                        .padding(bottom = 16.dp)
                ) {
                    SectionCard(title = "📚 Descubre") {
                        LazyRow(
                            state = listState,
                            flingBehavior = rememberSnapFlingBehavior(listState),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(books) { index, book ->
                                AnimatedBookCoverItem(
                                    book = book,
                                    isSelected = index == selectedIndex.value
                                ) {
                                    selectedIndex.value = index
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(index)
                                    }
                                }
                            }
                        }

                        selectedBook?.let { book ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                                    .animateContentSize(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        book.title,
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = book.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                                        maxLines = 4,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Género: ${book.genre}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "Actualizado: ${getRelativeTime(book.updatedAt)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { navController.navigate("book_detail/${book.id}") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(50),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("Leer ahora", color = MaterialTheme.colorScheme.onPrimary)
                                    }
                                }
                            }
                        }
                    }

                    SectionCard(title = "📅 Actualizados recientemente") {
                        LazyRow(
                            state = recentListState,
                            flingBehavior = rememberSnapFlingBehavior(recentListState),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(recentlyUpdatedBooks) { index, book ->
                                AnimatedBookCoverItem(
                                    book = book,
                                    isSelected = index == selectedRecentIndex.value
                                ) {
                                    selectedRecentIndex.value = index
                                    hasRecentManualSelection.value = true
                                    coroutineScope.launch {
                                        recentListState.animateScrollToItem(index)
                                    }
                                }
                            }
                        }

                        recentlyUpdatedBooks.getOrNull(selectedRecentIndex.value)?.let { book ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                                    .animateContentSize(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        book.title,
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = book.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                                        maxLines = 4,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Género: ${book.genre}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "Actualizado: ${getRelativeTime(book.updatedAt)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { navController.navigate("book_detail/${book.id}") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(50),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("Leer ahora", color = MaterialTheme.colorScheme.onPrimary)
                                    }
                                }
                            }
                        }
                    }



                    // Secciones adicionales
                    val mostViewed = books.sortedByDescending { it.views }.take(10)
                    val booksByGenre = books.groupBy { it.genre }
                    val popularTags = books.flatMap { it.tags }
                        .groupingBy { it }.eachCount()
                        .toList().sortedByDescending { it.second }
                        .take(5).map { it.first }

                    val booksByTag = popularTags.associateWith { tag ->
                        books.filter { it.tags.contains(tag) }.take(10)
                    }

                    TaggedSection("📈 Más vistos", mostViewed) {
                        navController.navigate("book_detail/${it.id}")
                    }

                    booksByGenre.forEach { (genre, booksForGenre) ->
                        if (booksForGenre.isNotEmpty()) {
                            TaggedSection("🎨 Género: $genre", booksForGenre) {
                                navController.navigate("book_detail/${it.id}")
                            }
                        }
                    }

                    booksByTag.forEach { (tag, booksForTag) ->
                        if (booksForTag.isNotEmpty()) {
                            TaggedSection("🏷️ Etiqueta: $tag", booksForTag) {
                                navController.navigate("book_detail/${it.id}")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate(Screen.Start.route) { popUpTo(0) }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Text("Cerrar sesión", color = Color.White)
                    }
                }
            }
        }
    }
}
@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}



@Composable
fun TransparentBookCard(book: Book, fixedHeight: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(220.dp)
            .then(if (fixedHeight) Modifier.height(280.dp) else Modifier)
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(16.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(book.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis,     color = Color.White.copy(alpha = 0.9f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(book.description, style = MaterialTheme.typography.bodySmall, maxLines = 3, overflow = TextOverflow.Ellipsis,     color = Color.White.copy(alpha = 0.9f),)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Género: ${book.genre}", style = MaterialTheme.typography.labelSmall,     color = Color.White.copy(alpha = 0.9f),)
            Text("Actualizado: ${getRelativeTime(book.updatedAt)}", style = MaterialTheme.typography.labelSmall,     color = Color.White.copy(alpha = 0.9f),)
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                Text("Leer ahora")
            }
        }
    }
}

@Composable
fun AnimatedBookCoverItem(book: Book, isSelected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (isSelected) 1.1f else 0.95f, label = "coverScale")
    Card(
        modifier = Modifier
            .width(140.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(if (isSelected) 8.dp else 2.dp)
    ) {
        AsyncImage(
            model = book.coverImageUrl,
            contentDescription = book.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Crop
        )
    }
}

fun getRelativeTime(timeMillis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timeMillis
    val minutes = diff / 60000
    return when {
        minutes < 1 -> "hace < 1 min"
        minutes < 60 -> "hace $minutes min"
        else -> "hace ${minutes / 60} h"
    }
}


@Composable
fun TaggedSection(
    title: String,
    books: List<Book>,
    onBookClick: (Book) -> Unit
) {
    SectionCard(title = title) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(books) { book ->
                BookCarouselItem(book = book) {
                    onBookClick(book)
                }
            }
        }
    }
}




@Composable
fun BookCarouselItem(book: Book, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .padding(end = 12.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = book.coverImageUrl,
                contentDescription = book.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = book.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Leer ahora", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}





