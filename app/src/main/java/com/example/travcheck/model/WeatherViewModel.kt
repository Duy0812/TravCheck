package com.example.travcheck.model

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class WeatherViewModel : ViewModel() {
    var temperature = mutableStateOf("")
    var weatherDescription = mutableStateOf("")
    var isLoading = mutableStateOf(false)

    private val client = OkHttpClient()

    fun fetchWeather(lat: Double, lng: Double) {
        isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Gọi API Open-Meteo lấy thời tiết hiện tại
                val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lng&current_weather=true"
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val responseData = response.body?.string() ?: ""

                if (response.isSuccessful && responseData.isNotEmpty()) {
                    val jsonObject = JSONObject(responseData)
                    val currentWeather = jsonObject.getJSONObject("current_weather")
                    val temp = currentWeather.getDouble("temperature")
                    val code = currentWeather.getInt("weathercode")

                    // Giải mã các mã thời tiết (WMO Weather interpretation codes)
                    val desc = when (code) {
                        0 -> "Trời quang mây tạnh ☀️"
                        1, 2, 3 -> "Có mây rải rác ⛅"
                        45, 48 -> "Sương mù 🌫️"
                        51, 53, 55 -> "Mưa phùn 🌧️"
                        61, 63, 65 -> "Mưa rào ☔"
                        71, 73, 75 -> "Tuyết rơi ❄️"
                        95, 96, 99 -> "Sấm chớp / Có bão ⛈️"
                        else -> "Thời tiết có biến động ☁️"
                    }

                    withContext(Dispatchers.Main) {
                        temperature.value = "$temp°C"
                        weatherDescription.value = desc
                        isLoading.value = false
                    }
                } else {
                    withContext(Dispatchers.Main) { isLoading.value = false }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { isLoading.value = false }
            }
        }
    }
}