# NEXT Browser
### Browse Beyond Limits.

NEXT Browser is an Android browser project focused on a premium, minimal experience, privacy-conscious defaults, and a modular architecture that can evolve over time.

## Current implementation

**Phase 4 — regular tab session persistence (build verification pending)**

- [x] Kotlin Android application foundation
- [x] Jetpack Compose and Material 3 UI foundation
- [x] Home screen and search/address entry
- [x] Android System WebView-based browsing
- [x] Navigation controls, loading progress and page title
- [x] Basic WebView security settings
- [x] In-memory regular and private-labelled tab list
- [x] Tab switcher, create and close actions
- [x] Persist regular tab IDs, last URLs and titles using app-private SharedPreferences
- [x] Restore the last active regular tab after app restart
- [x] Exclude private-labelled tabs from persisted session metadata
- [ ] Retain each tab's live WebView instance and page/form/scroll state
- [ ] Isolate private cookies, cache and DOM storage
- [ ] Persistent bookmarks, history and downloads

### Privacy and session limitations

- The browser uses the Android System WebView supplied by the device. It does not bundle a separate Chromium engine.
- Session restoration reloads each regular tab's last saved URL. It does not preserve live page state, form values, scroll position or the exact back/forward history stack.
- Private-labelled tabs are not stored in the regular session metadata and are discarded when the app process is restarted.
- Private-labelled tabs currently use the same WebView profile as regular tabs. Cookie/cache/DOM storage isolation is **not implemented**, so this mode must not be treated as fully isolated incognito browsing.
- No Android build or device test has been recorded for this phase yet.

## Technology

- Kotlin
- Jetpack Compose and Material 3
- Android System WebView (device-provided)
- SharedPreferences for lightweight regular-tab session metadata
- Minimum Android 10 (API 29)

## Run locally

1. Open this repository in Android Studio.
2. Allow Gradle sync to complete.
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
