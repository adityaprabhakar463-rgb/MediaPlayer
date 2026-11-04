package com.celestial.player.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.celestial.player.ui.theme.CelestialTheme

@Composable
fun SearchCategoryTabs(
    selected: SearchCategory,
    onSelected: (SearchCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CelestialTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CelestialTheme.spacing.screenEdge),
        horizontalArrangement = Arrangement.spacedBy(CelestialTheme.spacing.sm),
    ) {
        SearchCategory.values().forEach { category ->
            FilterChip(
                selected = category == selected,
                onClick = { onSelected(category) },
                colors = searchChipColors(),
                label = {
                    Text(
                        text = category.label,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
            )
        }
    }
}

@Composable
private fun searchChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = CelestialTheme.colors.deepSurface.copy(alpha = 0.6f),
    labelColor = CelestialTheme.colors.moonlight,
    selectedContainerColor = CelestialTheme.colors.cosmicViolet.copy(alpha = 0.28f),
    selectedLabelColor = CelestialTheme.colors.starlight,
)
