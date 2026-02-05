package com.pixeleye.gpsfieldareameasure.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun LocationRationaleDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Location Permission") },
        text = {
            Text("This app needs location access to measure areas as you walk and to show your current position on the map.")
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Allow")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    )
}

@Composable
fun SettingsRedirectDialog(
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permission Required") },
        text = {
            Text("Location permission is permanently denied. Please enable it in app settings to use location features.")
        },
        confirmButton = {
            Button(onClick = onGoToSettings) {
                Text("Go to Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
