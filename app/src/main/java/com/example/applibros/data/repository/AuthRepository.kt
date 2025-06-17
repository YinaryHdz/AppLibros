package com.example.applibros.data.repository

import com.example.applibros.data.firebase.FirestoreService
import com.example.applibros.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest

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
                    val profileUpdates = userProfileChangeRequest {
                        displayName = username
                    }

                    firebaseUser.updateProfile(profileUpdates)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = User(
                                    uid = firebaseUser.uid,
                                    username = username,
                                    username_lowercase = username.lowercase(),
                                    email = firebaseUser.email ?: "",
                                    photoUrl = firebaseUser.photoUrl?.toString() ?: ""
                                )
                                firestoreService.createUser(user, onSuccess, onFailure)
                            } else {
                                onFailure(task.exception ?: Exception("Error al actualizar el perfil"))
                            }
                        }
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

    fun resetPassword(email: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onSuccess()
                else onFailure(task.exception ?: Exception("Error desconocido"))
            }
    }

    fun loginWithGoogle(
        idToken: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(task.exception ?: Exception("Google sign-in failed"))
                }
            }
    }




}