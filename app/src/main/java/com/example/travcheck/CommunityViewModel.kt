package com.example.travcheck

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CommunityViewModel : ViewModel() {
    var allItineraries = mutableStateListOf<Itinerary>()
    var isLoading = mutableStateOf(false)

    init {
        fetchAllItineraries()
    }

    fun fetchAllItineraries() {
        isLoading.value = true
        FirebaseFirestore.getInstance().collection("Itineraries")
            .get()
            .addOnSuccessListener { documents ->
                allItineraries.clear()
                for (doc in documents) {
                    try {
                        val itinerary = doc.toObject(Itinerary::class.java)
                        itinerary.id = doc.id
                        allItineraries.add(itinerary)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                isLoading.value = false
            }
            .addOnFailureListener {
                isLoading.value = false
            }
    }

    // MỚI: Hàm xử lý khi người dùng bấm Tham gia chuyến đi
    fun joinItinerary(itineraryId: String, onResult: (Boolean, String) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            onResult(false, "Vui lòng đăng nhập!")
            return
        }

        // Thêm UID của người dùng vào mảng 'members' của chuyến đi này
        FirebaseFirestore.getInstance().collection("Itineraries").document(itineraryId)
            .update("members", FieldValue.arrayUnion(uid))
            .addOnSuccessListener {
                onResult(true, "Đã tham gia! Hãy kiểm tra tab Lịch sử.")
            }
            .addOnFailureListener { e ->
                onResult(false, "Lỗi khi tham gia: ${e.message}")
            }
    }

    fun toggleLike(itineraryId: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance().collection("Itineraries").document(itineraryId)

        db.get().addOnSuccessListener { doc ->
            val currentLikes = doc.get("likes") as? List<String> ?: emptyList()
            val updatedLikes = if (currentLikes.contains(uid)) {
                FieldValue.arrayRemove(uid)
            } else {
                FieldValue.arrayUnion(uid)
            }
            db.update("likes", updatedLikes)
        }
    }
}