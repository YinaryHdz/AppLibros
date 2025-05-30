package com.example.applibros.viewmodel

import androidx.lifecycle.ViewModel
import com.example.applibros.data.repository.AuthRepository

class AuthViewModel : ViewModel() {
    private val authRepo = AuthRepository()
    //Funcion para dar registro a un usuario
    fun register(
        email: String,
        password: String,
        username: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        authRepo.registerUser(email, password, username, onSuccess, onFailure)
    }
    //Funcion para logear a un usuario ya registrado
    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        authRepo.loginUser(email, password, onSuccess, onFailure)
    }

    //Manejo de sesión y la redirección automática según si el usuario está autenticado o no.
    fun checkUserLoggedIn(): Boolean {
        return authRepo.isUserLoggedIn()
    }


}