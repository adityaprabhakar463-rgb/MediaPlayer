package com.celestial.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.celestial.player.data.LibraryStateHolder
import com.celestial.player.data.SampleLibrary
import com.celestial.player.ui.CelestialAppShell
import com.celestial.player.ui.theme.CelestialEffects
import com.celestial.player.ui.theme.CelestialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            var reducedEffects by remember { mutableStateOf(false) }
            CelestialTheme(
                effects = CelestialEffects(
                    reducedMotion = reducedEffects,
                    ambientMotion = !reducedEffects,
                    glowIntensity = if (reducedEffects) 0.5f else 1f,
                ),
            ) {
                val library = remember { LibraryStateHolder(SampleLibrary) }
                CelestialAppShell(
                    library = library,
                    reducedEffects = reducedEffects,
                    onReducedEffectsChange = { reducedEffects = it },
                )
            }
        }
    }
}
