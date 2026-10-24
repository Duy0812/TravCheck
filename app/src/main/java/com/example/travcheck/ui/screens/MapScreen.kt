package com.example.travcheck.ui.screens

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.travcheck.model.AiViewModel
import com.example.travcheck.model.IncidentViewModel
import com.example.travcheck.model.WeatherViewModel
import com.example.travcheck.Place
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MapScreen(
    aiViewModel: AiViewModel,
    weatherViewModel: WeatherViewModel,
    incidentViewModel: IncidentViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val places = aiViewModel.placesList.value
    val incidents = incidentViewModel.incidents

    val placesJson = Gson().toJson(places)
    val incidentsJson = Gson().toJson(incidents)

    // State kết hợp để bản đồ reload khi có địa điểm hoặc sự cố mới
    val mapDataHash = "$placesJson-$incidentsJson"
    var lastLoadedHash by remember { mutableStateOf("") }

    // State cho Dialog báo cáo sự cố
    var showSosDialog by remember { mutableStateOf(false) }
    var selectedPlace by remember { mutableStateOf<Place?>(null) }
    var incidentType by remember { mutableStateOf("Kẹt xe") }
    var incidentDesc by remember { mutableStateOf("") }
    val incidentOptions = listOf("Kẹt xe", "Tai nạn", "Ngập lụt", "Sạt lở", "Đường hỏng")

    LaunchedEffect(places) {
        if (places.isNotEmpty() && places[0].lat != 0.0) {
            weatherViewModel.fetchWeather(places[0].lat, places[0].lng)
            selectedPlace = places[0]
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bản đồ & Cảnh báo", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSosDialog = true },
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ) {
                Icon(Icons.Default.Warning, contentDescription = "Báo sự cố")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            // NỬA TRÊN: Bản đồ Leaflet
            Box(modifier = Modifier.fillMaxWidth().weight(0.5f)) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webViewClient = WebViewClient()
                        }
                    },
                    update = { webView ->
                        if (mapDataHash != lastLoadedHash) {
                            lastLoadedHash = mapDataHash
                            val htmlContent = """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                    <meta charset="utf-8" />
                                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                                    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                                    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                                    <style>body { padding: 0; margin: 0; } html, body, #map { height: 100%; width: 100%; }</style>
                                </head>
                                <body>
                                    <div id="map"></div>
                                    <script>
                                        var map = L.map('map').setView([10.762622, 106.660172], 6);
                                        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19 }).addTo(map);
                                        
                                        var places = $placesJson;
                                        var incidents = $incidentsJson;
                                        var latlngs = [];

                                        // Vẽ lộ trình
                                        if (places && places.length > 0) {
                                            places.forEach(function(place, index) {
                                                if (place.lat !== 0.0 && place.lng !== 0.0) {
                                                    latlngs.push([place.lat, place.lng]);
                                                    L.marker([place.lat, place.lng]).addTo(map).bindPopup("<b>" + (index + 1) + ". " + place.name + "</b>");
                                                }
                                            });
                                            if (latlngs.length > 1) {
                                                var polyline = L.polyline(latlngs, {color: '#2196F3', weight: 5}).addTo(map);
                                                map.fitBounds(polyline.getBounds(), {padding: [50, 50]});
                                            } else if (latlngs.length === 1) {
                                                map.setView(latlngs[0], 14);
                                            }
                                        }

                                        // Vẽ các cảnh báo sự cố (Icon Đỏ)
                                        var warningIcon = L.icon({
                                            iconUrl: 'https://cdn-icons-png.flaticon.com/512/564/564246.png',
                                            iconSize: [30, 30],
                                            iconAnchor: [15, 30],
                                            popupAnchor: [0, -30]
                                        });
                                        
                                        if (incidents && incidents.length > 0) {
                                            incidents.forEach(function(inc) {
                                                if (inc.lat !== 0.0 && inc.lng !== 0.0) {
                                                    L.marker([inc.lat, inc.lng], {icon: warningIcon})
                                                     .addTo(map)
                                                     .bindPopup("<b style='color:red;'>⚠️ " + inc.type + "</b><br/>" + inc.description);
                                                }
                                            });
                                        }
                                    </script>
                                </body>
                                </html>
                            """.trimIndent()
                            webView.loadDataWithBaseURL("https://localhost", htmlContent, "text/html", "UTF-8", null)
                        }
                    }
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

            // NỬA DƯỚI: Thời tiết & Lịch trình
            Column(modifier = Modifier.fillMaxWidth().weight(0.5f).background(MaterialTheme.colorScheme.surface)) {
                if (places.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Thời tiết tại điểm xuất phát:", style = MaterialTheme.typography.bodySmall)
                                Text(places[0].name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            }
                            if (weatherViewModel.isLoading.value) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else if (weatherViewModel.temperature.value.isNotEmpty()) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(weatherViewModel.temperature.value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                                    Text(weatherViewModel.weatherDescription.value)
                                }
                            }
                        }
                    }
                }

                Text("Lịch trình chi tiết (${places.size} điểm đến)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(places) { index, place ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)) {
                                Text("${index + 1}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(place.name.ifEmpty { "Địa điểm chưa xác định" }, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }

    // Dialog Báo cáo sự cố (Sử dụng GPS thiết bị)
    if (showSosDialog) {
        AlertDialog(
            onDismissRequest = { showSosDialog = false },
            title = { Text("Phát tín hiệu SOS", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Vị trí xảy ra sự cố:")
                    Spacer(modifier = Modifier.height(4.dp))

                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = selectedPlace?.name ?: "Vị trí GPS hiện tại của tôi",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            places.forEach { place ->
                                DropdownMenuItem(
                                    text = { Text(place.name) },
                                    onClick = { selectedPlace = place; expanded = false }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Loại sự cố:")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        incidentOptions.take(3).forEach { type ->
                            FilterChip(
                                selected = incidentType == type,
                                onClick = { incidentType = type },
                                label = { Text(type) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = incidentDesc,
                        onValueChange = { incidentDesc = it },
                        label = { Text("Mô tả chi tiết (tùy chọn)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        incidentViewModel.reportIncidentWithGps(
                            context = context,
                            type = incidentType,
                            description = incidentDesc,
                            manualLat = selectedPlace?.lat,
                            manualLng = selectedPlace?.lng
                        ) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (success) showSosDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (incidentViewModel.isReporting.value) "Đang gửi..." else "Báo cáo ngay")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSosDialog = false }) { Text("Hủy") }
            }
        )
    }
}