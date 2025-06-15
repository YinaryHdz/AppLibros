package com.example.applibros.navigation

sealed class Screen(val route: String) {
    object Start : Screen("start")
    object Register : Screen("register")
    object Home : Screen("home")
    object CreateBook : Screen("create_book")
    object Profile : Screen("profile")
    object Search : Screen("search")


}