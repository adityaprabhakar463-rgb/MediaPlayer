package com.celestial.player.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.celestial.player.ui.gallery.DesignFoundationGallery
import com.celestial.player.ui.theme.CelestialTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DesignFoundationScreenshotsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun galleryTop() {
        composeRule.setContent {
            CelestialTheme {
                DesignFoundationGallery()
            }
        }
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/gallery_top.png")
    }

    @Test
    fun galleryBottom() {
        composeRule.setContent {
            CelestialTheme {
                DesignFoundationGallery()
            }
        }
        composeRule.onNodeWithText("Try again").performScrollTo()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/gallery_bottom.png")
    }
}
