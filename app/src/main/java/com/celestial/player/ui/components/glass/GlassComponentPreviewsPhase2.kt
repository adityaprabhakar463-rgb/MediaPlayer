package com.celestial.player.ui.components.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.TextFieldValue
import com.celestial.player.playback.PlayerUiState
import com.celestial.player.ui.components.player.MiniPlayerPlanet
import com.celestial.player.ui.theme.CelestialTheme

@Composable
private fun Phase2PreviewStage(content: @Composable () -> Unit) {
    CelestialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF000000)),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Preview(name = "GlassArtwork", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun GlassArtworkPreview() {
    Phase2PreviewStage {
        GlassArtwork(size = 56.dp, colorSeed = 3)
    }
}

@Preview(name = "GlassArtwork initials", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun GlassArtworkInitialsPreview() {
    Phase2PreviewStage {
        GlassArtwork(size = 56.dp, colorSeed = 2, icon = null, initials = "LX")
    }
}

@Preview(name = "GlassSearchField empty", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun GlassSearchFieldPreview() {
    Phase2PreviewStage {
        GlassSearchField(
            value = TextFieldValue(""),
            onValueChange = {},
        )
    }
}

@Preview(name = "GlassSettingsPanel", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun GlassSettingsPanelPreview() {
    Phase2PreviewStage {
        GlassSettingsPanel(
            modifier = Modifier.fillMaxWidth(),
            sections = listOf(
                SettingsSection(
                    title = "Universe",
                    entries = listOf(
                        SettingsEntryValue.Toggle(
                            label = "Reduced effects",
                            description = "Calm down the scene",
                            checked = true,
                            onCheckedChange = {},
                        ),
                    ),
                ),
                SettingsSection(
                    title = "About",
                    entries = listOf(
                        SettingsEntryValue.Info(
                            label = "Celestial",
                            description = "Version 0.1.0",
                            trailing = "",
                            enabled = true,
                        ),
                    ),
                ),
            ),
            onDismiss = {},
        )
    }
}

@Preview(name = "MiniPlayerPlanet", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun MiniPlayerPlanetPreview() {
    Phase2PreviewStage {
        MiniPlayerPlanet(
            state = PlayerUiState(
                positionMs = 153_000L,
                durationMs = 240_000L,
            ),
            albumColorSeed = 1,
        )
    }
}
