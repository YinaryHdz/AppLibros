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

    fun resetPassword(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        authRepo.resetPassword(email, onSuccess, onFailure)
    }
    fun loginWithGoogle(
        idToken: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        authRepo.loginWithGoogle(idToken, onSuccess, onFailure)
    }

    fun checkUserLoggedIn(): Boolean {
        return authRepo.isUserLoggedIn()
    }





}