package com.pixeleye.gpsfieldareameasure.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppDrawerContent(
    onItemSelected: (String) -> Unit,
    onClose: () -> Unit
) {
    ModalDrawerSheet {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.BottomStart
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Area Measure",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Professional Toolkit",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        DrawerItem(
            icon = Icons.Default.List,
            label = "Saved list",
            onClick = { onItemSelected("saved_list") }
        )
        DrawerItem(
            icon = Icons.Default.Star,
            label = "Rate app",
            onClick = { onItemSelected("rate") }
        )
        DrawerItem(
            icon = Icons.Default.Share,
            label = "Share app",
            onClick = { onItemSelected("share") }
        )
        DrawerItem(
            icon = Icons.Default.PrivacyTip,
            label = "Privacy policy",
            onClick = { onItemSelected("privacy") }
        )
        DrawerItem(
            icon = Icons.Default.Update,
            label = "Check update",
            onClick = { onItemSelected("update") }
        )
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}
