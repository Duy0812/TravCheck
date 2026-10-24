package com.example.travcheck.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.travcheck.Itinerary
import com.example.travcheck.model.AuthViewModel
import com.example.travcheck.model.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    authViewModel: AuthViewModel,
    onItemClick: (Itinerary) -> Unit
) {
    val context = LocalContext.current
    var showJoinDialog by remember { mutableStateOf(false) }
    var itineraryToDelete by remember { mutableStateOf<Itinerary?>(null) }

    // Tự động tải dữ liệu khi màn hình được mở
    LaunchedEffect(Unit) {
        viewModel.fetchHistory()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử chuyến đi", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = { authViewModel.logout() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Đăng xuất")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showJoinDialog = true },
                icon = { Icon(Icons.Default.GroupAdd, contentDescription = "Tham gia") },
                text = { Text("Tham gia Party") }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (viewModel.isLoading.value) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (viewModel.errorMessage.value.isNotEmpty()) {
                Text(
                    text = viewModel.errorMessage.value,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else if (viewModel.itineraries.isEmpty()) {
                Text(
                    text = "Bạn chưa có chuyến đi nào. Hãy tạo hoặc tham gia cùng bạn bè nhé!",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.itineraries) { itinerary ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemClick(itinerary) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = itinerary.name.ifEmpty { "Chuyến đi chưa đặt tên" },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Thời gian: ${itinerary.days} ngày\nThành viên: ${itinerary.members.size} người\nMã Party: ${itinerary.inviteCode}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(onClick = { itineraryToDelete = itinerary }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Xóa",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog nhập mã Tham gia Party
    if (showJoinDialog) {
        var inputCode by remember { mutableStateOf("") }
        var isJoining by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isJoining) showJoinDialog = false },
            title = { Text("Tham gia Party", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = inputCode,
                    onValueChange = { inputCode = it.uppercase() },
                    label = { Text("Nhập mã 6 ký tự (VD: TRV24X)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isJoining = true
                        viewModel.joinParty(inputCode) { isSuccess, message ->
                            isJoining = false
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            if (isSuccess) {
                                showJoinDialog = false
                            }
                        }
                    },
                    enabled = inputCode.isNotBlank() && !isJoining
                ) {
                    Text(if (isJoining) "Đang kết nối..." else "Tham gia")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showJoinDialog = false },
                    enabled = !isJoining
                ) { Text("Hủy") }
            }
        )
    }

    // Dialog xác nhận xóa chuyến đi
    itineraryToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itineraryToDelete = null },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa chuyến đi \"${item.name.ifEmpty { "Chuyến đi này" }}\" khỏi lịch sử không?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteItinerary(item.id) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (success) {
                                viewModel.fetchHistory()
                            }
                            itineraryToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { itineraryToDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}