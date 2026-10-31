# NEXT Browser
### Browse Beyond Limits.

NEXT Browser is an Android browser project focused on a premium, minimal experience, privacy-conscious defaults, and a modular architecture that can evolve over time.

## Current implementation

**Phase 6 — downloads foundation (CI build verified; device testing pending)**

- [x] Kotlin Android application foundation with Jetpack Compose and Material 3
- [x] Android System WebView-based browsing, address/search, navigation controls and basic security settings
- [x] In-memory tab management and regular-tab URL/title session metadata persistence
- [x] Room database with bookmark and browsing-history tables
- [x] Add bookmarks from the browser toolbar; duplicate bookmark URLs update the existing entry
- [x] Browse, search and delete bookmarks
- [x] Record successful page visits from regular tabs; private-labelled tabs are excluded
- [x] Browse/search history and clear all history
- [x] Native Android DownloadManager integration for HTTP(S) downloads
- [x] Download completion notifications and in-app download list with progress/status
- [x] Open completed downloads and cancel/remove unfinished or failed items
- [ ] Bookmark folder creation, rename and move UI
- [ ] History date filters and per-entry deletion
- [ ] Independent retained WebView instances and isolated private storage
- [ ] Pause/resume controls (Android DownloadManager does not expose a public pause/resume API)
- [ ] Media playback enhancements and picture-in-picture

### Important limitations

- Android System WebView is provided by the device; a separate bundled Chromium engine is not included.
- Regular-tab session restoration reloads saved URLs; it does not preserve live page state or full back/forward history.
- Private-labelled tabs are not persisted and do not write to browsing history or bookmarks through the current browser UI, but their cookies/cache/DOM storage are **not isolated** from regular tabs.
- Bookmark folder metadata is represented in the database, but folder management controls are not yet available.
- Phase 5 and the first Phase 6 downloads implementation pass the GitHub Actions Gradle configuration, unit-test task, and debug APK build. Physical-device download behavior still needs testing.
- Android DownloadManager owns transfer execution and system notifications; the app does not currently expose pause/resume controls.
- Downloads are disabled from private-labelled tabs because system downloads are persisted by Android and the current private mode is not storage-isolated.

## Technology

- Kotlin, Jetpack Compose, Material 3
- Android System WebView
- Room database for bookmarks and history
- SharedPreferences for lightweight tab-session metadata
- Minimum Android 10 (API 29), Java 17

## Run locally

1. Open the repository in Android Studio.
2. Allow Gradle sync to complete and resolve the Room/KSP dependencies.
3. Run the `app` configuration on an Android 10+ emulator or device.
4. Internet access is required to load websites.

## Development principles

- Build and test each phase before marking it release-ready.
- Do not ship controls that pretend to work.
- Keep engine integration modular before considering a custom Chromium engine.
- Store secrets only with platform-backed secure storage.
- Review permissions and privacy implications before adding capabilities.

## License

No license has been selected. All rights reserved by default until a license is added.
