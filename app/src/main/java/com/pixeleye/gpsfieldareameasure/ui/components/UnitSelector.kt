package com.pixeleye.gpsfieldareameasure.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pixeleye.gpsfieldareameasure.model.MeasurementUnit

@Composable
fun UnitSelector(
    currentUnit: MeasurementUnit,
    onUnitSelected: (MeasurementUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    // A simplified horizontal scroll or just the most common ones.
    // For now, let's just show a few common ones or use a dropdown in TopBar.
    // If this is in the top bar, it should be compact.
    
    // Changing design decision: Let's make it a Chip Group that is horizontal.
    // But for size, maybe just toggle or a single button that opens a sheet?
    // Let's do a simple chip row for now, assuming horizontal scrolling if needed,
    // or just the main ones.
    
    Row(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Show only current and maybe cycle on tap? Or a dropdown.
        // Let's try FilterChip style.
        
        MeasurementUnit.values().forEach { unit ->
            val selected = unit == currentUnit
            FilterChip(
                selected = selected,
                onClick = { onUnitSelected(unit) },
                label = { Text(unit.shortName) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}
