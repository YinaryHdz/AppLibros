package com.example.applibros.data.model

data class Comment(
    val id: String = "",
    val bookId: String = "",
    val userId: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
