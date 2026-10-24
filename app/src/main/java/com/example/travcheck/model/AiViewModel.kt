package com.example.travcheck.model

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travcheck.AiResponse
import com.example.travcheck.Place
import com.example.travcheck.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore // Bổ sung import Firestore
import com.google.gson.Gson
import kotlinx.coroutines.launch

class AiViewModel : ViewModel() {
    var itineraryResult = mutableStateOf("")
        private set

    // Thêm biến để lưu trữ danh sách tọa độ dùng cho bản đồ sau này
    var placesList = mutableStateOf<List<Place>>(emptyList())
        private set

    var isLoading = mutableStateOf(false)
        private set

    var errorMessage = mutableStateOf("")
        private set

    fun fetchItinerary(destination: String, days: Int) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            errorMessage.value = "Vui lòng đăng nhập để tạo lịch trình."
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = ""
            itineraryResult.value = ""
            placesList.value = emptyList()

            try {
                val response = RetrofitClient.instance.generateItinerary(destination, days, uid)
                var rawJson = response.string()

                // Bước 1: Làm sạch chuỗi
                rawJson = rawJson.trim()
                if (rawJson.startsWith("```json")) {
                    rawJson = rawJson.removePrefix("```json")
                } else if (rawJson.startsWith("```")) {
                    rawJson = rawJson.removePrefix("```")
                }
                if (rawJson.endsWith("```")) {
                    rawJson = rawJson.removeSuffix("```")
                }
                rawJson = rawJson.trim()

                // Bước 2: Kiểm tra xem chuỗi có thực sự là JSON không
                if (rawJson.startsWith("{")) {
                    val aiResponse = Gson().fromJson(rawJson, AiResponse::class.java)
                    itineraryResult.value = aiResponse.content
                    placesList.value = aiResponse.places
                } else {
                    errorMessage.value = rawJson
                }

            } catch (e: Exception) {
                errorMessage.value = "Lỗi xử lý dữ liệu: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    // MỚI: Hàm lưu lịch trình AI lên Firebase để tạo Party
    fun saveAiItineraryToFirebase(
        itineraryName: String,
        days: Int,
        onResult: (Boolean, String, String) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            onResult(false, "", "Lỗi: Bạn chưa đăng nhập!")
            return
        }

        if (placesList.value.isEmpty()) {
            onResult(false, "", "AI chưa tạo địa điểm nào để lưu!")
            return
        }

        // Tạo mã Party ngẫu nhiên 6 ký tự
        val inviteCode = (1..6).map { "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".random() }.joinToString("")

        // Đóng gói dữ liệu AI thành chuẩn Firebase giống hệt tab Tạo tay
        val itineraryData = hashMapOf(
            "name" to itineraryName,
            "days" to days,
            "places" to placesList.value.map { mapOf("name" to it.name, "lat" to it.lat, "lng" to it.lng) },
            "ownerId" to uid,
            "members" to listOf(uid),
            "inviteCode" to inviteCode,
            "content" to itineraryResult.value // Lưu luôn cả đoạn văn bản tư vấn của AI
        )

        FirebaseFirestore.getInstance().collection("Itineraries")
            .add(itineraryData)
            .addOnSuccessListener {
                onResult(true, inviteCode, "Lưu lịch trình AI thành công!")
            }
            .addOnFailureListener { e ->
                onResult(false, "", "Lỗi lưu dữ liệu: ${e.message}")
            }
    }
}