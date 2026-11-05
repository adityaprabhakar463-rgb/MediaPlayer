package com.celestial.player.scene.model

import com.celestial.player.scene.math.Vector3
import kotlin.random.Random

data class StarParticle3D(
    val position: Vector3,
    val size: Float,
    val brightness: Float,
    val twinklePhase: Float,
    val twinkleSpeed: Float,
)

class Starfield3D(
    val count: Int = 800,
    val sphereRadius: Float = 60f,
    seed: Long = 42L,
) {
    val stars: List<StarParticle3D>

    init {
        val rand = Random(seed)
        stars = List(count) {
            val theta = rand.nextFloat() * 2f * Math.PI.toFloat()
            val phi = Math.acos((2f * rand.nextFloat() - 1f).toDouble()).toFloat()
            val r = sphereRadius * (0.7f + rand.nextFloat() * 0.5f)

            val x = r * kotlin.math.sin(phi) * kotlin.math.cos(theta)
            val y = r * kotlin.math.sin(phi) * kotlin.math.sin(theta)
            val z = r * kotlin.math.cos(phi)

            StarParticle3D(
                position = Vector3(x, y, z),
                size = 1.0f + rand.nextFloat() * 2.2f,
                brightness = 0.35f + rand.nextFloat() * 0.65f,
                twinklePhase = rand.nextFloat() * 2f * Math.PI.toFloat(),
                twinkleSpeed = 0.8f + rand.nextFloat() * 1.6f,
            )
        }
    }
}
