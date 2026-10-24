package com.example.travcheck.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.travcheck.model.AiViewModel
import com.example.travcheck.model.ManualViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateItineraryScreen(
    aiViewModel: AiViewModel,
    manualViewModel: ManualViewModel
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf("Trợ lý AI 🤖", "Tạo thủ công ✍️")

    Column(modifier = Modifier.fillMaxSize()) {
        // Sub-tab chuyển đổi giữa AI và Tạo thủ công
        PrimaryTabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            subTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedSubTab) {
                0 -> AiScreen(aiViewModel = aiViewModel)
                1 -> ManualItineraryScreen(viewModel = manualViewModel)
            }
        }
    }
}