# NEXT Browser
### Browse Beyond Limits.

NEXT Browser is an Android browser project focused on a premium, minimal experience, privacy-conscious defaults, and a modular architecture that can evolve over time.

## Project status

**Phase 2 — first browsing experience**

- [x] Android/Kotlin project structure
- [x] Jetpack Compose + Material 3 design foundation
- [x] Light/dark theme support
- [x] Home screen with working search/address submission
- [x] Android System WebView integration
- [x] Address navigation and search query handling
- [x] Loading progress, page title, reload, back and forward
- [x] Main-frame load error feedback
- [x] Basic WebView hardening (no file/content access, no mixed content, Safe Browsing)
- [ ] Tabs, bookmarks, history, downloads and advanced privacy controls

> Engine note: this build uses the Android System WebView installed on the device. It is Chromium-based on most supported Android devices, but NEXT Browser does not bundle or independently maintain a Chromium engine.

## Technology

- Kotlin
- Jetpack Compose and Material 3
- Android System WebView (device-provided)
- Single-activity Android app
- Minimum Android 10 (API 29)
- Gradle Kotlin DSL and version catalog

## Open the project

1. Open this repository in Android Studio (a recent stable version).
2. Allow Gradle sync to complete.
3. Select the `app` run configuration and run it on an Android 10+ device or emulator.
4. Internet access is required to load websites.

## Development principles

- Keep every phase buildable before starting the next.
- Do not ship controls that pretend to work.
- Keep the browsing engine behind an abstraction before considering a custom engine.
- Store secrets only with platform-backed secure storage.
- Review permissions and privacy implications before adding capabilities.

## License

No license has been selected yet. All rights reserved by default until a license is added.
