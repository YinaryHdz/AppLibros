package com.example.applibros.data.model

data class Book(
    var id: String = "",
    val title: String = "",
    val description: String = "",
    val authorId: String = "",
    val authorUsername: String = "",
    val genre: String = "",
    val coverImageUrl: String = "",
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val likes: Int = 0,
    val views: Int = 0,
    val title_lowercase: String = "",
    val authorUsername_lowercase: String = ""
)
