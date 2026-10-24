package com.example.travcheck.network

// Cấu trúc này phải khớp với Entity bên Spring Boot
data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String,
    val phoneNumber: String?
)