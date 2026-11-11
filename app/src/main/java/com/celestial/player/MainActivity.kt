package com.celestial.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.celestial.player.ui.CelestialAppShell
import com.celestial.player.ui.theme.CelestialEffects
import com.celestial.player.ui.theme.CelestialTheme

class MainActivity : ComponentActivity() {

    private val appViewModel: CelestialAppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val library = appViewModel.library
        val player = appViewModel.player
        val nav = appViewModel.nav
        setContent {
            var reducedEffects by rememberSaveable { mutableStateOf(false) }

            CelestialTheme(
                effects = CelestialEffects(
                    reducedMotion = reducedEffects,
                    ambientMotion = !reducedEffects,
                    glowIntensity = if (reducedEffects) 0.5f else 1f,
                ),
            ) {
                CelestialAppShell(
                    library = library,
                    playerController = player,
                    navState = nav,
                    reducedEffects = reducedEffects,
                    onReducedEffectsChange = { reducedEffects = it },
                )
            }
        }
    }
}
