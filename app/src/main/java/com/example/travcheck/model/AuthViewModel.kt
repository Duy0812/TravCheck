package com.example.travcheck.model

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    // Lưu trữ trạng thái người dùng (null nghĩa là chưa đăng nhập)
    var currentUser = mutableStateOf<FirebaseUser?>(auth.currentUser)
        private set

    var isLoading = mutableStateOf(false)
        private set

    var errorMessage = mutableStateOf("")
        private set

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            errorMessage.value = "Vui lòng nhập đầy đủ thông tin"
            return
        }
        isLoading.value = true
        errorMessage.value = ""
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    currentUser.value = auth.currentUser
                } else {
                    errorMessage.value = task.exception?.message ?: "Đăng nhập thất bại"
                }
            }
    }

    fun register(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            errorMessage.value = "Vui lòng nhập đầy đủ thông tin"
            return
        }
        isLoading.value = true
        errorMessage.value = ""
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    currentUser.value = auth.currentUser
                } else {
                    errorMessage.value = task.exception?.message ?: "Đăng ký thất bại"
                }
            }
    }

    fun logout() {
        auth.signOut()
        currentUser.value = null
    }
}