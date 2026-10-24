package com.example.travcheck.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
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
import com.example.travcheck.model.ManualViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

// Hàm tạo ảnh Bitmap mã QR
fun generateQrCodeBitmap(text: String, size: Int = 512): Bitmap? {
    return try {
        val bitMatrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(
                    x, y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                )
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualItineraryScreen(viewModel: ManualViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }

    // Các biến trạng thái để hiển thị mã QR
    var showQrDialog by remember { mutableStateOf(false) }
    var currentInviteCode by remember { mutableStateOf("") }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lên lịch trình thủ công", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Thêm địa điểm")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = viewModel.destination.value,
                onValueChange = { viewModel.destination.value = it },
                label = { Text("Tên chuyến đi (VD: Phượt Đà Lạt)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = viewModel.days.value,
                onValueChange = { viewModel.days.value = it },
                label = { Text("Số ngày") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Danh sách điểm đến (Checkpoints):", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (viewModel.places.isEmpty()) {
                Text("Chưa có địa điểm nào. Bấm dấu + để thêm.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(viewModel.places) { place ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = place.name, fontWeight = FontWeight.Medium)
                                    Text(
                                        text = "Lat: ${String.format("%.4f", place.lat)} | Lng: ${String.format("%.4f", place.lng)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(onClick = { viewModel.removePlace(place) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            if (viewModel.places.size >= 3) {
                OutlinedButton(
                    onClick = {
                        viewModel.optimizeRoute { isSuccess, message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    enabled = !viewModel.isLoadingSearch.value
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (viewModel.isLoadingSearch.value) "Đang tính toán..." else "Tìm đường đi ngắn nhất")
                }
            }

            // Nút Lưu gọi hàm Firebase
            Button(
                onClick = {
                    viewModel.saveItineraryToFirebase { isSuccess, inviteCode, message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        if (isSuccess) {
                            currentInviteCode = inviteCode
                            showQrDialog = true // Mở popup quét QR
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                enabled = viewModel.destination.value.isNotEmpty() && viewModel.places.isNotEmpty()
            ) {
                Text("Lưu Lịch Trình & Tạo Party")
            }
        }
    }

    // Dialog giao diện TÌM KIẾM
    if (showAddDialog) {
        var placeKeyword by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = {
                if (!viewModel.isLoadingSearch.value) showAddDialog = false
            },
            title = { Text("Tìm kiếm địa điểm") },
            text = {
                Column {
                    OutlinedTextField(
                        value = placeKeyword,
                        onValueChange = { placeKeyword = it },
                        label = { Text("Nhập tên (VD: Chợ Bến Thành)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (viewModel.isLoadingSearch.value) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Đang định vị...")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.searchAndAddPlace(placeKeyword) { isSuccess, message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            if (isSuccess) {
                                showAddDialog = false
                            }
                        }
                    },
                    enabled = placeKeyword.isNotBlank() && !viewModel.isLoadingSearch.value
                ) {
                    Text("Thêm vào lịch trình")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddDialog = false },
                    enabled = !viewModel.isLoadingSearch.value
                ) { Text("Hủy") }
            }
        )
    }

    // Dialog hiển thị mã QR sau khi tạo Party
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

                    val qrBitmap = remember(currentInviteCode) { generateQrCodeBitmap(currentInviteCode) }
                    qrBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Mã QR",
                            modifier = Modifier.size(200.dp).padding(8.dp)
                        )
                    }
                    Text("Đưa mã này cho bạn bè để cùng tham gia chuyến đi!", textAlign = TextAlign.Center)
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