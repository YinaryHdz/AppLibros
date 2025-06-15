package com.example.applibros.data.model

data class Comment(
    val id: String = "",
    val bookId: String = "",
    val chapterId: String = "",
    val userId: String = "",
    val content: String = "",
    val parentId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
