package com.example.applibros.data.repository

import com.example.applibros.data.firebase.FirestoreService
import com.example.applibros.data.model.User
import com.google.firebase.auth.FirebaseAuth

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestoreService = FirestoreService()

    fun registerUser(
        email: String,
        password: String,
        username: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val firebaseUser = result.user
                if (firebaseUser != null) {
                    val user = User(
                        uid = firebaseUser.uid,
                        username = username,
                        email = firebaseUser.email ?: "",
                        photoUrl = firebaseUser.photoUrl?.toString() ?: ""
                    )
                    firestoreService.createUser(user, onSuccess, onFailure)
                } else {
                    onFailure(Exception("Usuario nulo"))
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun isUserLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

}