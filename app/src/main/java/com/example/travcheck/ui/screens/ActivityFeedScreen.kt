package com.example.travcheck.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.travcheck.CommunityViewModel
import com.google.firebase.auth.FirebaseAuth
import coil.compose.AsyncImage
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityFeedScreen(viewModel: CommunityViewModel) {
    val itineraries = viewModel.allItineraries
    val isLoading = viewModel.isLoading.value
    val context = LocalContext.current
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cộng đồng TravCheck", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = { viewModel.fetchAllItineraries() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Làm mới")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                itineraries.isEmpty() -> {
                    Text(
                        text = "Chưa có chuyến đi nào trong cộng đồng.",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(itineraries) { itinerary ->
                            val randomImageId = remember(itinerary.id) { Random.nextInt(1, 100) }
                            val isLiked = itinerary.likes.contains(currentUid)

                            FeedItemCard(
                                username = "Thành viên TravCheck",
                                destination = itinerary.name.ifEmpty { "Chuyến đi không tên" },
                                snippet = "Lộ trình ${itinerary.days} ngày đi qua ${itinerary.places.size} địa điểm hấp dẫn.\nMã Party chia sẻ: ${itinerary.inviteCode}",
                                imageUrl = "https://picsum.photos/id/$randomImageId/600/300",
                                likeCount = itinerary.likes.size,
                                isLiked = isLiked,
                                onLikeClick = { viewModel.toggleLike(itinerary.id) },
                                onJoinClick = {
                                    viewModel.joinItinerary(itinerary.id) { isSuccess, message ->
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeedItemCard(
    username: String,
    destination: String,
    snippet: String,
    imageUrl: String,
    likeCount: Int,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onJoinClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Cover Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {}
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = username,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = destination,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = snippet, style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = onLikeClick) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Thích",
                            tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (likeCount > 0) "$likeCount Thích" else "Thích")
                    }
                    TextButton(onClick = onJoinClick) {
                        Icon(Icons.Default.Share, contentDescription = "Tham gia")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tham gia")
                    }
                }
            }
        }
    }
}