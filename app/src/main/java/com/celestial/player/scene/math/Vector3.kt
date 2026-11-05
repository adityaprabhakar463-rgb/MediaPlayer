package com.celestial.player.scene.math

import kotlin.math.sqrt

data class Vector3(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
) {
    operator fun plus(v: Vector3) = Vector3(x + v.x, y + v.y, z + v.z)
    operator fun minus(v: Vector3) = Vector3(x - v.x, y - v.y, z - v.z)
    operator fun times(scale: Float) = Vector3(x * scale, y * scale, z * scale)
    operator fun div(scale: Float) = Vector3(x / scale, y / scale, z / scale)
    operator fun unaryMinus() = Vector3(-x, -y, -z)

    fun length(): Float = sqrt(x * x + y * y + z * z)

    fun normalized(): Vector3 {
        val len = length()
        return if (len > 0.00001f) this / len else ZERO
    }

    fun dot(v: Vector3): Float = x * v.x + y * v.y + z * v.z

    fun cross(v: Vector3): Vector3 = Vector3(
        y * v.z - z * v.y,
        z * v.x - x * v.z,
        x * v.y - y * v.x,
    )

    fun distanceTo(v: Vector3): Float = (this - v).length()

    companion object {
        val ZERO = Vector3(0f, 0f, 0f)
        val UP = Vector3(0f, 1f, 0f)
        val FORWARD = Vector3(0f, 0f, -1f)
        val RIGHT = Vector3(1f, 0f, 0f)

        fun lerp(start: Vector3, stop: Vector3, fraction: Float): Vector3 =
            Vector3(
                x = start.x + (stop.x - start.x) * fraction,
                y = start.y + (stop.y - start.y) * fraction,
                z = start.z + (stop.z - start.z) * fraction,
            )
    }
}
