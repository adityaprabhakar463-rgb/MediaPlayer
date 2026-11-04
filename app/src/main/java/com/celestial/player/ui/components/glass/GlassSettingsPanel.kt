package com.celestial.player.ui.components.glass

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.celestial.player.ui.theme.CelestialTheme

@Composable
fun GlassSettingsPanel(
    modifier: Modifier = Modifier,
    title: String = "Settings",
    sections: List<SettingsSection>,
    onDismiss: () -> Unit,
) {
    val colors = CelestialTheme.colors
    val spacing = CelestialTheme.spacing
    CelestialGlassSurface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.lg, vertical = spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.starlight,
                    modifier = Modifier.weight(1f),
                )
                GlassIconButton(
                    icon = Icons.Filled.Close,
                    contentDescription = "Close settings",
                    onClick = onDismiss,
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                sections.forEachIndexed { index, section ->
                    GlassSectionHeader(text = section.title)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.screenEdge),
                        verticalArrangement = Arrangement.spacedBy(spacing.xs),
                    ) {
                        section.entries.forEach { entry ->
                            when (entry) {
                                is SettingsEntryValue.Toggle -> GlassSettingsToggleEntry(entry)
                                is SettingsEntryValue.Info -> GlassSettingsInfoEntry(entry)
                            }
                        }
                    }
                    if (index != sections.lastIndex) {
                        Spacer(modifier = Modifier.padding(top = spacing.md))
                    }
                }
                Spacer(modifier = Modifier.padding(bottom = spacing.lg))
            }
        }
    }
}

sealed interface SettingsEntryValue {
    data class Info(
        val label: String,
        val description: String,
        val trailing: String? = null,
        val icon: ImageVector? = null,
        val enabled: Boolean = false,
    ) : SettingsEntryValue

    data class Toggle(
        val label: String,
        val description: String,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit,
    ) : SettingsEntryValue
}

data class SettingsSection(
    val title: String,
    val entries: List<SettingsEntryValue>,
)

@Composable
private fun GlassSettingsToggleEntry(
    entry: SettingsEntryValue.Toggle,
) {
    val colors = CelestialTheme.colors
    val spacing = CelestialTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Switch,
                onClick = { entry.onCheckedChange(!entry.checked) },
            )
            .padding(horizontal = spacing.sm, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = entry.label,
                style = MaterialTheme.typography.titleMedium,
                color = colors.starlight,
            )
            Text(
                text = entry.description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.moonlight,
            )
        }
        Switch(
            checked = entry.checked,
            onCheckedChange = null,
            colors = settingsSwitchColors(),
            modifier = Modifier.clearAndSetSemantics { },
        )
    }
}

@Composable
private fun GlassSettingsInfoEntry(entry: SettingsEntryValue.Info) {
    val colors = CelestialTheme.colors
    val spacing = CelestialTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (entry.enabled) 1f else 0.55f)
            .padding(horizontal = spacing.sm, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = entry.label,
                style = MaterialTheme.typography.titleMedium,
                color = colors.starlight,
            )
            Text(
                text = entry.description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.moonlight,
            )
        }
        if (entry.trailing != null) {
            Text(
                text = entry.trailing.orEmpty(),
                style = MaterialTheme.typography.labelLarge,
                color = colors.moonlight,
            )
        }
    }
}

@Composable
private fun settingsSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = CelestialTheme.colors.starlight,
    checkedTrackColor = CelestialTheme.colors.cosmicViolet,
    uncheckedThumbColor = CelestialTheme.colors.moonlight,
    uncheckedTrackColor = CelestialTheme.colors.deepSurface,
)
