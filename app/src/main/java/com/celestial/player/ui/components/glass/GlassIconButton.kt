package com.celestial.player.ui.components.glass

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.celestial.player.ui.theme.CelestialTheme

@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    stateDescription: String? = null,
    tint: Color = Color.Unspecified,
    shape: Shape = CircleShape,
) {
    val colors = CelestialTheme.colors
    val reducedMotion = CelestialTheme.effects.reducedMotion
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            !enabled || reducedMotion -> 1f
            pressed -> 0.94f
            else -> 1f
        },
        animationSpec = if (reducedMotion) snap() else spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "glassIconButtonScale",
    )

    val fillAlpha = when {
        pressed -> 0.16f
        selected -> 0.18f
        else -> 0.06f
    }
    val borderColor = when {
        selected -> colors.cosmicViolet.copy(alpha = 0.55f)
        else -> colors.glassBorder.copy(alpha = 0.10f)
    }
    val contentTint = when {
        !enabled -> colors.moonlight.copy(alpha = 0.5f)
        tint != Color.Unspecified -> tint
        selected -> colors.cosmicViolet
        else -> colors.starlight
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.38f
            }
            .clip(shape)
            .background(
                if (selected) colors.cosmicViolet.copy(alpha = fillAlpha)
                else Color.White.copy(alpha = fillAlpha),
            )
            .border(width = 1.dp, color = borderColor, shape = shape)
            .semantics {
                this.selected = selected
                if (!enabled) disabled()
                stateDescription?.let { this.stateDescription = it }
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentTint,
        )
    }
}
