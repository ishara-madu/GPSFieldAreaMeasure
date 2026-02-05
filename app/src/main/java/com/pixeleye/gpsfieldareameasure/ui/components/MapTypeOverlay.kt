package com.pixeleye.gpsfieldareameasure.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.MapType

@Composable
fun MapTypeOverlay(
        selectedMapType: MapType,
        onMapTypeChange: (MapType) -> Unit,
        modifier: Modifier = Modifier
) {
    Row(
            modifier =
                    modifier.background(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(12.dp)
                            )
                            .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        MapTypeButton(
                icon = Icons.Filled.Map,
                label = "Normal",
                selected = selectedMapType == MapType.NORMAL,
                onClick = { onMapTypeChange(MapType.NORMAL) }
        )
        MapTypeButton(
                icon = Icons.Filled.Satellite,
                label = "Satellite",
                selected = selectedMapType == MapType.SATELLITE,
                onClick = { onMapTypeChange(MapType.SATELLITE) }
        )
        MapTypeButton(
                icon = Icons.Filled.Landscape,
                label = "Terrain",
                selected = selectedMapType == MapType.TERRAIN,
                onClick = { onMapTypeChange(MapType.TERRAIN) }
        )
    }
}

@Composable
private fun MapTypeButton(
        icon: ImageVector,
        label: String,
        selected: Boolean,
        onClick: () -> Unit
) {
    IconButton(
            onClick = onClick,
            modifier =
                    Modifier.size(48.dp)
                            .background(
                                    color =
                                            if (selected) MaterialTheme.colorScheme.primaryContainer
                                            else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                            )
    ) {
        Icon(
                imageVector = icon,
                contentDescription = label,
                tint =
                        if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface
        )
    }
}
