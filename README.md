# NEXT Browser
### Browse Beyond Limits.

NEXT Browser is a private-use Android browser built with Kotlin, Jetpack Compose, Material 3, and Android System WebView.

## Product configuration
- Platform: Android (minimum Android 10 / API 29)
- Rendering engine: Android System WebView supplied by the device
- UI: premium native Compose interface; preserve the existing NEXT Browser home-screen design
- Architecture: modular engine, UI, domain, data, and platform layers
- AI: backend-mediated assistant; provider secrets must never be bundled in the APK

## Existing implementation
- Kotlin Android application with Jetpack Compose and Material 3
- Android System WebView browsing, address/search, navigation, loading and error handling
- Separate Mobile/Desktop User-Agent handling and viewport configuration
- WebView renderer-crash recovery and guarded cleanup
- Regular-tab session metadata persistence
- Room-backed bookmarks and browsing history
- Native Android DownloadManager integration and in-app download status
- Theme and search-engine preferences
- GitHub Actions workflow for Gradle validation, unit tests, lint, and debug APK artifact

## Current limitations — do not treat as completed
- The WebView engine is device-provided; a separately bundled Chromium engine is not included.
- Regular-tab restoration reloads saved URLs and does not guarantee restoration of live page state or full navigation history.
- Private tabs are currently not fully isolated from regular-tab cookies, cache, and DOM storage. Do not describe them as secure isolated private profiles.
- Bookmark folders, richer history management, independent retained WebViews, and enhanced media/Picture-in-Picture behavior remain in the roadmap.
- Android DownloadManager does not expose a public pause/resume API.
- AI assistant contract is scaffolded, but live AI responses require a configured secure backend. No provider API key may be stored in the APK.

## Development roadmap
1. Core stability: URL resolution, navigation, WebView lifecycle, mode switching, responsive controls.
2. Browsing: retained per-tab WebViews, tab restoration, tab groups, isolated private profiles.
3. Library: bookmark folders, history filters, per-entry deletion, search history.
4. Downloads and media: reliable lifecycle UI, media session and Picture-in-Picture where supported.
5. Privacy: per-origin permissions, tracking protection, safe defaults, private-profile isolation.
6. AI assistant: consent-based page extraction, secure backend, summaries, translation, streaming.
7. Release: automated tests, lint, APK build, physical-device QA, accessibility and performance checks.

## Build and test
1. Open the repository in Android Studio.
2. Use JDK 17 and allow Gradle sync to finish.
3. Run on an Android 10+ emulator or physical device.
4. Internet access is required for websites.
5. Run `gradle :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`.

## Engineering principles
- Do not ship controls that pretend to work.
- Do not bypass TLS certificate errors.
- Do not expose JavaScript bridges to untrusted origins.
- Keep AI provider credentials server-side.
- Obtain user consent before sending page content to an AI backend.
- Mark a feature complete only after automated tests and physical-device checks pass.

## License
No license has been selected. All rights reserved by default until a license is added.
