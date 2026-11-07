package app.celestial.backend

import app.celestial.backend.auth.DeviceRegisterRequest
import app.celestial.backend.auth.RefreshRequest
import app.celestial.backend.config.AppConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assume.assumeTrue
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.PostgreSQLContainer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AuthIntegrationTest {
    private var pg: PostgreSQLContainer<*>? = null

    @BeforeTest
    fun startDb() {
        assumeTrue("Docker required for integration tests", DockerClientFactory.instance().isDockerAvailable)
        pg = PostgreSQLContainer("postgres:16-alpine").apply {
            withDatabaseName("celestial")
            withUsername("celestial")
            withPassword("celestial")
            start()
        }
    }

    @AfterTest
    fun stopDb() {
        pg?.stop()
    }

    private fun config() = AppConfig(
        databaseUrl = pg!!.jdbcUrl,
        databaseUser = pg!!.username,
        databasePassword = pg!!.password,
        jwtAccessSecret = "test-access-secret-1234567890",
        jwtRefreshSecret = "test-refresh-secret-1234567890",
    )

    @Test
    fun `register refresh rotation and revoke`() = testApplication {
        application { module(configOverride = config()) }
        val c = createClient { install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) } }

        val reg = c.post("/api/v1/auth/device/register") {
            contentType(ContentType.Application.Json)
            setBody(DeviceRegisterRequest(installationId = "install-1", deviceInfo = "test"))
        }
        assertEquals(HttpStatusCode.Created, reg.status)
        val regBody = Json.parseToJsonElement(reg.body<String>()).jsonObject
        val refresh1 = regBody["refreshToken"]!!.jsonPrimitive.content
        val access1 = regBody["accessToken"]!!.jsonPrimitive.content
        assertTrue(regBody["deviceSecret"]!!.jsonPrimitive.content.isNotBlank())

        val ref = c.post("/api/v1/auth/token/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequest(refreshToken = refresh1))
        }
        assertEquals(HttpStatusCode.OK, ref.status)
        val refBody = Json.parseToJsonElement(ref.body<String>()).jsonObject
        val refresh2 = refBody["refreshToken"]!!.jsonPrimitive.content
        val access2 = refBody["accessToken"]!!.jsonPrimitive.content
        assertNotEquals(refresh1, refresh2)

        // old refresh must be rejected (rotation)
        val old = c.post("/api/v1/auth/token/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequest(refreshToken = refresh1))
        }
        assertEquals(HttpStatusCode.Unauthorized, old.status)

        val revoke = c.post("/api/v1/auth/revoke") {
            header(HttpHeaders.Authorization, "Bearer $access2")
        }
        assertEquals(HttpStatusCode.OK, revoke.status)

        // after revoke, current refresh2 should fail
        val afterRevoke = c.post("/api/v1/auth/token/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequest(refreshToken = refresh2))
        }
        assertEquals(HttpStatusCode.Unauthorized, afterRevoke.status)
    }
}
