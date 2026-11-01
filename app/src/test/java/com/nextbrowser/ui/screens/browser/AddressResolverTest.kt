package com.nextbrowser.ui.screens.browser

import com.nextbrowser.domain.model.BrowserSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class AddressResolverTest {
    @Test
    fun blankInputOpensGoogleHomePage() {
        assertEquals("https://www.google.com", resolveAddress("   "))
    }

    @Test
    fun plainTextUsesGoogleByDefault() {
        assertEquals(
            "https://www.google.com/search?q=weather%20today",
            resolveAddress("weather today")
        )
    }

    @Test
    fun explicitlySelectedGoogleSearchesWithGoogle() {
        assertEquals(
            "https://www.google.com/search?q=best%20android%20browser",
            resolveAddress("best android browser", BrowserSettings.SEARCH_ENGINE_GOOGLE)
        )
    }

    @Test
    fun bingSearchUsesSelectedProvider() {
        assertEquals(
            "https://www.bing.com/search?q=weather%20today",
            resolveAddress("weather today", BrowserSettings.SEARCH_ENGINE_BING)
        )
    }

    @Test
    fun duckDuckGoSearchUsesSelectedProvider() {
        assertEquals(
            "https://duckduckgo.com/?q=weather",
            resolveAddress("weather", BrowserSettings.SEARCH_ENGINE_DUCKDUCKGO)
        )
    }

    @Test
    fun searchCharactersAreUtf8Encoded() {
        assertEquals(
            "https://www.google.com/search?q=cats%20%26%20dogs%20%E0%A6%AC%E0%A6%BE%E0%A6%82%E0%A6%B2%E0%A6%BE",
            resolveAddress("cats & dogs বাংলা")
        )
    }

    @Test
    fun domainGetsHttpsScheme() {
        assertEquals("https://example.com", resolveAddress("example.com"))
    }

    @Test
    fun domainWithQueryStringIsRecognizedAsAddress() {
        assertEquals(
            "https://example.com?ref=home",
            resolveAddress("example.com?ref=home")
        )
    }

    @Test
    fun domainWithPathAndPortIsRecognizedAsAddress() {
        assertEquals(
            "https://example.com:8443/path?q=hello#top",
            resolveAddress("example.com:8443/path?q=hello#top")
        )
    }

    @Test
    fun explicitHttpSchemeIsPreserved() {
        assertEquals("http://example.com/path", resolveAddress("http://example.com/path"))
    }

    @Test
    fun uppercaseHttpsSchemeIsPreserved() {
        assertEquals("HTTPS://example.com/path", resolveAddress("HTTPS://example.com/path"))
    }

    @Test
    fun localhostWithPathUsesHttp() {
        assertEquals("http://localhost:8080/dashboard", resolveAddress("localhost:8080/dashboard"))
    }

    @Test
    fun emulatorHostUsesHttp() {
        assertEquals("http://10.0.2.2:8080", resolveAddress("10.0.2.2:8080"))
    }

    @Test
    fun dottedIpAddressGetsHttpsScheme() {
        assertEquals("https://127.0.0.1", resolveAddress("127.0.0.1"))
    }

    @Test
    fun invalidIpv4AddressIsTreatedAsSearchText() {
        assertEquals(
            "https://www.google.com/search?q=256.1.1.1",
            resolveAddress("256.1.1.1")
        )
    }

    @Test
    fun inputIsTrimmedBeforeResolution() {
        assertEquals("https://example.com", resolveAddress("  example.com  "))
    }
}
