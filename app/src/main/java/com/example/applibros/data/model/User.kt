package com.example.applibros.data.model

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val bio: String = "",
    val username_lowercase:String = "",
    val photoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
