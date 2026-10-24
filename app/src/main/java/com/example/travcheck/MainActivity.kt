package com.example.travcheck

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.travcheck.model.AiViewModel
import com.example.travcheck.model.AuthViewModel
import com.example.travcheck.model.HistoryViewModel
import com.example.travcheck.model.IncidentViewModel
import com.example.travcheck.model.ManualViewModel
import com.example.travcheck.model.UserViewModel
import com.example.travcheck.model.WeatherViewModel
import com.example.travcheck.CommunityViewModel
import com.example.travcheck.ui.screens.ActivityFeedScreen
import com.example.travcheck.ui.screens.AuthScreen
import com.example.travcheck.ui.screens.CreateItineraryScreen
import com.example.travcheck.ui.screens.HistoryScreen
import com.example.travcheck.ui.screens.MapScreen
import com.example.travcheck.ui.screens.ProfileScreen
import com.example.travcheck.ui.theme.TravCheckTheme

class MainActivity : ComponentActivity() {

    private val userViewModel by viewModels<UserViewModel>()
    private val aiViewModel by viewModels<AiViewModel>()
    private val historyViewModel by viewModels<HistoryViewModel>()
    private val authViewModel by viewModels<AuthViewModel>()
    private val manualViewModel by viewModels<ManualViewModel>()
    private val communityViewModel by viewModels<CommunityViewModel>()
    private val weatherViewModel by viewModels<WeatherViewModel>()
    private val incidentViewModel by viewModels<IncidentViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TravCheckTheme {
                if (authViewModel.currentUser.value == null) {
                    AuthScreen(viewModel = authViewModel)
                } else {
                    val context = LocalContext.current
                    LaunchedEffect(Unit) {
                        val userEmail = authViewModel.currentUser.value?.email ?: "Bạn"
                        val displayName = userEmail.substringBefore("@")
                        Toast.makeText(context, "Chào mừng $displayName đã đến với TravCheck 😊", Toast.LENGTH_LONG).show()
                    }

                    var selectedTab by remember { mutableIntStateOf(0) }

                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                val items = listOf(
                                    Triple(0, Icons.Default.Home, "Cộng đồng"),
                                    Triple(1, Icons.Default.AddCircle, "Tạo chuyến"),
                                    Triple(2, Icons.Default.LocationOn, "Bản đồ"),
                                    Triple(3, Icons.AutoMirrored.Filled.List, "Lịch sử"),
                                    Triple(4, Icons.Default.Person, "Tài khoản")
                                )

                                items.forEach { (index, icon, label) ->
                                    NavigationBarItem(
                                        icon = { Icon(icon, contentDescription = label) },
                                        label = {
                                            Text(
                                                text = label,
                                                maxLines = 1,
                                                softWrap = false,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        selected = selectedTab == index,
                                        onClick = { selectedTab = index },
                                        alwaysShowLabel = true
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (selectedTab) {
                                0 -> ActivityFeedScreen(viewModel = communityViewModel)
                                1 -> CreateItineraryScreen(
                                    aiViewModel = aiViewModel,
                                    manualViewModel = manualViewModel
                                )
                                2 -> MapScreen(
                                    aiViewModel = aiViewModel,
                                    weatherViewModel = weatherViewModel,
                                    incidentViewModel = incidentViewModel,
                                    onBackClick = { selectedTab = 3 }
                                )
                                3 -> HistoryScreen(
                                    viewModel = historyViewModel,
                                    authViewModel = authViewModel,
                                    onItemClick = { item ->
                                        val selectedItinerary = item as Itinerary
                                        aiViewModel.placesList.value = selectedItinerary.places
                                        selectedTab = 2
                                    }
                                )
                                4 -> ProfileScreen(authViewModel = authViewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}