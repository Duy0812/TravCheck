package com.example.travcheck.model

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.travcheck.Itinerary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class HistoryViewModel : ViewModel() {
    var itineraries = mutableStateListOf<Itinerary>()
    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf("")

    fun fetchHistory() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        isLoading.value = true
        errorMessage.value = ""

        FirebaseFirestore.getInstance().collection("Itineraries")
            .whereArrayContains("members", uid)
            .get()
            .addOnSuccessListener { documents ->
                itineraries.clear()
                for (doc in documents) {
                    val item = doc.toObject(Itinerary::class.java)
                    item.id = doc.id
                    itineraries.add(item)
                }
                isLoading.value = false
            }
            .addOnFailureListener { e ->
                errorMessage.value = e.message ?: "Lỗi tải lịch sử"
                isLoading.value = false
            }
    }

    // Hàm xóa chuyến đi
    fun deleteItinerary(itineraryId: String, onResult: (Boolean, String) -> Unit) {
        if (itineraryId.isBlank()) {
            onResult(false, "Mã chuyến đi không hợp lệ!")
            return
        }

        FirebaseFirestore.getInstance().collection("Itineraries").document(itineraryId)
            .delete()
            .addOnSuccessListener {
                onResult(true, "Đã xóa chuyến đi thành công!")
            }
            .addOnFailureListener { e ->
                onResult(false, "Lỗi khi xóa: ${e.message}")
            }
    }

    // Hàm tham gia Party bằng Mã 6 ký tự
    fun joinParty(inviteCode: String, onResult: (Boolean, String) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (inviteCode.isBlank()) {
            onResult(false, "Mã không được để trống!")
            return
        }

        FirebaseFirestore.getInstance().collection("Itineraries")
            .whereEqualTo("inviteCode", inviteCode)
            .get()
            .addOnSuccessListener { query ->
                if (query.isEmpty) {
                    onResult(false, "Mã Party không tồn tại!")
                } else {
                    val doc = query.documents[0]
                    doc.reference.update("members", FieldValue.arrayUnion(uid))
                        .addOnSuccessListener {
                            fetchHistory()
                            onResult(true, "Tham gia Party thành công!")
                        }
                        .addOnFailureListener { e ->
                            onResult(false, "Lỗi tham gia: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                onResult(false, "Lỗi tìm kiếm: ${e.message}")
            }
    }
}