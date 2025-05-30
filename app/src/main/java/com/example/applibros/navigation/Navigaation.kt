package com.example.applibros.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.applibros.ui.auth.*
import com.example.applibros.ui.book.BookDetailScreen
import com.example.applibros.ui.book.CreateBookScreen
import com.example.applibros.ui.book.CreateChapterScreen
import com.example.applibros.ui.book.EditBookScreen
import com.example.applibros.ui.book.EditChapterScreen
import com.example.applibros.ui.book.ReadingScreen
import com.example.applibros.ui.home.HomeScreen
import com.example.applibros.ui.home.SearchScreen
import com.example.applibros.ui.profile.EditProfileScreen
import com.example.applibros.ui.profile.FullListScreen
import com.example.applibros.ui.profile.ProfileContent
import com.example.applibros.ui.profile.ProfileScreen
import com.example.applibros.viewmodel.BookViewModel
import com.example.applibros.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Navigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Start.route) {
        composable(Screen.Start.route) {
            StartScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToHome = { navController.navigate(Screen.Home.route) }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Screen.Home.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Screen.Home.route) }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.CreateBook.route) {
            CreateBookScreen(onBookCreated = {
                navController.navigate(Screen.Home.route)
            })
        }
        composable("profile") {
            ProfileScreen(navController = navController)
        }

        composable("profile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            ProfileScreen(navController = navController, userId = userId)
        }

        composable("edit_profile") {
            EditProfileScreen(onBack = { navController.popBackStack() })
        }

        composable(
            "book_detail/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: return@composable
            val currentUser = FirebaseAuth.getInstance().currentUser
            val currentUserId = currentUser?.uid ?: ""

            BookDetailScreen(
                navController = navController,
                bookId = bookId,
                currentUserId = currentUserId
            )
        }

        composable(
            "edit_book/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: return@composable
            EditBookScreen(navController = navController, bookId = bookId)
        }

        composable(Screen.Search.route) {
            SearchScreen(navController = navController)
        }

        composable("author_profile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            ProfileScreen(navController = navController, userId = userId)
        }

        composable(
            route = "create_chapter/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: return@composable
            CreateChapterScreen(navController = navController, bookId = bookId)
        }

        composable(
            route = "read_book/{bookId}?startChapter={startChapter}",
            arguments = listOf(
                navArgument("bookId") { type = NavType.StringType },
                navArgument("startChapter") {
                    type = NavType.IntType
                    defaultValue = 0
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: return@composable
            val startChapter = backStackEntry.arguments?.getInt("startChapter") ?: 0

            ReadingScreen(navController = navController, bookId = bookId, startChapter = startChapter)
        }

        composable(
            "edit_chapter/{chapterId}",
            arguments = listOf(navArgument("chapterId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: return@composable
            EditChapterScreen(navController = navController, chapterId = chapterId)
        }

        composable("favorites_list") {
            val userViewModel: UserViewModel = viewModel()
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            val favoriteBooks by userViewModel.favoriteBooks.collectAsState()

            LaunchedEffect(currentUserId) {
                currentUserId?.let {
                    userViewModel.loadFavoriteBooks(it)
                }
            }

            FullListScreen(
                title = "Favoritos",
                books = favoriteBooks,
                onBack = { navController.popBackStack() },
                onBookClick = { book -> navController.navigate("book_detail/${book.id}") }
            )
        }


        composable("reading_list_detail/{listName}") { backStackEntry ->
            val userViewModel: UserViewModel = viewModel()
            val listName = backStackEntry.arguments?.getString("listName") ?: ""
            val booksByList by userViewModel.booksByList.collectAsState()

            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            LaunchedEffect(currentUserId) {
                currentUserId?.let {
                    userViewModel.loadBooksFromReadingLists(it)
                }
            }

            val books = booksByList[listName].orEmpty()

            FullListScreen(
                title = listName,
                books = books,
                onBack = { navController.popBackStack() },
                onBookClick = { book -> navController.navigate("book_detail/${book.id}") }
            )
        }


        composable("user_books") {
            val bookViewModel: BookViewModel = viewModel()
            val userBooks by bookViewModel.userBooks.collectAsState()

            FullListScreen(
                title = "Todas las historias",
                books = userBooks,
                onBack = { navController.popBackStack() },
                onBookClick = { book -> navController.navigate("book_detail/${book.id}") }
            )
        }
















    }
}