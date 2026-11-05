package com.celestial.player.scene.model

import androidx.compose.ui.graphics.Color
import com.celestial.player.scene.math.Vector3

data class CentralStar3D(
    val position: Vector3 = Vector3(0f, 0f, 0f),
    val radius: Float = 1.15f,
    val coreColor: Color = Color(0xFFFFFFFF),
    val innerCoronaColor: Color = Color(0xFFFDE047), // Soft yellow glow
    val outerCoronaColor: Color = Color(0xFF8B5CF6), // Soft violet-blue corona
    val flareColor: Color = Color(0xFFC084FC),
)
