package com.pixeleye.gpsfieldareameasure.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.maps.android.compose.MapType
import com.pixeleye.gpsfieldareameasure.R

@Composable
fun MapTypeDialog(
    currentMapType: MapType,
    onDismiss: () -> Unit,
    onApply: (MapType) -> Unit
) {
    var selectedType by remember { mutableStateOf(currentMapType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Map Type", fontWeight = FontWeight.Bold) },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MapTypeOption(
                    label = "Normal",
                    imageRes = R.drawable.map_normal,
                    type = MapType.NORMAL,
                    selected = selectedType == MapType.NORMAL
                ) {
                    selectedType = MapType.NORMAL
                }
                MapTypeOption(
                    label = "Satellite",
                    imageRes = R.drawable.map_satellite,
                    type = MapType.SATELLITE,
                    selected = selectedType == MapType.SATELLITE
                ) {
                    selectedType = MapType.SATELLITE
                }
                MapTypeOption(
                    label = "Terrain",
                    imageRes = R.drawable.map_terrain,
                    type = MapType.TERRAIN,
                    selected = selectedType == MapType.TERRAIN
                ) {
                    selectedType = MapType.TERRAIN
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(selectedType) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Apply", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.primary)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun RowScope.MapTypeOption(
    label: String,
    imageRes: Int,
    type: MapType,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent

    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = label,
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
