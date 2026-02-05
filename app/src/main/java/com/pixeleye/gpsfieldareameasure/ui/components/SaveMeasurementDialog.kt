package com.pixeleye.gpsfieldareameasure.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pixeleye.gpsfieldareameasure.model.MeasurementUnit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SaveMeasurementDialog(
        area: String,
        perimeter: String,
        unit: MeasurementUnit,
        pointsCount: Int,
        onDismiss: () -> Unit,
        onSave: (name: String) -> Unit
) {
    var name by remember { mutableStateOf("Area ${getCurrentDateTime()}") }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                        text = "Save Measurement",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Measurement Details Card
                    Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                                    CardDefaults.cardColors(
                                            containerColor =
                                                    MaterialTheme.colorScheme.primaryContainer.copy(
                                                            alpha = 0.3f
                                                    )
                                    )
                    ) {
                        Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Area
                            DetailRow(
                                    icon = Icons.Filled.Crop,
                                    label = "Area",
                                    value = area,
                                    emphasized = true
                            )

                            HorizontalDivider()

                            // Perimeter
                            DetailRow(
                                    icon = Icons.Filled.Timeline,
                                    label = "Perimeter",
                                    value = perimeter
                            )

                            // Points
                            DetailRow(
                                    icon = Icons.Filled.Place,
                                    label = "Points",
                                    value = "$pointsCount points"
                            )

                            // Timestamp
                            DetailRow(
                                    icon = Icons.Filled.Schedule,
                                    label = "Time",
                                    value = getCurrentDateTime()
                            )
                        }
                    }

                    // Name input
                    OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name (Optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Filled.Label, contentDescription = null) }
                    )
                }
            },
            confirmButton = {
                Button(
                        onClick = { onSave(name) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save Now")
                }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun DetailRow(
        icon: ImageVector,
        label: String,
        value: String,
        emphasized: Boolean = false
) {
    Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
            )
            Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
                text = value,
                style =
                        if (emphasized) MaterialTheme.typography.titleLarge
                        else MaterialTheme.typography.bodyLarge,
                fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal,
                color =
                        if (emphasized) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun getCurrentDateTime(): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date())
}
