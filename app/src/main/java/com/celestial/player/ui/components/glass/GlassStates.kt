package com.celestial.player.ui.components.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.celestial.player.ui.theme.CelestialTheme

@Composable
fun GlassEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = CelestialTheme.colors
    val spacing = CelestialTheme.spacing
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.xxl, vertical = spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(colors.deepSurface.copy(alpha = 0.62f))
                    .border(1.dp, colors.glassBorder.copy(alpha = 0.10f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.moonlight,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.starlight,
            textAlign = TextAlign.Center,
        )
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.moonlight,
                textAlign = TextAlign.Center,
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(spacing.xs))
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(colors.cosmicViolet.copy(alpha = 0.18f))
                    .border(1.dp, colors.cosmicViolet.copy(alpha = 0.55f), MaterialTheme.shapes.small)
                    .clickable(role = Role.Button, onClick = onAction)
                    .padding(horizontal = spacing.lg, vertical = spacing.sm),
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.starlight,
                )
            }
        }
    }
}

@Composable
fun GlassLoadingState(
    modifier: Modifier = Modifier,
    label: String = "Loading",
) {
    val colors = CelestialTheme.colors
    val spacing = CelestialTheme.spacing
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        CircularProgressIndicator(
            color = colors.cosmicViolet,
            strokeWidth = 2.dp,
            trackColor = colors.glassBorder.copy(alpha = 0.08f),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.moonlight,
        )
    }
}

@Composable
fun GlassErrorState(
    message: String,
    modifier: Modifier = Modifier,
    title: String = "Something went wrong",
    retryLabel: String = "Try again",
    onRetry: () -> Unit,
) {
    val colors = CelestialTheme.colors
    val spacing = CelestialTheme.spacing
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.xxl, vertical = spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.starlight,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = colors.moonlight,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(spacing.xs))
        Box(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .background(colors.cosmicViolet.copy(alpha = 0.18f))
                .border(1.dp, colors.cosmicViolet.copy(alpha = 0.55f), MaterialTheme.shapes.small)
                .clickable(role = Role.Button, onClick = onRetry)
                .padding(horizontal = spacing.lg, vertical = spacing.sm),
        ) {
            Text(
                text = retryLabel,
                style = MaterialTheme.typography.labelLarge,
                color = colors.starlight,
            )
        }
    }
}
