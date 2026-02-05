package com.pixeleye.gpsfieldareameasure.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowToUseScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("How to Use", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Welcome to Area Measure!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            HelpSection(
                title = "1. Manual Mode",
                description = "Tap on the map to add points. The polygon will form as you add at least 3 points. You can drag points to adjust them."
            )

            HelpSection(
                title = "2. Auto Mode",
                description = "Switch to Auto mode and click 'Start Tracking'. Walk around the perimeter of the area. points will be added automatically based on your location."
            )

            HelpSection(
                title = "3. Units",
                description = "Swipe up the bottom sheet to change units between m², km², Hectares, and Acres."
            )

            HelpSection(
                title = "4. Saving",
                description = "Once you have a valid polygon, the Save button will appear. Provide a name and save it to your history."
            )
        }
    }
}

@Composable
private fun HelpSection(title: String, description: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(description, style = MaterialTheme.typography.bodyMedium)
    }
}
