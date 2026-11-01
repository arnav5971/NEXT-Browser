# NEXT Browser

A mobile-first Android browser built with Kotlin, Jetpack Compose and Android System WebView.

## Interface
- Premium gradient welcome header and clean responsive mobile layout
- Dedicated address/search field and quick-launch shortcuts
- Refined navigation bar with back, forward, reload/home, new tab and tab switcher
- Redesigned tab manager, bookmarks, history, downloads and privacy screens
- Light/dark appearance preference persisted locally
- Home dashboard with saved pages and recently visited pages

## Browser engine and local data
- JavaScript, DOM storage and image loading enabled for modern websites
- Mobile and desktop User-Agent switching, wide viewport and zoom controls
- HTTPS-first URL handling and Google search for plain queries
- Local bookmarks and recent history stored in app preferences
- Android DownloadManager integration

## Privacy note
Private tabs are excluded from local browsing history, but WebView cookies, cache and DOM storage are not isolated from regular tabs yet. This is not fully isolated private browsing.

## Build
GitHub Actions uses Java 17 and Gradle 8.13 to run unit tests and assemble a debug APK. The APK is uploaded as the `next-browser-debug-apk` workflow artifact after a successful build.

Minimum Android version: Android 10 (API 29).
