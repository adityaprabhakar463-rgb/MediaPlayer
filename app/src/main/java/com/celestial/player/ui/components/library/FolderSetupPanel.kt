package com.celestial.player.ui.components.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.celestial.player.ui.components.glass.CelestialGlassSurface
import com.celestial.player.ui.theme.CelestialTheme

/**
 * First-run invitation to allocate a music folder. Skippable: the universe remains usable and the
 * folder can be chosen later from Settings.
 */
@Composable
fun MusicFolderPrompt(
    isScanning: Boolean,
    statusText: String,
    onChooseFolder: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CelestialTheme.colors
    val spacing = CelestialTheme.spacing

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.spaceVoid.copy(alpha = 0.62f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
        contentAlignment = Alignment.Center,
    ) {
        CelestialGlassSurface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(horizontal = spacing.lg),
            selected = true,
        ) {
            Column(
                modifier = Modifier.padding(spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    Icon(
                        imageVector = Icons.Filled.FolderOpen,
                        contentDescription = null,
                        tint = colors.auroraBlue,
                    )
                    Text(
                        text = "Choose your music folder",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.starlight,
                    )
                }
                Text(
                    text = "Celestial plays audio from one folder on this device. " +
                        "Subfolders are included and nothing is uploaded.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.moonlight,
                )
                if (isScanning) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    ) {
                        CircularProgressIndicator(
                            color = colors.cosmicViolet,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.moonlight,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(spacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Later", color = colors.moonlight)
                    }
                    Spacer(modifier = Modifier.width(spacing.sm))
                    Button(onClick = onChooseFolder) {
                        Text("Choose folder")
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryScanPill(
    statusText: String,
    modifier: Modifier = Modifier,
) {
    val colors = CelestialTheme.colors
    CelestialGlassSurface(
        modifier = modifier,
        selected = true,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CircularProgressIndicator(
                color = colors.cosmicViolet,
                strokeWidth = 2.dp,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = colors.starlight,
            )
        }
    }
}
