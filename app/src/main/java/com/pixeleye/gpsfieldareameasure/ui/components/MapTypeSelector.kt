package com.pixeleye.gpsfieldareameasure.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.MapType

data class MapTypeOption(
        val type: MapType,
        val label: String,
        val icon: ImageVector,
        val contentDesc: String
)

private val mapTypeOptions =
        listOf(
                MapTypeOption(
                        type = MapType.NORMAL,
                        label = "Normal",
                        icon = Icons.Filled.Map,
                        contentDesc = "Normal map view"
                ),
                MapTypeOption(
                        type = MapType.SATELLITE,
                        label = "Satellite",
                        icon = Icons.Filled.Satellite,
                        contentDesc = "Satellite imagery view"
                ),
                MapTypeOption(
                        type = MapType.TERRAIN,
                        label = "Terrain",
                        icon = Icons.Filled.Landscape,
                        contentDesc = "Terrain view with topography"
                )
        )

@Composable
fun MapTypeSelector(
        selectedMapType: MapType,
        onMapTypeChange: (MapType) -> Unit,
        modifier: Modifier = Modifier,
        showLabels: Boolean = false
) {
    SingleChoiceSegmentedButtonRow(
            modifier = modifier.semantics { contentDescription = "Map type selector" }
    ) {
        mapTypeOptions.forEachIndexed { index, option ->
            SegmentedButton(
                    selected = selectedMapType == option.type,
                    onClick = { onMapTypeChange(option.type) },
                    shape =
                            SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = mapTypeOptions.size
                            ),
                    icon = {
                        SegmentedButtonDefaults.Icon(active = selectedMapType == option.type) {
                            Icon(
                                    imageVector = option.icon,
                                    contentDescription = option.contentDesc,
                                    modifier = Modifier.padding(2.dp)
                            )
                        }
                    },
                    label = {
                        if (showLabels) {
                            Text(text = option.label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
            )
        }
    }
}
