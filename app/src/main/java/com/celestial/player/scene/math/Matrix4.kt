package com.celestial.player.scene.math

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

class Matrix4 private constructor(
    val m: FloatArray = FloatArray(16),
) {
    init {
        require(m.size == 16)
    }

    operator fun times(v: Vector3): Vector3 {
        val x = m[0] * v.x + m[4] * v.y + m[8] * v.z + m[12]
        val y = m[1] * v.x + m[5] * v.y + m[9] * v.z + m[13]
        val z = m[2] * v.x + m[6] * v.y + m[10] * v.z + m[14]
        val w = m[3] * v.x + m[7] * v.y + m[11] * v.z + m[15]
        return if (w != 0f && w != 1f) Vector3(x / w, y / w, z / w) else Vector3(x, y, z)
    }

    operator fun times(other: Matrix4): Matrix4 {
        val res = FloatArray(16)
        for (i in 0..3) {
            for (j in 0..3) {
                var sum = 0f
                for (k in 0..3) {
                    sum += this.m[i + k * 4] * other.m[k + j * 4]
                }
                res[i + j * 4] = sum
            }
        }
        return Matrix4(res)
    }

    fun project(point: Vector3, viewportWidth: Float, viewportHeight: Float): ProjectedPoint {
        val x = m[0] * point.x + m[4] * point.y + m[8] * point.z + m[12]
        val y = m[1] * point.x + m[5] * point.y + m[9] * point.z + m[13]
        val z = m[2] * point.x + m[6] * point.y + m[10] * point.z + m[14]
        val w = m[3] * point.x + m[7] * point.y + m[11] * point.z + m[15]

        if (w <= 0.001f) {
            return ProjectedPoint(0f, 0f, -1f, isBehind = true, w = w)
        }

        val ndcX = x / w
        val ndcY = y / w
        val ndcZ = z / w

        val screenX = (ndcX + 1f) * 0.5f * viewportWidth
        val screenY = (1f - ndcY) * 0.5f * viewportHeight

        return ProjectedPoint(
            x = screenX,
            y = screenY,
            depth = ndcZ,
            isBehind = false,
            w = w,
        )
    }

    companion object {
        fun identity(): Matrix4 {
            val m = FloatArray(16)
            m[0] = 1f; m[5] = 1f; m[10] = 1f; m[15] = 1f
            return Matrix4(m)
        }

        fun perspective(fovYDeg: Float, aspect: Float, near: Float, far: Float): Matrix4 {
            val fovRad = Math.toRadians(fovYDeg.toDouble()).toFloat()
            val f = 1f / tan(fovRad / 2f)
            val rangeInv = 1f / (near - far)

            val m = FloatArray(16)
            m[0] = f / aspect
            m[5] = f
            m[10] = (far + near) * rangeInv
            m[11] = -1f
            m[14] = (2f * far * near) * rangeInv
            return Matrix4(m)
        }

        fun lookAt(eye: Vector3, center: Vector3, up: Vector3): Matrix4 {
            val f = (center - eye).normalized()
            val s = f.cross(up).normalized()
            val u = s.cross(f)

            val m = FloatArray(16)
            m[0] = s.x; m[4] = s.y; m[8] = s.z; m[12] = -s.dot(eye)
            m[1] = u.x; m[5] = u.y; m[9] = u.z; m[13] = -u.dot(eye)
            m[2] = -f.x; m[6] = -f.y; m[10] = -f.z; m[14] = f.dot(eye)
            m[3] = 0f; m[7] = 0f; m[11] = 0f; m[15] = 1f
            return Matrix4(m)
        }

        fun rotationY(angleRad: Float): Matrix4 {
            val c = cos(angleRad)
            val s = sin(angleRad)
            val m = FloatArray(16)
            m[0] = c; m[8] = s
            m[5] = 1f
            m[2] = -s; m[10] = c
            m[15] = 1f
            return Matrix4(m)
        }

        fun rotationX(angleRad: Float): Matrix4 {
            val c = cos(angleRad)
            val s = sin(angleRad)
            val m = FloatArray(16)
            m[0] = 1f
            m[5] = c; m[9] = -s
            m[6] = s; m[10] = c
            m[15] = 1f
            return Matrix4(m)
        }
    }
}

data class ProjectedPoint(
    val x: Float,
    val y: Float,
    val depth: Float,
    val isBehind: Boolean,
    val w: Float,
)
