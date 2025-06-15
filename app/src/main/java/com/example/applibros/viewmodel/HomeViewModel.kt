package com.example.applibros.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.applibros.data.firebase.FirestoreService
import com.example.applibros.data.model.Book
import com.example.applibros.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val firestore = FirestoreService()
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user
    private val _allBooks = MutableStateFlow<List<Book>>(emptyList())
    val allBooks: StateFlow<List<Book>> = _allBooks

    init {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                firestore.getUserById(
                    uid = uid,
                    onSuccess = { _user.value = it },
                    onFailure = { _user.value = null }
                )
            }
        }

        loadAllBooks()
    }

    fun loadAllBooks() {
        FirebaseFirestore.getInstance()
            .collection("books")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val books = snapshot.toObjects(Book::class.java)
                        .filter { !it.archived && !it.deleted }
                        .sortedByDescending { it.createdAt }

                    _allBooks.value = books
                }
            }
    }

}