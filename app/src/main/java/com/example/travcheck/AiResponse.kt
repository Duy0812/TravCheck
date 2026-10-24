package com.example.travcheck

import com.google.gson.annotations.SerializedName

// Khớp chính xác với cấu trúc JSON mà Gemini sẽ trả về
data class AiResponse(
    @SerializedName("content") var content: String = "",
    @SerializedName("places") var places: List<Place> = emptyList()
)

// Khớp với cả Gson và Firebase (Có var và giá trị mặc định)
data class Place(
    @SerializedName("name") var name: String = "",
    @SerializedName("lat") var lat: Double = 0.0,
    @SerializedName("lng") var lng: Double = 0.0
)