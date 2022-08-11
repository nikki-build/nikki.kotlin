package com.nikkibuild.websocket.app.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PropertiesProviderTest {

    @Test
    fun `when service token path is wrong`() {
        assertThrows<IllegalArgumentException> { PropertiesProvider("abc", "abc") }
    }

    @Test
    fun `when paths are okay`() {
        //no exceptions thrown
        PropertiesProvider("./serviceToken.json", "./serviceDef.json")
    }
}