package com.pixeleye.gpsfieldareameasure.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixeleye.gpsfieldareameasure.model.MeasureMode
import com.pixeleye.gpsfieldareameasure.model.MeasurementUnit
import com.pixeleye.gpsfieldareameasure.viewmodel.GpsStatus

@Composable
fun ResultSheet(
        area: String,
        perimeter: String,
        currentMode: MeasureMode,
        currentUnit: MeasurementUnit,
        gpsStatus: GpsStatus = GpsStatus.NONE,
        currentAccuracy: Float? = null,
        onModeChange: (MeasureMode) -> Unit,
        onUnitChange: (MeasurementUnit) -> Unit,
        onSave: () -> Unit,
        modifier: Modifier = Modifier
) {
        Surface(
                modifier = modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
        ) {
                Column(
                        modifier = Modifier.padding(16.dp).animateContentSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                        // 1. Current Area Value (Very Large)
                        Text(
                                text = area,
                                style =
                                        MaterialTheme.typography.displayMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 48.sp
                                        )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 2. Mode Selector
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = currentMode == MeasureMode.MANUAL,
                                onClick = { onModeChange(MeasureMode.MANUAL) },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    inactiveContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    inactiveBorderColor = MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Text("Manual")
                            }
                            SegmentedButton(
                                selected = currentMode == MeasureMode.AUTO,
                                onClick = { onModeChange(MeasureMode.AUTO) },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    inactiveContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    inactiveBorderColor = MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Text("Auto Tracking")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3. Unit Selector
                        Text(
                                text = "Select Unit",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
                        )
                        
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            MeasurementUnit.entries.forEachIndexed { index, unit ->
                                SegmentedButton(
                                    selected = currentUnit == unit,
                                    onClick = { onUnitChange(unit) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = MeasurementUnit.entries.size
                                    ),
                                    colors = SegmentedButtonDefaults.colors(
                                        activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        inactiveContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        inactiveBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                ) {
                                    Text(
                                        text = when(unit) {
                                            MeasurementUnit.SQUARE_METER -> "m²"
                                            MeasurementUnit.SQUARE_KILOMETER -> "km²"
                                            MeasurementUnit.HECTARE -> "ha"
                                            MeasurementUnit.ACRE -> "acres"
                                             MeasurementUnit.SQUARE_FEET -> "ft²"
                                        },
                                        maxLines = 1,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }

                        // Perimeter (Secondary info)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                        text = "Perimeter: ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                        text = perimeter,
                                        style =
                                                MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                )
                                )
                        }

                        // GPS Status info (only if relevant and in auto mode, subtle)
                         if (currentMode == MeasureMode.AUTO) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "GPS Accuracy: ${currentAccuracy?.toInt() ?: "?"}m (${gpsStatus.name})",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (gpsStatus == GpsStatus.EXCELLENT || gpsStatus == GpsStatus.GOOD) 
                                    Color(0xFF4CAF50) else Color(0xFFFF9800)
                            )
                         }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                }
        }
}
