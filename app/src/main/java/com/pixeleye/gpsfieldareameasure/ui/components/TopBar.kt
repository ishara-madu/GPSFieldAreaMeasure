package com.pixeleye.gpsfieldareameasure.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(onMenuClick: () -> Unit) {
        CenterAlignedTopAppBar(
                title = {
                        Text(
                                text = "AreaWalk",
                                style =
                                        MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold
                                        )
                        )
                },
                navigationIcon = {
                        IconButton(onClick = onMenuClick) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                },
                actions = {},
                colors =
                        TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor =
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                titleContentColor = MaterialTheme.colorScheme.onSurface
                        ),
                modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp)
        )
}
