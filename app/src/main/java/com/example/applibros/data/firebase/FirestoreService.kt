package com.example.applibros.data.firebase

import android.net.Uri
import androidx.compose.runtime.Composable
import com.example.applibros.data.model.Book
import com.example.applibros.data.model.Comment
import com.example.applibros.data.model.CommentWithUser
import com.example.applibros.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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

    fun addComment(
        comment: Comment,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val docRef = db.collection("comments").document()
        val commentWithId = comment.copy(id = docRef.id)
        docRef.set(commentWithId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }



    fun listenToCommentsWithUsers(
        bookId: String,
        chapterId: String,
        onUpdate: (List<CommentWithUser>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return db.collection("comments")
            .whereEqualTo("bookId", bookId)
            .whereEqualTo("chapterId", chapterId)
            .orderBy("createdAt")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    onError(e)
                    return@addSnapshotListener
                }

                val comments = snapshots?.documents
                    ?.mapNotNull { it.toObject(Comment::class.java) }
                    ?: emptyList()

                if (comments.isEmpty()) {
                    onUpdate(emptyList()) // ✅ Notifica que ya no hay comentarios
                    return@addSnapshotListener
                }

                val userIds = comments.map { it.userId }.toSet()

                // ⚠️ Firestore no permite whereIn con listas vacías, pero ya lo evitamos arriba
                db.collection("users")
                    .whereIn("uid", userIds.toList())
                    .get()
                    .addOnSuccessListener { userResult ->
                        val users = userResult.documents.mapNotNull { it.toObject(User::class.java) }
                        val userMap = users.associateBy { it.uid }

                        val combined = comments.map { comment ->
                            CommentWithUser(comment, userMap[comment.userId])
                        }

                        onUpdate(combined)
                    }
                    .addOnFailureListener { onError(it) }
            }
    }


    fun deleteComment(
        commentId: String,
        onSuccess:  () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("comments").document(commentId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
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


    fun getBooksByUser(userId: String, onResult: (List<Book>) -> Unit) {
        db.collection("books")
            .whereEqualTo("authorId", userId)
            .get()
            .addOnSuccessListener { result ->
                val books = result.mapNotNull { doc ->
                    val book = doc.toObject(Book::class.java).copy(id = doc.id)
                    if (!book.deleted) book else null
                }
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
                    .filter { !it.archived && !it.deleted }
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