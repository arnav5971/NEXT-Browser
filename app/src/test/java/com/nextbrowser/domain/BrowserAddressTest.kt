package com.nextbrowser.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class BrowserAddressTest {
    @Test fun blankInputReturnsEmpty() = assertEquals("", BrowserAddress.resolve(" "))
    @Test fun explicitSchemesArePreserved() {
        assertEquals("http://example.com", BrowserAddress.resolve("http://example.com"))
        assertEquals("https://example.com", BrowserAddress.resolve("https://example.com"))
    }
    @Test fun domainAndIpv4DefaultToHttps() {
        assertEquals("https://example.com/path", BrowserAddress.resolve("example.com/path"))
        assertEquals("https://192.168.1.10:8443/dashboard", BrowserAddress.resolve("192.168.1.10:8443/dashboard"))
    }
    @Test fun localhostDefaultsToHttps() = assertEquals("https://localhost:8080", BrowserAddress.resolve("localhost:8080"))
    @Test fun selectedProviderSearchesText() =
        assertEquals("https://www.bing.com/search?q=hello+world", BrowserAddress.resolve("hello world", SearchProvider.BING))
    @Test fun invalidIpv4IsSearchText() =
        assertEquals("https://www.google.com/search?q=256.1.1.1", BrowserAddress.resolve("256.1.1.1"))
}
