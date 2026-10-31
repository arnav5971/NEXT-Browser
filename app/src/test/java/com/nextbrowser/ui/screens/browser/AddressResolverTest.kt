package com.nextbrowser.ui.screens.browser

import org.junit.Assert.assertEquals
import org.junit.Test

class AddressResolverTest {
    @Test
    fun blankInputOpensHomePage() {
        assertEquals("https://www.google.com", resolveAddress("   "))
    }

    @Test
    fun domainGetsHttpsScheme() {
        assertEquals("https://example.com", resolveAddress("example.com"))
    }

    @Test
    fun explicitHttpSchemeIsPreserved() {
        assertEquals("http://example.com/path", resolveAddress("http://example.com/path"))
    }

    @Test
    fun plainSearchTextUsesSearchProvider() {
        assertEquals("https://www.google.com/search?q=weather", resolveAddress("weather"))
    }

    @Test
    fun searchSpacesAreEncoded() {
        assertEquals("https://www.google.com/search?q=weather%20today", resolveAddress("weather today"))
    }
    @Test
    fun inputIsTrimmedBeforeResolution() {
        assertEquals("https://example.com", resolveAddress("  example.com  "))
    }

    @Test
    fun uppercaseHttpsSchemeIsPreserved() {
        assertEquals("HTTPS://example.com/path", resolveAddress("HTTPS://example.com/path"))
    }

    @Test
    fun localhostIsTreatedAsAHost() {
        assertEquals("http://localhost", resolveAddress("localhost"))
    }

    @Test
    fun dottedIpAddressIsTreatedAsAHost() {
        assertEquals("https://127.0.0.1", resolveAddress("127.0.0.1"))
    }

    @Test
    fun reservedSearchCharactersAreEncoded() {
        assertEquals(
            "https://www.google.com/search?q=cats%20%26%20dogs",
            resolveAddress("cats & dogs")
        )
    }

    @Test
    fun bingSearchUsesSelectedProvider() {
        assertEquals(
            "https://www.bing.com/search?q=weather%20today",
            resolveAddress("weather today", "bing")
        )
    }

    @Test
    fun duckDuckGoSearchUsesSelectedProvider() {
        assertEquals(
            "https://duckduckgo.com/?q=weather",
            resolveAddress("weather", "duckduckgo")
        )
    }

    @Test
    fun localhostUsesHttpForLocalDevelopmentServers() {
        assertEquals("http://localhost:8080", resolveAddress("localhost:8080"))
    }

    @Test
    fun domainWithPortIsRecognizedAsAddress() {
        assertEquals("https://example.com:8443/path", resolveAddress("example.com:8443/path"))
    }
}
