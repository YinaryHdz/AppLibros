package com.example.applibros.ui.profile

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.applibros.navigation.Screen
import com.example.applibros.viewmodel.BookViewModel
import com.example.applibros.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(
    navController: NavController,
    userId: String? = null
) {
    val context = LocalContext.current

    val userViewModel: UserViewModel = viewModel()
    val bookViewModel: BookViewModel = viewModel()

    val currentUser by userViewModel.currentUser.collectAsState()
    val viewedUser by userViewModel.viewedUser.collectAsState()
    val userBooks by bookViewModel.userBooks.collectAsState()

    val isOwnProfile = userId == null || userId == currentUser?.uid
    val user = if (isOwnProfile) currentUser else viewedUser

    val favoriteBooks by userViewModel.favoriteBooks.collectAsState()
    val userLists by userViewModel.readingLists.collectAsState()

    val booksByList by userViewModel.booksByList.collectAsState()


    LaunchedEffect(userId) {
        val uid = if (isOwnProfile) FirebaseAuth.getInstance().currentUser?.uid else userId
        uid?.let {
            userViewModel.loadReadingLists(it)
            userViewModel.loadBooksFromReadingLists(it)
        }

        if (isOwnProfile) {
            userViewModel.loadCurrentUser()
            userViewModel.loadFavoriteBooks(FirebaseAuth.getInstance().currentUser?.uid ?: "")
        } else {
            userViewModel.loadUserById(userId!!)
        }
    }



    // Carga de libros cuando el usuario esté listo
    LaunchedEffect(user) {
        user?.uid?.let { bookViewModel.loadBooksByUser(it) }
    }

    if (user != null) {
        ProfileContent(
            navController = navController,
            user = user,
            isOwnProfile = isOwnProfile,
            userBooks = userBooks, // ✅ Pasamos libros
            favoriteBooks = if (isOwnProfile) favoriteBooks else emptyList(),
            readingLists = userLists,
            booksByList = booksByList,
            onLogout = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate(Screen.Start.route) {
                    popUpTo(0)
                }
            },
            onEdit = {
                navController.navigate("edit_profile")
            },
            onBookClick = { book ->
                navController.navigate("book_detail/${book.id}")
            }
        )
    }
}


