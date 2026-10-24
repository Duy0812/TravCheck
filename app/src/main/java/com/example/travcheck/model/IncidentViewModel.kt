package com.example.travcheck.model

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Data class đại diện cho một sự cố
data class Incident(
    var id: String = "",
    val type: String = "",
    val description: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val reportedBy: String = ""
)

class IncidentViewModel : ViewModel() {
    var incidents = mutableStateListOf<Incident>()
    var isReporting = mutableStateOf(false)

    init {
        fetchIncidentsRealtime()
    }

    // Lắng nghe sự cố theo thời gian thực từ Firestore
    private fun fetchIncidentsRealtime() {
        FirebaseFirestore.getInstance().collection("Incidents")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                incidents.clear()
                for (doc in snapshot.documents) {
                    val incident = doc.toObject(Incident::class.java)
                    if (incident != null) {
                        incident.id = doc.id
                        incidents.add(incident)
                    }
                }
            }
    }

    // Gửi báo cáo sự cố bằng tọa độ chỉ định
    fun reportIncident(
        type: String,
        description: String,
        lat: Double,
        lng: Double,
        onResult: (Boolean, String) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "Ẩn danh"
        isReporting.value = true

        val incidentData = Incident("", type, description, lat, lng, uid)
        sendToFirestore(incidentData, onResult)
    }

    // Gửi báo cáo sự cố lấy vị trí thực tế qua GPS thiết bị
    @SuppressLint("MissingPermission")
    fun reportIncidentWithGps(
        context: Context,
        type: String,
        description: String,
        manualLat: Double? = null,
        manualLng: Double? = null,
        onResult: (Boolean, String) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "Ẩn danh"
        isReporting.value = true

        // Nếu có truyền tọa độ thủ công thì ưu tiên dùng
        if (manualLat != null && manualLng != null) {
            sendToFirestore(Incident("", type, description, manualLat, manualLng, uid), onResult)
            return
        }

        // Lấy vị trí GPS thực tế
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                sendToFirestore(
                    Incident("", type, description, location.latitude, location.longitude, uid),
                    onResult
                )
            } else {
                isReporting.value = false
                onResult(false, "Không thể lấy vị trí GPS hiện tại!")
            }
        }.addOnFailureListener { e ->
            isReporting.value = false
            onResult(false, "Lỗi GPS: ${e.message}")
        }
    }

    private fun sendToFirestore(incident: Incident, onResult: (Boolean, String) -> Unit) {
        FirebaseFirestore.getInstance().collection("Incidents")
            .add(incident)
            .addOnSuccessListener {
                isReporting.value = false
                onResult(true, "Đã phát cảnh báo an toàn cho cộng đồng!")
            }
            .addOnFailureListener { e ->
                isReporting.value = false
                onResult(false, "Lỗi: ${e.message}")
            }
    }
}