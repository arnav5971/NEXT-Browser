# NEXT Browser
### Browse Beyond Limits.

NEXT Browser is an Android browser project focused on a premium, minimal experience, privacy-conscious defaults, and a modular architecture that can evolve over time.

## Project status

**Phase 3 — tab management foundation**

- [x] Android/Kotlin project structure
- [x] Jetpack Compose + Material 3 design foundation
- [x] Light/dark theme support
- [x] Home screen with search/address submission
- [x] Android System WebView integration
- [x] Address navigation, search fallback and loading progress
- [x] Page title, reload, back/forward and load error feedback
- [x] Basic WebView security configuration
- [x] In-memory tab list, tab counter, switcher and close-tab actions
- [x] Temporary/private-labelled tab creation
- [ ] Independent WebView state retention per tab
- [ ] Isolated private cookies/storage and private data cleanup
- [ ] Persistent session restoration
- [ ] Bookmarks, history and downloads

> Engine note: this build uses the Android System WebView supplied by the device. It does not bundle or independently maintain Chromium.
>
> Privacy note: the current private-labelled tabs are temporary UI sessions and are not written to persistent browser history. They do **not** yet have isolated cookies or web storage, so they must not be treated as fully private/incognito browsing.

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
