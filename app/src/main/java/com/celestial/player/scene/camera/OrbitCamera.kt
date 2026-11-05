package com.celestial.player.scene.camera

import com.celestial.player.scene.math.Matrix4
import com.celestial.player.scene.math.Ray
import com.celestial.player.scene.math.Vector3
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

class OrbitCamera(
    var yawDeg: Float = 25f,
    var pitchDeg: Float = 22f,
    var distance: Float = 14f,
    var target: Vector3 = Vector3.ZERO,
    var fovYDeg: Float = 45f,
    var near: Float = 0.1f,
    var far: Float = 120f,
) {
    var velocityYaw: Float = 0f
    var velocityPitch: Float = 0f

    fun calculateEye(): Vector3 {
        val yawRad = Math.toRadians(yawDeg.toDouble()).toFloat()
        val pitchRad = Math.toRadians(pitchDeg.coerceIn(-85f, 85f).toDouble()).toFloat()

        val x = target.x + distance * cos(pitchRad) * sin(yawRad)
        val y = target.y + distance * sin(pitchRad)
        val z = target.z + distance * cos(pitchRad) * cos(yawRad)

        return Vector3(x, y, z)
    }

    fun getViewMatrix(): Matrix4 {
        val eye = calculateEye()
        return Matrix4.lookAt(eye = eye, center = target, up = Vector3.UP)
    }

    fun getProjectionMatrix(aspect: Float): Matrix4 {
        return Matrix4.perspective(fovYDeg = fovYDeg, aspect = aspect, near = near, far = far)
    }

    fun updateInertia(damping: Float = 0.92f) {
        if (kotlin.math.abs(velocityYaw) > 0.01f || kotlin.math.abs(velocityPitch) > 0.01f) {
            yawDeg += velocityYaw
            pitchDeg = (pitchDeg + velocityPitch).coerceIn(-85f, 85f)
            velocityYaw *= damping
            velocityPitch *= damping
        } else {
            velocityYaw = 0f
            velocityPitch = 0f
        }
    }

    fun unprojectRay(
        touchX: Float,
        touchY: Float,
        viewportWidth: Float,
        viewportHeight: Float,
    ): Ray {
        val eye = calculateEye()
        val ndcX = (2f * touchX / viewportWidth) - 1f
        val ndcY = 1f - (2f * touchY / viewportHeight)

        val aspect = viewportWidth / viewportHeight
        val fovRad = Math.toRadians(fovYDeg.toDouble()).toFloat()
        val tanFov = tan(fovRad / 2f)

        val forward = (target - eye).normalized()
        val right = forward.cross(Vector3.UP).normalized()
        val up = right.cross(forward).normalized()

        val rayDir = (forward + right * (ndcX * aspect * tanFov) + up * (ndcY * tanFov)).normalized()

        return Ray(origin = eye, direction = rayDir)
    }
}
