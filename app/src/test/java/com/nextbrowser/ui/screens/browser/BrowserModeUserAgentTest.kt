package com.nextbrowser.ui.screens.browser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserModeUserAgentTest {
    private val webViewUserAgent =
        "Mozilla/5.0 (Linux; Android 15; Pixel 9 Build/ABCD; wv) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 " +
            "Chrome/132.0.0.0 Mobile Safari/537.36"

    @Test
    fun mobileModeKeepsAndroidMobileIdentityAndRemovesWebViewMarkers() {
        val userAgent = browserModeUserAgent(webViewUserAgent, desktopMode = false)

        assertTrue(userAgent.contains("Android 15"))
        assertTrue(userAgent.contains("Mobile Safari"))
        assertFalse(userAgent.contains("; wv)"))
        assertFalse(userAgent.contains("Version/4.0"))
    }

    @Test
    fun desktopModeUsesWindowsIdentityAndInstalledChromiumVersion() {
        val userAgent = browserModeUserAgent(webViewUserAgent, desktopMode = true)

        assertTrue(userAgent.contains("Windows NT 10.0; Win64; x64"))
        assertTrue(userAgent.contains("Chrome/132.0.0.0"))
        assertFalse(userAgent.contains("Android"))
        assertFalse(userAgent.contains("Mobile"))
    }

    @Test
    fun desktopModeFallsBackSafelyWhenEngineVersionIsUnavailable() {
        val unknownUserAgent = "Android WebView without a Chrome version"

        assertEquals(
            unknownUserAgent,
            browserModeUserAgent(unknownUserAgent, desktopMode = true)
        )
    }
}
