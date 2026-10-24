package com.example.travcheck.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travcheck.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import okhttp3.Protocol

class ManualViewModel : ViewModel() {
    val destination = mutableStateOf("")
    val days = mutableStateOf("")

    val places = mutableStateListOf<Place>()
    val isLoadingSearch = mutableStateOf(false)

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .protocols(listOf(Protocol.HTTP_1_1))
        .build()

    fun removePlace(place: Place) {
        places.remove(place)
    }

    fun searchAndAddPlace(keyword: String, onResult: (Boolean, String) -> Unit) {
        if (keyword.isBlank()) {
            onResult(false, "Vui lòng nhập tên địa điểm")
            return
        }

        isLoadingSearch.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val encodedName = URLEncoder.encode(keyword, "UTF-8")
                val urlString = "https://photon.komoot.io/api/?q=$encodedName&limit=1"

                val request = Request.Builder()
                    .url(urlString)
                    .addHeader("User-Agent", "TravCheck_Project_1.0")
                    .addHeader("Connection", "close")
                    .build()

                val response = client.newCall(request).execute()
                val responseData = response.body?.string() ?: ""

                if (response.isSuccessful && responseData.isNotEmpty()) {
                    val jsonObject = JSONObject(responseData)
                    val features = jsonObject.optJSONArray("features")

                    if (features != null && features.length() > 0) {
                        val firstFeature = features.getJSONObject(0)
                        val geometry = firstFeature.getJSONObject("geometry")
                        val coordinates = geometry.getJSONArray("coordinates")
                        val lon = coordinates.getDouble(0)
                        val lat = coordinates.getDouble(1)

                        val properties = firstFeature.getJSONObject("properties")
                        val displayName = properties.optString("name", keyword)

                        withContext(Dispatchers.Main) {
                            places.add(Place(name = displayName, lat = lat, lng = lon))
                            isLoadingSearch.value = false
                            onResult(true, "Đã thêm: $displayName")
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            isLoadingSearch.value = false
                            onResult(false, "Không tìm thấy tọa độ cho địa điểm này!")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        isLoadingSearch.value = false
                        onResult(false, "Lỗi Server: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoadingSearch.value = false
                    onResult(false, "Lỗi mạng: ${e.message}")
                }
            }
        }
    }

    fun optimizeRoute(onResult: (Boolean, String) -> Unit) {
        if (places.size < 3) {
            onResult(false, "Cần ít nhất 3 địa điểm để tối ưu hóa lộ trình!")
            return
        }

        isLoadingSearch.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val coordinates = places.joinToString(";") { "${it.lng},${it.lat}" }
                val urlString = "https://router.project-osrm.org/trip/v1/driving/$coordinates?roundtrip=false&source=first"

                val request = Request.Builder()
                    .url(urlString)
                    .addHeader("User-Agent", "TravCheck_Project_1.0 (kinhvolakinh@gmail.com)")
                    .addHeader("Connection", "close")
                    .build()

                val response = client.newCall(request).execute()
                val responseData = response.body?.string() ?: ""

                if (response.isSuccessful && responseData.isNotEmpty()) {
                    val jsonObject = JSONObject(responseData)
                    if (jsonObject.getString("code") == "Ok") {
                        val waypoints = jsonObject.getJSONArray("waypoints")
                        val sortedIndices = IntArray(waypoints.length())
                        for (i in 0 until waypoints.length()) {
                            val wp = waypoints.getJSONObject(i)
                            sortedIndices[i] = wp.getInt("waypoint_index")
                        }

                        val tempArr = arrayOfNulls<Place>(places.size)
                        for (i in places.indices) {
                            tempArr[sortedIndices[i]] = places[i]
                        }

                        withContext(Dispatchers.Main) {
                            places.clear()
                            places.addAll(tempArr.filterNotNull())
                            isLoadingSearch.value = false
                            onResult(true, "Đã tự động sắp xếp đường đi ngắn nhất!")
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            isLoadingSearch.value = false
                            onResult(false, "Không thể tính toán đường đi.")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        isLoadingSearch.value = false
                        onResult(false, "Lỗi Server OSRM: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoadingSearch.value = false
                    onResult(false, "Lỗi API Tìm đường: ${e.message}")
                }
            }
        }
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    fun saveItineraryToFirebase(onResult: (Boolean, String, String) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            onResult(false, "", "Lỗi: Bạn chưa đăng nhập!")
            return
        }

        val inviteCode = generateInviteCode()

        val itineraryData = hashMapOf(
            "name" to destination.value,
            "days" to (days.value.toIntOrNull() ?: 1),
            "places" to places.map { mapOf("name" to it.name, "lat" to it.lat, "lng" to it.lng) },
            "ownerId" to uid,
            "members" to listOf(uid),
            "inviteCode" to inviteCode
        )

        FirebaseFirestore.getInstance().collection("Itineraries")
            .add(itineraryData)
            .addOnSuccessListener {
                destination.value = ""
                days.value = ""
                places.clear()
                onResult(true, inviteCode, "Lưu lịch trình thành công!")
            }
            .addOnFailureListener { e ->
                onResult(false, "", "Lỗi lưu dữ liệu: ${e.message}")
            }
    }

    // ĐÃ CHUYỂN RA NGOÀI: Hàm tham gia chuyến đi bằng mã chia sẻ
    fun joinParty(inviteCode: String, onResult: (Boolean, String) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            onResult(false, "Lỗi: Bạn chưa đăng nhập!")
            return
        }

        val code = inviteCode.trim().uppercase()
        if (code.isEmpty()) {
            onResult(false, "Vui lòng nhập mã chia sẻ!")
            return
        }

        val db = FirebaseFirestore.getInstance()

        db.collection("Itineraries")
            .whereEqualTo("inviteCode", code)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    onResult(false, "Mã chia sẻ không hợp lệ hoặc không tồn tại!")
                    return@addOnSuccessListener
                }

                val docId = documents.documents[0].id
                val itineraryName = documents.documents[0].getString("name") ?: "Chuyến đi"

                db.collection("Itineraries").document(docId)
                    .update("members", FieldValue.arrayUnion(uid))
                    .addOnSuccessListener {
                        onResult(true, "Đã tham gia thành công vào: $itineraryName")
                    }
                    .addOnFailureListener { e ->
                        onResult(false, "Lỗi khi cập nhật thành viên: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, "Lỗi mạng hoặc cơ sở dữ liệu: ${e.message}")
            }
    }
}