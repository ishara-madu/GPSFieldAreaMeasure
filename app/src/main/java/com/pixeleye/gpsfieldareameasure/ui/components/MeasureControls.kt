package com.pixeleye.gpsfieldareameasure.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pixeleye.gpsfieldareameasure.model.MeasureMode

@Composable
fun MeasureControls(
    mode: MeasureMode,
    isTracking: Boolean,
    onUndo: () -> Unit,
    onClear: () -> Unit,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onAddPoint: () -> Unit, // For manual manual add button if needed (though map tap is default)
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Secondary Actions (Small FABs)
        if (!isTracking) {
             SmallFloatingActionButton(
                onClick = onUndo,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(Icons.Default.Undo, contentDescription = "Undo Last Point")
            }
            
            SmallFloatingActionButton(
                onClick = onClear,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Clear All")
            }
        }

        // Primary Action
        if (mode == MeasureMode.AUTO) {
            ExtendedFloatingActionButton(
                onClick = {
                    if (isTracking) onStopTracking() else onStartTracking()
                },
                icon = {
                    Icon(
                        imageVector = if (isTracking) Icons.Default.Pause else Icons.Default.PlayArrow, // Pause or Stop? User said Stop -> finalize.
                        contentDescription = if (isTracking) "Stop Tracking" else "Start Walking"
                    )
                },
                text = { Text(text = if (isTracking) "Stop Measuring" else "Start Measuring") },
                containerColor = if (isTracking) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                contentColor = if (isTracking) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
            )
        } else {
            // Manual Mode - Maybe a "Close Shape" or just simple Add?
            // "Start Measuring" in Manual might just mean nothing, as tap works.
            // Requirement: "Start Measuring" (Manual/Auto).
            // Let's assume Manual works by Tap. So maybe no big FAB needed?
            // User requirement: "Primary: Start Measuring (split into Manual/Auto)".
            // Actually, if we are ALREADY in Manual mode, we are "measuring".
            // So perhaps the FAB is to "Save" or "Done"?
            // Or maybe the FAB is to "Add Point" explicitly (useful for crosshair mode).
            
            // Let's Add a generic "Add Point" FAB for manual mode to help accessibility or center-screen adding.
             ExtendedFloatingActionButton(
                onClick = onAddPoint,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Point") },
                text = { Text("Add Point") },
                 containerColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}
