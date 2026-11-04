package com.celestial.player.ui.components.glass

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.celestial.player.ui.theme.CelestialTheme

@Composable
fun GlassSearchField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    hint: String = "Search",
    enabled: Boolean = true,
    leadingIcon: ImageVector = Icons.Filled.Search,
    autoFocus: Boolean = false,
) {
    val colors = CelestialTheme.colors
    val spacing = CelestialTheme.spacing
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(autoFocus) {
        if (autoFocus) focusRequester.requestFocus()
    }

    val fieldSemantics = Modifier.semantics {
        contentDescription = "Search field, $hint active current query ${value.text.ifBlank { "empty" }}"
    }

    CelestialGlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        enabled = enabled,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = spacing.md, vertical = spacing.sm)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = colors.moonlight,
                modifier = Modifier.size(24.dp),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = spacing.md)
                    .then(fieldSemantics),
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    readOnly = !enabled,
                    enabled = enabled,
                    textStyle = TextStyle(
                        color = colors.starlight,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    ),
                    cursorBrush = SolidColor(colors.cosmicViolet),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    decorationBox = { innerTextField ->
                        if (value.text.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.moonlight,
                            )
                        }
                        innerTextField()
                    },
                )
            }
            if (value.text.isNotEmpty()) {
                SearchClearButton(
                    modifier = Modifier.padding(start = spacing.sm),
                    onClear = { onValueChange(TextFieldValue("")) },
                )
            }
        }
    }
}

@Composable
private fun SearchClearButton(
    modifier: Modifier = Modifier,
    onClear: () -> Unit,
) {
    val colors = CelestialTheme.colors
    Box(
        modifier = modifier
            .size(48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClear,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Clear,
            contentDescription = "Clear search",
            tint = colors.starlight,
            modifier = Modifier.size(22.dp),
        )
    }
}
