package com.example.travcheck.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.travcheck.model.AiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiScreen(aiViewModel: AiViewModel) {
    var destination by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("3") }

    // Các biến trạng thái cho tính năng Lưu & Tạo Party
    var showQrDialog by remember { mutableStateOf(false) }
    var currentInviteCode by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trợ lý AI TravCheck", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Khu vực nhập liệu
            OutlinedTextField(
                value = destination,
                onValueChange = { destination = it },
                label = { Text("Điểm đến (VD: Đà Lạt, Sapa...)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = days,
                onValueChange = { days = it.filter { char -> char.isDigit() } },
                label = { Text("Số ngày") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Nút Tạo lịch trình
            Button(
                onClick = {
                    val daysInt = days.toIntOrNull() ?: 3
                    aiViewModel.fetchItinerary(destination, daysInt)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = destination.isNotBlank() && !aiViewModel.isLoading.value
            ) {
                if (aiViewModel.isLoading.value) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đang xử lý...")
                } else {
                    Text("Tạo lịch trình")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Khu vực hiển thị kết quả hoặc lỗi
            if (aiViewModel.errorMessage.value.isNotEmpty()) {
                Text(
                    text = aiViewModel.errorMessage.value,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (aiViewModel.itineraryResult.value.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Kết quả từ AI:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Khung cuộn chứa nội dung văn bản và nút Lưu
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = aiViewModel.itineraryResult.value,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Nút Lưu xuất hiện khi AI đã tạo xong danh sách địa điểm
                    if (aiViewModel.placesList.value.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                isSaving = true
                                val daysInt = days.toIntOrNull() ?: 3
                                aiViewModel.saveAiItineraryToFirebase(
                                    itineraryName = "Lịch trình $destination (AI)",
                                    days = daysInt
                                ) { isSuccess, inviteCode, message ->
                                    isSaving = false
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    if (isSuccess) {
                                        currentInviteCode = inviteCode
                                        showQrDialog = true
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            enabled = !isSaving
                        ) {
                            Text(if (isSaving) "Đang lưu lên Cloud..." else "Lưu Lịch Trình & Tạo Party")
                        }
                    }
                }
            }
        }
    }

    // Dialog hiển thị mã QR sau khi lưu thành công
    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = {
                Text("Party đã sẵn sàng!", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mã chia sẻ của bạn:")
                    Text(
                        text = currentInviteCode,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Sử dụng chung hàm generateQrCodeBitmap từ ManualItineraryScreen
                    val qrBitmap = remember(currentInviteCode) { generateQrCodeBitmap(currentInviteCode) }
                    qrBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Mã QR",
                            modifier = Modifier.size(200.dp).padding(8.dp)
                        )
                    }
                    Text("Đưa mã này cho bạn bè để cùng tham gia chuyến đi do AI thiết kế!", textAlign = TextAlign.Center)
                }
            },
            confirmButton = {
                Button(onClick = { showQrDialog = false }, modifier = Modifier.fillMaxWidth()) {
                    Text("Xong")
                }
            }
        )
    }
}