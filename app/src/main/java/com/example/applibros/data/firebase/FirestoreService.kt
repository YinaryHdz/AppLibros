package com.example.applibros.data.firebase

import android.net.Uri
import com.example.applibros.data.model.Book
import com.example.applibros.data.model.Chapter
import com.example.applibros.data.model.Comment
import com.example.applibros.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

data class SimpleUser(val uid: String, val username: String)
class FirestoreService {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()


    fun createUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users")
            .document(user.uid)
            .set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun createBook(book: Book, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val doc = db.collection("books").document()
        book.id = doc.id
        doc.set(book)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun addComment(comment: Comment, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("comments")
            .add(comment)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }


    //Obtener el nombre de usuario para personalizar la app
    fun getUserById(
        uid: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) onSuccess(user)
                else onFailure(Exception("Usuario no encontrado"))
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    //Actualizar informacion del usuario
    fun updateUser(
        uid: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    //Actualizar imagen del usuario
    fun uploadProfileImage(
        uid: String,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val ref = storage.reference.child("profile_images/$uid.jpg")

        ref.putFile(imageUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }.addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }


    fun getBooksByUser(
        userId: String,
        onResult: (List<Book>) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection("books")
            .whereEqualTo("authorId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val books = snapshot.toObjects(Book::class.java)
                onResult(books)
            }
    }

    fun updateBookData(
        bookId: String,
        coverUrl: String,
        title: String,
        description: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection("books")
            .document(bookId)
            .update(
                mapOf(
                    "coverImageUrl" to coverUrl,
                    "title" to title,
                    "description" to description,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }


    fun uploadBookCover(
        bookId: String,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val ref = FirebaseStorage.getInstance().reference.child("book_covers/$bookId.jpg")
        ref.putFile(imageUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }.addOnFailureListener(onFailure)
            }
            .addOnFailureListener(onFailure)
    }


    fun searchBooks(
        query: String,
        byAuthor: Boolean,
        onSuccess: (List<Book>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val lowercaseQuery = query.lowercase()
        val field = if (byAuthor) "authorUsername_lowercase" else "title_lowercase"

        db.collection("books")
            .orderBy(field)
            .startAt(lowercaseQuery)
            .endAt(lowercaseQuery + "\uf8ff")
            .get()
            .addOnSuccessListener { snapshot ->
                val books = snapshot.toObjects(Book::class.java)
                onSuccess(books)
            }
            .addOnFailureListener(onFailure)
    }

    fun searchUsersByUsername(
        query: String,
        onSuccess: (List<SimpleUser>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users")
            .orderBy("username_lowercase")
            .startAt(query.lowercase())
            .endAt(query.lowercase() + "\uf8ff")
            .get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.documents.mapNotNull {
                    val uid = it.getString("uid")
                    val username = it.getString("username")
                    if (uid != null && username != null) SimpleUser(uid, username) else null
                }
                onSuccess(users)
            }
            .addOnFailureListener(onFailure)
    }









}