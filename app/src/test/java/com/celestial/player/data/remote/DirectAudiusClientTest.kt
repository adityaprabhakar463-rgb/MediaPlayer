package com.celestial.player.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DirectAudiusClientTest {

    @Test
    fun `curated track resolves to direct audius stream url`() {
        DirectAudiusClient.updateApiKey(null)
        val url = DirectAudiusClient.resolveDirectStreamUrl("album-zero-gravity-1")
        assertEquals(
            "https://discoveryprovider.audius.co/v1/tracks/dGON5PJ/stream?app_name=Celestial",
            url,
        )
    }

    @Test
    fun `direct audius id resolves directly`() {
        DirectAudiusClient.updateApiKey(null)
        val url = DirectAudiusClient.resolveDirectStreamUrl("2PWPRN3")
        assertEquals(
            "https://discoveryprovider.audius.co/v1/tracks/2PWPRN3/stream?app_name=Celestial",
            url,
        )
    }

    @Test
    fun `api key is appended when configured`() {
        DirectAudiusClient.updateApiKey("test-key-123")
        val url = DirectAudiusClient.resolveDirectStreamUrl("album-zero-gravity-1")
        assertTrue(url.contains("api_key=test-key-123"))
        assertTrue(url.contains("app_name=Celestial"))
        DirectAudiusClient.updateApiKey(null)
    }
}
