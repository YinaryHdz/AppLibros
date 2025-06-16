package com.example.applibros.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.applibros.data.firebase.FirestoreService
import com.example.applibros.data.model.Book
import com.example.applibros.data.model.Chapter
import com.example.applibros.ui.book.BookValidator
import com.example.applibros.utils.Constants.IMGBB_API_KEY
import com.example.applibros.utils.uploadToImgBB
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BookViewModel : ViewModel() {

    private val firestore = FirestoreService()
    private val _userBooks = MutableStateFlow<List<Book>>(emptyList())
    val userBooks: StateFlow<List<Book>> = _userBooks

    private val _selectedBook = MutableStateFlow<Book?>(null)
    val selectedBook: StateFlow<Book?> = _selectedBook

    private val _bookChapters = MutableStateFlow<List<Chapter>>(emptyList())
    val bookChapters: StateFlow<List<Chapter>> = _bookChapters

    private val _selectedChapter = MutableStateFlow<Chapter?>(null)
    val selectedChapter: StateFlow<Chapter?> = _selectedChapter

    private val db = FirebaseFirestore.getInstance()

    //Crear libro
    fun createBook(
        book: Book,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.createBook(book, onSuccess, onFailure)
    }
    //Crear libro con todos sus componentes
    fun uploadCoverAndCreateBook(
        context: Context,
        title: String,
        description: String,
        genre: String,
        imageUri: Uri?,
        tags: List<String>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        val username = user?.displayName ?: "Anónimo"

        if (user == null) {
            onFailure(Exception("Usuario no autenticado"))
            return
        }

        val validation = BookValidator.validate(title, description, genre, tags)
        if (!validation.isValid) {
            onFailure(Exception(validation.errorMessage ?: "Error de validación"))
            return
        }

        fun buildBook(coverUrl: String = ""): Book {
            return Book(
                title = title,
                description = description,
                genre = genre,
                tags = tags,
                authorId = user.uid,
                authorUsername = username,
                title_lowercase = title.lowercase(),
                authorUsername_lowercase = username.lowercase(),
                coverImageUrl = coverUrl,
                archived = false,
                deleted = false
            )
        }

        // Si no hay imagen
        if (imageUri == null) {
            val book = buildBook()
            createBook(book, onSuccess, onFailure)
            return
        }

        // Subir imagen a ImgBB
        uploadToImgBB(
            context = context,
            imageUri = imageUri,
            apiKey = IMGBB_API_KEY,
            onSuccess = { imageUrl ->
                val book = buildBook(coverUrl = imageUrl)
                createBook(book, onSuccess, onFailure)
            },
            onFailure = { error ->
                onFailure(error)
            }
        )

    }



    fun loadBooksByUser(userId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        firestore.getBooksByUser(userId) { allBooks ->
            val filteredBooks = if (userId == currentUserId) {
                // Soy el autor: muestro todos excepto los eliminados
                allBooks.filter { !it.deleted }
            } else {
                // Otro usuario: muestro solo activos
                allBooks.filter { !it.archived && !it.deleted }
            }
            _userBooks.value = filteredBooks
        }
        Log.d("BookFilter", "Filtrando libros del usuario: $userId - actual: $currentUserId")

    }


    //Carga un libro especifico por su id
    fun loadBookById(bookId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseFirestore.getInstance()
            .collection("books")
            .document(bookId)
            .get()
            .addOnSuccessListener { doc ->
                doc.toObject(Book::class.java)?.let {
                    val book = it.copy(id = doc.id)
                    if (book.deleted) return@addOnSuccessListener
                    if (book.archived && book.authorId != currentUserId) return@addOnSuccessListener
                    _selectedBook.value = book
                }
            }
    }


    //Muestra los capitulos
    fun loadChaptersForBook(bookId: String) {
        FirebaseFirestore.getInstance()
            .collection("books")
            .document(bookId)
            .collection("chapters")
            .orderBy("order")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    _bookChapters.value = snapshot.toObjects(Chapter::class.java)
                }
            }
    }

    //Actualizar informacion general del libro
    fun updateBookData(
        bookId: String,
        coverUrl: String,
        title: String,
        description: String,
        onComplete: () -> Unit
    ) {
        firestore.updateBookData(
            bookId, coverUrl, title, description,
            onSuccess = {
                loadBookById(bookId)
                onComplete()
            },
            onFailure = { onComplete() }
        )
    }
    //Actualiza la informacion general del libro
    fun uploadBookCoverAndUpdate(
        bookId: String,
        uri: Uri,
        title: String,
        description: String,
        onComplete: () -> Unit,
        context: Context
    ) {
        uploadToImgBB(
            context = context,
            imageUri = uri,
            apiKey = IMGBB_API_KEY,
            onSuccess = { imageUrl ->
                updateBookData(bookId, imageUrl, title, description, onComplete)
            },
            onFailure = { onComplete() }
        )
    }


    //Crear capitulos para el libro
    fun createChapter(
        bookId: String,
        title: String,
        content: String,
        onComplete: () -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val chapterRef = FirebaseFirestore.getInstance()
            .collection("books")
            .document(bookId)
            .collection("chapters")
            .document()

        val chapter = Chapter(
            id = chapterRef.id,
            bookId = bookId,
            title = title,
            content = content,
            order = System.currentTimeMillis().toInt()
        )

        chapterRef.set(chapter)
            .addOnSuccessListener {
                // actualiza la marca de tiempo del libro
                FirebaseFirestore.getInstance()
                    .collection("books")
                    .document(bookId)
                    .update("updatedAt", System.currentTimeMillis())
                    .addOnCompleteListener { onComplete() }
            }
            .addOnFailureListener { onComplete() }
    }


    //Busca capitulos para actualizar
    fun loadChapterById(chapterId: String) {
        FirebaseFirestore.getInstance()
            .collectionGroup("chapters")
            .whereEqualTo("id", chapterId)
            .get()
            .addOnSuccessListener { snapshot ->
                val chapter = snapshot.documents.firstOrNull()?.toObject(Chapter::class.java)
                Log.d("EditChapterScreen", "Capítulo obtenido: $chapter")
                _selectedChapter.value = chapter
            }
            .addOnFailureListener { e ->
                Log.e("EditChapterScreen", "Error al obtener capítulo: ", e)
            }
    }


    //Actualiza los capitulos
    fun updateChapter(
        chapterId: String,
        title: String,
        content: String,
        onComplete: () -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collectionGroup("chapters")
            .whereEqualTo("id", chapterId)
            .get()
            .addOnSuccessListener { snapshot ->
                val docRef = snapshot.documents.firstOrNull()?.reference
                if (docRef != null) {
                    val bookId = docRef.path.split("/")[1] // Extrae el ID del libro del path

                    docRef.update(
                        mapOf(
                            "title" to title,
                            "content" to content
                        )
                    ).addOnSuccessListener {
                        //  actualiza la marca de tiempo del libro
                        FirebaseFirestore.getInstance()
                            .collection("books")
                            .document(bookId)
                            .update("updatedAt", System.currentTimeMillis())
                            .addOnCompleteListener { onComplete() }
                    }
                } else {
                    onComplete()
                }
            }
            .addOnFailureListener { onComplete() }
    }

    //Guarda el progreso de lectura
    fun saveReadingProgress(bookId: String, userId: String, chapterIndex: Int) {
        db.collection("readingProgress")
            .document(userId)
            .collection("books")
            .document(bookId)
            .set(
                mapOf(
                    "lastChapterIndex" to chapterIndex,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
    }
    //Busca el progreso de lectura
    fun loadReadingProgress(bookId: String, userId: String, onComplete: (Int) -> Unit) {
        db.collection("readingProgress")
            .document(userId)
            .collection("books")
            .document(bookId)
            .get()
            .addOnSuccessListener { snapshot ->
                val index = snapshot.getLong("lastChapterIndex")?.toInt() ?: 0
                onComplete(index)
            }
            .addOnFailureListener {
                onComplete(0) // default if fails
            }
    }

    //Busca si el libro esta entre favoritos o no
    fun isBookFavorite(bookId: String, userId: String, onResult: (Boolean) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("favorites")
            .document(userId)
            .collection("books")
            .document(bookId)
            .get()
            .addOnSuccessListener { onResult(it.exists()) }
            .addOnFailureListener { onResult(false) }
    }

    // Añade un libro a favoritos
    fun toggleFavorite(book: Book, userId: String, isCurrentlyFavorite: Boolean, onComplete: (Boolean) -> Unit) {
        val docRef = FirebaseFirestore.getInstance()
            .collection("favorites")
            .document(userId)
            .collection("books")
            .document(book.id)

        if (isCurrentlyFavorite) {
            docRef.delete().addOnSuccessListener { onComplete(false) }
        } else {
            docRef.set(book).addOnSuccessListener { onComplete(true) }
        }
    }

    fun loadReadingLists(userId: String, onResult: (List<String>) -> Unit) {
        db.collection("readingLists")
            .document(userId)
            .collection("lists")
            .get()
            .addOnSuccessListener { snapshot ->
                val names = snapshot.documents.mapNotNull { it.getString("name") }
                onResult(names)
            }
    }

    fun addBookToList(userId: String, listName: String, book: Book, onComplete: () -> Unit) {
        val listRef = db.collection("readingLists")
            .document(userId)
            .collection("lists")
            .document(listName)

        listRef.set(mapOf("name" to listName))
        listRef.collection("books").document(book.id).set(book)
            .addOnSuccessListener { onComplete() }
    }

    fun removeBookFromList(userId: String, listName: String, bookId: String, onComplete: () -> Unit) {
        db.collection("readingLists")
            .document(userId)
            .collection("lists")
            .document(listName)
            .collection("books")
            .document(bookId)
            .delete()
            .addOnSuccessListener { onComplete() }
    }

    fun isBookInList(userId: String, listName: String, bookId: String, onResult: (Boolean) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("readingLists")
            .document(userId)
            .collection("lists")
            .document(listName)
            .collection("books")
            .document(bookId)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.exists())
            }
    }
    //Archivar un libro
    fun archiveBook(bookId: String) {
        FirebaseFirestore.getInstance()
            .collection("books")
            .document(bookId)
            .update("archived", true)
    }
    //Eliminar un libro
    fun deleteBook(bookId: String) {
        FirebaseFirestore.getInstance()
            .collection("books")
            .document(bookId)
            .delete()
    }

    //Mostrar los libros archivados
    private val _archivedBooks = MutableStateFlow<List<Book>>(emptyList())
    val archivedBooks: StateFlow<List<Book>> = _archivedBooks

    fun loadArchivedBooks(userId: String) {
        FirebaseFirestore.getInstance()
            .collection("books")
            .whereEqualTo("authorId", userId)
            .whereEqualTo("archived", true)
            .get()
            .addOnSuccessListener { snapshot ->
                val books = snapshot.toObjects(Book::class.java)
                _archivedBooks.value = books
            }
    }

    fun unarchiveBook(bookId: String, onComplete: () -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("books")
            .document(bookId)
            .update("archived", false)
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener { onComplete() }
    }
















}
