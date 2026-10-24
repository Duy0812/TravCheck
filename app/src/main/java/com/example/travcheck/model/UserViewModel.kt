package com.example.travcheck.model

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travcheck.network.RetrofitClient
import com.example.travcheck.network.User
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    // Biến lưu trữ danh sách người dùng lấy từ Spring Boot
    var userList = mutableStateListOf<User>()
        private set

    var errorMessage = mutableStateOf("")
        private set

    var isLoading = mutableStateOf(true)
        private set

    init {
        fetchUsers() // Tự động gọi API khi mở app
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            try {
                isLoading.value = true
                val users = RetrofitClient.instance.getUsers()
                userList.clear()
                userList.addAll(users)
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Lỗi kết nối mạng"
            } finally {
                isLoading.value = false
            }
        }
    }
}