package com.example.travcheck.network

import com.example.travcheck.Itinerary
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

// 1. Khai báo các đường dẫn API
interface ApiService {
    @GET("api/users")
    suspend fun getUsers(): List<User> // (Nếu không dùng bảng User cục bộ nữa, bạn có thể xóa hàm này sau)

    // THÊM: Tham số userId để Backend biết ai đang tạo lịch trình
    @GET("api/ai/generate-itinerary")
    suspend fun generateItinerary(
        @Query("destination") destination: String,
        @Query("days") days: Int,
        @Query("userId") userId: String
    ): ResponseBody

    // SỬA: Khớp đường dẫn backend mới và yêu cầu truyền userId
    @GET("api/ai/history")
    suspend fun getHistory(
        @Query("userId") userId: String
    ): List<Itinerary>
}

// 2. Khởi tạo công cụ kết nối
object RetrofitClient {
    // Tạo cấu hình mạng mới, cho phép chờ tối đa 60 giây
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Chèn cấu hình mạng vào Retrofit
    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            // Đảm bảo baseUrl trỏ đúng IP của máy ảo Android
            .baseUrl("http://10.0.2.2:8080/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }
}