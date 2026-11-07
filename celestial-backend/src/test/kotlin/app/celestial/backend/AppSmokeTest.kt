package app.celestial.backend

import app.celestial.backend.config.AppConfig
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppSmokeTest {
    @Test
    fun `health and version respond without db`() = testApplication {
        application { module(configOverride = AppConfig(), skipDb = true) }
        val health = client.get("/health")
        assertEquals(HttpStatusCode.OK, health.status)
        assertTrue(health.bodyAsText().contains("\"ok\""))
        val version = client.get("/version")
        assertEquals(HttpStatusCode.OK, version.status)
        assertTrue(version.bodyAsText().contains("celestial-backend"))
    }
}
