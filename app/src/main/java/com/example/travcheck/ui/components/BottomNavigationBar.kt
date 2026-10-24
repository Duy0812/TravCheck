package com.example.travcheck.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val badgeText: String? = null,
    val badgeColor: androidx.compose.ui.graphics.Color? = null,
    val onBadgeColor: androidx.compose.ui.graphics.Color? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar() {
    var selectedItem by remember { mutableStateOf(3) } // Mặc định đang ở tab Alerts (index 3)

    // Khai báo danh sách các tab kèm dữ liệu Badge (huy hiệu)
    val items = listOf(
        BottomNavItem("Map", Icons.Default.Explore),
        BottomNavItem("Trip", Icons.Default.Tour, "65%", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer),
        BottomNavItem("Members", Icons.Default.Group, "18", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer),
        BottomNavItem("Alerts", Icons.Default.Notifications, "3", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer),
        BottomNavItem("Profile", Icons.Default.AccountCircle)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    if (item.badgeText != null && item.badgeColor != null && item.onBadgeColor != null) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = item.badgeColor,
                                    contentColor = item.onBadgeColor,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Text(item.badgeText, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                            }
                        ) {
                            Icon(item.icon, contentDescription = item.title, modifier = Modifier.size(24.dp))
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.title, modifier = Modifier.size(24.dp))
                    }
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selectedItem == index) FontWeight.Bold else FontWeight.Medium
                    )
                },
                selected = selectedItem == index,
                onClick = { selectedItem = index },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}