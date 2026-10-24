package com.example.travcheck.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travcheck.model.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser

    var displayName by remember { mutableStateOf(currentUser?.displayName ?: "") }
    val email = currentUser?.email ?: "Người dùng ẩn danh"

    // State quản lý Dialog
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ cá nhân", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. THẺ THÔNG TIN CÁ NHÂN (HEADER CARD)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            modifier = Modifier.size(90.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .padding(18.dp)
                                    .fillMaxSize(),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        // Nút chỉnh sửa nhanh Avatar/Tên
                        SmallFloatingActionButton(
                            onClick = { showEditNameDialog = true },
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Sửa tên",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = displayName.ifBlank { email.substringBefore("@") },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Badge danh hiệu phượt thủ
                    SuggestionChip(
                        onClick = { },
                        label = { Text("Phượt thủ TravCheck 🧭", fontSize = 12.sp) },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. BẢNG THỐNG KÊ (STATS CARD)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(number = "5+", label = "Chuyến đi")
                    VerticalDivider(modifier = Modifier.height(40.dp))
                    StatItem(number = "12", label = "Địa điểm")
                    VerticalDivider(modifier = Modifier.height(40.dp))
                    StatItem(number = "100%", label = "Đam mê")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. DANH SÁCH TÙY CHỌN & CÀI ĐẶT
            Text(
                text = "Cài đặt & Tài khoản",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    ProfileOptionItem(
                        icon = Icons.Default.Edit,
                        title = "Đổi tên hiển thị",
                        subtitle = "Cập nhật tên tài khoản của bạn",
                        onClick = { showEditNameDialog = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileOptionItem(
                        icon = Icons.Default.Lock,
                        title = "Gửi email đổi mật khẩu",
                        subtitle = "Nhận link khôi phục qua Email",
                        onClick = { showChangePasswordDialog = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileOptionItem(
                        icon = Icons.Default.Notifications,
                        title = "Thông báo & Cảnh báo SOS",
                        subtitle = "Bật/Tắt cập nhật giao thông real-time",
                        onClick = {
                            Toast.makeText(context, "Đã bật thông báo thời gian thực", Toast.LENGTH_SHORT).show()
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileOptionItem(
                        icon = Icons.Default.Info,
                        title = "Giới thiệu về TravCheck",
                        subtitle = "Phiên bản 2.0 - Hỗ trợ bởi Gemini AI & Firebase",
                        onClick = {
                            Toast.makeText(context, "TravCheck v2.0 - Hỗ trợ bởi Gemini AI & Firebase", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. NÚT ĐĂNG XUẤT
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Đăng xuất")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đăng xuất tài khoản", fontWeight = FontWeight.Bold)
            }
        }
    }

    // --- DIALOG ĐỔI TÊN HIỂN THỊ ---
    if (showEditNameDialog) {
        var newNameInput by remember { mutableStateOf(displayName) }
        var isUpdating by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isUpdating) showEditNameDialog = false },
            title = { Text("Đổi tên hiển thị", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newNameInput,
                    onValueChange = { newNameInput = it },
                    label = { Text("Tên mới") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null && newNameInput.isNotBlank()) {
                            isUpdating = true
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(newNameInput.trim())
                                .build()

                            user.updateProfile(profileUpdates).addOnCompleteListener { task ->
                                isUpdating = false
                                if (task.isSuccessful) {
                                    displayName = newNameInput.trim()
                                    Toast.makeText(context, "Cập nhật tên thành công!", Toast.LENGTH_SHORT).show()
                                    showEditNameDialog = false
                                } else {
                                    Toast.makeText(context, "Lỗi cập nhật: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    enabled = newNameInput.isNotBlank() && !isUpdating
                ) {
                    Text(if (isUpdating) "Đang lưu..." else "Lưu thay đổi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) { Text("Hủy") }
            }
        )
    }

    // --- DIALOG XÁC NHẬN ĐỔI MẬT KHẨU ---
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Khôi phục mật khẩu", fontWeight = FontWeight.Bold) },
            text = { Text("Hệ thống sẽ gửi link đặt lại mật khẩu đến email:\n\n$email\n\nBạn có muốn tiếp tục?") },
            confirmButton = {
                Button(
                    onClick = {
                        if (email.isNotBlank()) {
                            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Đã gửi email khôi phục mật khẩu!", Toast.LENGTH_LONG).show()
                                    showChangePasswordDialog = false
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                ) { Text("Gửi Email") }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) { Text("Hủy") }
            }
        )
    }

    // --- DIALOG XÁC NHẬN ĐĂNG XUẤT ---
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Xác nhận đăng xuất", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn đăng xuất khỏi TravCheck không?") },
            confirmButton = {
                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        authViewModel.currentUser.value = null
                        showLogoutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Đăng xuất") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Hủy") }
            }
        )
    }
}

@Composable
fun StatItem(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = number,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProfileOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Chi tiết",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}