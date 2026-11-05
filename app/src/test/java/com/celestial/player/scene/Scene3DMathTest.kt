package com.celestial.player.scene

import com.celestial.player.scene.camera.OrbitCamera
import com.celestial.player.scene.math.Ray
import com.celestial.player.scene.math.Vector3
import com.celestial.player.scene.model.Planet3D
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Scene3DMathTest {

    @Test
    fun testVector3Operations() {
        val v1 = Vector3(1f, 2f, 3f)
        val v2 = Vector3(4f, 5f, 6f)

        val sum = v1 + v2
        assertEquals(Vector3(5f, 7f, 9f), sum)

        val diff = v2 - v1
        assertEquals(Vector3(3f, 3f, 3f), diff)

        val dot = v1.dot(v2)
        assertEquals(32f, dot, 0.001f)

        val norm = Vector3(3f, 0f, 0f).normalized()
        assertEquals(1f, norm.x, 0.001f)
        assertEquals(0f, norm.y, 0.001f)
    }

    @Test
    fun testRaySphereIntersection() {
        val ray = Ray(
            origin = Vector3(0f, 0f, 10f),
            direction = Vector3(0f, 0f, -1f),
        )
        val sphereCenter = Vector3(0f, 0f, 0f)
        val hitDistance = ray.intersectsSphere(sphereCenter, radius = 2f)

        assertNotNull(hitDistance)
        assertEquals(8f, hitDistance!!, 0.01f)

        val missRay = Ray(
            origin = Vector3(10f, 10f, 10f),
            direction = Vector3(0f, 0f, -1f),
        )
        assertNull(missRay.intersectsSphere(sphereCenter, radius = 2f))
    }

    @Test
    fun testOrbitCameraEyePosition() {
        val camera = OrbitCamera(yawDeg = 0f, pitchDeg = 0f, distance = 10f, target = Vector3.ZERO)
        val eye = camera.calculateEye()

        assertEquals(0f, eye.x, 0.01f)
        assertEquals(0f, eye.y, 0.01f)
        assertEquals(10f, eye.z, 0.01f)
    }

    @Test
    fun testPlanetPositionCalculation() {
        val planet = Planet3D.defaultPlanets().first()
        val pos0 = planet.calculatePosition(0f)
        val pos1 = planet.calculatePosition(10f)

        assertTrue(pos0.length() > 0f)
        assertTrue(pos1.length() > 0f)
    }
}
