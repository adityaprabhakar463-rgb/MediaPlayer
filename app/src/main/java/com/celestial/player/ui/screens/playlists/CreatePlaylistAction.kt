package com.celestial.player.ui.screens.playlists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.dp
import com.celestial.player.ui.components.glass.CelestialGlassSurface
import com.celestial.player.ui.components.glass.GlassIconButton
import com.celestial.player.ui.theme.CelestialTheme

@Composable
fun CreatePlaylistAction(
    onCreate: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CelestialTheme.colors
    val spacing = CelestialTheme.spacing
    var name by remember { mutableStateOf(TextFieldValue("")) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    CelestialGlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenEdge),
    ) {
        Row(
            modifier = Modifier.padding(spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = null,
                tint = colors.moonlight,
                modifier = Modifier.size(20.dp),
            )
            androidx.compose.foundation.text.BasicTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = colors.starlight,
                ),
                cursorBrush = SolidColor(colors.cosmicViolet),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { if (name.text.isNotBlank()) onCreate(name.text.trim()) },
                ),
                decorationBox = { innerTextField ->
                    if (name.text.isEmpty()) {
                        Text(
                            text = "Playlist name",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.moonlight,
                        )
                    }
                    innerTextField()
                },
            )
            GlassIconButton(
                icon = Icons.Filled.Close,
                contentDescription = "Cancel playlist creation",
                onClick = onDismiss,
            )
            GlassIconButton(
                icon = Icons.Filled.Check,
                contentDescription = "Save playlist",
                enabled = name.text.isNotBlank(),
                onClick = { if (name.text.isNotBlank()) onCreate(name.text.trim()) },
            )
        }
    }
}
