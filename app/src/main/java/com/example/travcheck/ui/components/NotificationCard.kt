package com.example.travcheck.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Định nghĩa Enum cho các loại thông báo để dễ quản lý màu sắc
enum class NotificationType {
    ALERT, CHECKPOINT, ROSTER, INFO
}

@Composable
fun NotificationCard(
    type: NotificationType,
    tag: String,
    time: String,
    content: String,
    actionText: String,
    actionIcon: ImageVector,
    isUnread: Boolean = false,
    onClick: () -> Unit = {}
) {
    // Xác định màu sắc dựa vào loại thông báo
    val (iconBgColor, iconColor, tagBgColor, tagColor) = when (type) {
        NotificationType.ALERT -> listOf(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
        NotificationType.CHECKPOINT -> listOf(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.secondary)
        NotificationType.ROSTER -> listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.primary)
        else -> listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    val cardBgColor = if (isUnread) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnread) 4.dp else 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            // Đã sửa lỗi: Dùng verticalAlignment của Compose thay vì cú pháp Flutter
            verticalAlignment = Alignment.Top
        ) {
            // Icon bên trái
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                // Tạm dùng Text để giữ chỗ cho Icon (Bạn có thể thay bằng Icon thực tế)
                Text(
                    text = tag.take(1),
                    color = iconColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Nội dung bên phải
            Column(modifier = Modifier.weight(1f)) {
                // Dòng trên cùng: Tag và Thời gian
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tag label
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(tagBgColor)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tag.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = tagColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Thời gian và dấu chấm đỏ (nếu chưa đọc)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = time,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isUnread) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Nội dung chính
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Nút hành động (Action Button)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onClick() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = actionIcon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = actionText,
                            style = MaterialTheme.typography.labelMedium,
                            color = iconColor
                        )
                    }
                }
            }
        }
    }
}