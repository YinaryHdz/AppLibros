package com.example.applibros.data.model

data class Chapter(
    val id: String = "",
    val bookId: String = "",
    val title: String = "",
    val content: String = "",
    val order: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
