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
}
