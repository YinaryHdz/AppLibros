package com.example.applibros.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.applibros.data.firebase.FirestoreService
import com.example.applibros.data.model.Book
import com.example.applibros.data.model.User
import com.example.applibros.utils.Constants
import com.example.applibros.utils.uploadToImgBB
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserViewModel : ViewModel() {
    private val firestore = FirestoreService()
    private val _currentUser = MutableStateFlow<User?>(null)
    private val _viewedUser = MutableStateFlow<User?>(null)

    val currentUser: StateFlow<User?> = _currentUser
    val viewedUser: StateFlow<User?> = _viewedUser

    fun loadCurrentUser() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.getUserById(
            uid,
            onSuccess = { _currentUser.value = it },
            onFailure = { _currentUser.value = null }
        )
    }

    fun loadUserById(uid: String) {
        firestore.getUserById(
            uid,
            onSuccess = { _viewedUser.value = it },
            onFailure = { _viewedUser.value = null }
        )
    }

    fun updateProfile(photoUrl: String, username: String, bio: String, onComplete: () -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val updates = mutableMapOf<String, Any>()
        if (photoUrl.isNotBlank()) updates["photoUrl"] = photoUrl
        if (username.isNotBlank()) updates["username"] = username
        if (bio.isNotBlank()) updates["bio"] = bio

        firestore.updateUser(
            uid,
            updates,
            onSuccess = {
                // Si se cambió el username, actualizamos los libros también
                if (username.isNotBlank()) {
                    updateAuthorUsernameInBooks(uid, username)
                }
                loadCurrentUser()
                onComplete()
            },
            onFailure = { onComplete() }
        )
    }

    private fun updateAuthorUsernameInBooks(uid: String, newUsername: String) {
        FirebaseFirestore.getInstance().collection("books")
            .whereEqualTo("authorId", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    doc.reference.update(
                        mapOf(
                            "authorUsername" to newUsername,
                            "authorUsername_lowercase" to newUsername.lowercase()
                        )
                    )
                }
            }
    }


    fun uploadProfileImageToImgBBAndSave(
        context: Context,
        imageUri: Uri,
        username: String,
        bio: String,
        onComplete: () -> Unit
    ) {
        uploadToImgBB(
            context = context,
            imageUri = imageUri,
            apiKey = Constants.IMGBB_API_KEY,
            onSuccess = { imageUrl ->
                updateProfile(imageUrl, username, bio, onComplete)
            },
            onFailure = {
                it.printStackTrace()
                onComplete()
            }
        )
    }

    //Carga los libros añadidos a favoritos de cada usuario
    private val _favoriteBooks = MutableStateFlow<List<Book>>(emptyList())
    val favoriteBooks: StateFlow<List<Book>> = _favoriteBooks

    fun loadFavoriteBooks(userId: String) {
        FirebaseFirestore.getInstance()
            .collection("favorites")
            .document(userId)
            .collection("books")
            .get()
            .addOnSuccessListener { snapshot ->
                val books = snapshot.documents.mapNotNull { it.toObject(Book::class.java) }
                    .filter { !it.deleted && !it.archived }
                _favoriteBooks.value = books
            }
    }

    //Cargar las listas de lectura en el perfil
    private val _readingLists = MutableStateFlow<List<String>>(emptyList())
    val readingLists: StateFlow<List<String>> = _readingLists

    fun loadReadingLists(userId: String) {
        FirebaseFirestore.getInstance()
            .collection("readingLists")
            .document(userId)
            .collection("lists")
            .get()
            .addOnSuccessListener { snapshot ->
                val listNames = snapshot.documents.mapNotNull { it.getString("name") }
                _readingLists.value = listNames
            }
    }

    private val _booksByList = MutableStateFlow<Map<String, List<Book>>>(emptyMap())
    val booksByList: StateFlow<Map<String, List<Book>>> = _booksByList

    fun loadBooksFromReadingLists(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val result = mutableMapOf<String, List<Book>>()

        db.collection("readingLists")
            .document(userId)
            .collection("lists")
            .get()
            .addOnSuccessListener { listsSnapshot ->
                val tasks = listsSnapshot.documents.mapNotNull { listDoc ->
                    val listName = listDoc.getString("name") ?: return@mapNotNull null

                    db.collection("readingLists")
                        .document(userId)
                        .collection("lists")
                        .document(listName)
                        .collection("books")
                        .get()
                        .continueWith { booksTask ->
                            val books = booksTask.result?.toObjects(Book::class.java)
                                ?.filter { !it.deleted && !it.archived } ?: emptyList()
                            result[listName] = books

                        }
                }

                Tasks.whenAllSuccess<Any>(tasks).addOnSuccessListener {
                    _booksByList.value = result
                }
            }
    }






}

