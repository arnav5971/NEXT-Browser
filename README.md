# NEXT Browser

A fresh Android-first browser built with Kotlin, Jetpack Compose and Android System WebView.

## First implementation
- Combined address and web search
- HTTPS-first URL handling
- Web navigation, back, forward and reload
- Multiple tabs and tab switcher
- Local bookmarks and browsing history
- Clear-history action
- Android DownloadManager integration
- Desktop-site toggle, share page, light/dark appearance
- Privacy disclosure for current private-tab limitations

## Privacy note
Private tabs are excluded from local browsing history, but WebView cookies, cache and DOM storage are not isolated from regular tabs yet. This is not fully isolated private browsing.

## Build
GitHub Actions uses Java 17 and Gradle 8.13 to run unit tests and assemble a debug APK. The APK is uploaded as the next-browser-debug-apk workflow artifact.

Minimum Android version: Android 10 (API 29).
