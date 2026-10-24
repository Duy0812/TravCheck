package com.example.travcheck

data class ItineraryItem(
    val id: String = "",
    val name: String = "",
    val days: Int = 1,
    val ownerId: String = "",
    val members: List<String> = emptyList(),
    val inviteCode: String = ""
)