package com.example.travcheck

data class Itinerary(
    var id: String = "",
    val name: String = "",
    val days: Int = 0,
    val places: List<Place> = emptyList(),
    val ownerId: String = "",
    val members: List<String> = emptyList(),
    val inviteCode: String = "",
    val content: String = "",
    val likes: List<String> = emptyList() // Bổ sung thuộc tính lưu danh sách UID thích chuyến đi
)