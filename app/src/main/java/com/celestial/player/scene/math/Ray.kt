package com.celestial.player.scene.math

data class Ray(
    val origin: Vector3,
    val direction: Vector3,
) {
    fun intersectsSphere(center: Vector3, radius: Float): Float? {
        val oc = origin - center
        val a = direction.dot(direction)
        val b = 2.0f * oc.dot(direction)
        val c = oc.dot(oc) - radius * radius
        val discriminant = b * b - 4 * a * c
        if (discriminant < 0) return null

        val t = (-b - kotlin.math.sqrt(discriminant)) / (2.0f * a)
        return if (t >= 0) t else null
    }
}
