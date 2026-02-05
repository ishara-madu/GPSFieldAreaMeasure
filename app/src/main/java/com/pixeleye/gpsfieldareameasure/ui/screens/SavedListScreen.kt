package com.pixeleye.gpsfieldareameasure.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pixeleye.gpsfieldareameasure.viewmodel.MainViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Map
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import com.pixeleye.gpsfieldareameasure.utils.FormatUtils
import com.pixeleye.gpsfieldareameasure.model.MeasurementUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedListScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val history by viewModel.history.collectAsState()
    val gson = remember { com.google.gson.Gson() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved list", color = MaterialTheme.colorScheme.onPrimary) },
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
        if (history.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No saved measurements yet.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history.size) { index ->
                    val item = history[index]
                    Card(
                        onClick = {
                            viewModel.loadMeasurement(item)
                            onBack()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Map Preview (Canvas)
                            PolygonPreview(
                                pointsJson = item.pointsJson,
                                gson = gson,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = FormatUtils.formatArea(item.area, MeasurementUnit.valueOf(item.unit)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(onClick = { viewModel.deleteMeasurement(item) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Red.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PolygonPreview(
    pointsJson: String,
    gson: com.google.gson.Gson,
    modifier: Modifier = Modifier
) {
    val points = remember(pointsJson) {
        try {
            val type = object : com.google.gson.reflect.TypeToken<List<com.pixeleye.gpsfieldareameasure.model.Point>>() {}.type
            gson.fromJson<List<com.pixeleye.gpsfieldareameasure.model.Point>>(pointsJson, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    if (points.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Map, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    androidx.compose.foundation.Canvas(modifier = modifier.padding(8.dp)) { // Padding for margin
        val path = androidx.compose.ui.graphics.Path()

        // 1. Find Bounds
        var minLat = Double.MAX_VALUE
        var maxLat = Double.MIN_VALUE
        var minLng = Double.MAX_VALUE
        var maxLng = Double.MIN_VALUE

        points.forEach { p ->
            minLat = minOf(minLat, p.latLng.latitude)
            maxLat = maxOf(maxLat, p.latLng.latitude)
            minLng = minOf(minLng, p.latLng.longitude)
            maxLng = maxOf(maxLng, p.latLng.longitude)
        }

        val latRange = maxLat - minLat
        val lngRange = maxLng - minLng
        
        // Avoid division by zero
        if (latRange == 0.0 || lngRange == 0.0) return@Canvas

        // 2. Scale & Draw
        val width = size.width
        val height = size.height

        // Maintain aspect ratio
        val scaleX = width / lngRange
        val scaleY = height / latRange
        val scale = minOf(scaleX, scaleY).toFloat()

        // Center the shape
        val offsetX = (width - (lngRange * scale)) / 2
        val offsetY = (height - (latRange * scale)) / 2

        points.forEachIndexed { i, p ->
            // Map: Y increases downwards, Latitude increases upwards. So we invert Lat.
            // X = (lng - minLng) * scale
            // Y = (maxLat - lat) * scale (to flip vertically correctly relative to screen)
            
            val x = ((p.latLng.longitude - minLng) * scale + offsetX).toFloat()
            val y = ((maxLat - p.latLng.latitude) * scale + offsetY).toFloat()

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()

        drawPath(
            path = path,
            color = primaryColor, // Match Primary Blue
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
        )
        drawPath(
            path = path,
            color = primaryColor.copy(alpha = 0.2f),
            style = androidx.compose.ui.graphics.drawscope.Fill
        )
    }
}

