# NEXT Browser — Advanced Implementation Plan

## Product decisions
- Platform: Android, private self-use.
- Rendering: Android System WebView (device-provided Chromium).
- App language: Kotlin.
- UI: Jetpack Compose + Material 3; preserve the existing NEXT Browser home-screen design.
- Architecture: modular UI, domain, data, engine, and platform layers.
- AI: secure backend integration; never embed provider API secrets in the APK.

## Existing implementation to preserve
The project already contains a Compose application, WebView engine adapter, URL/search navigation, Mobile/Desktop User-Agent switching, Room-backed bookmarks/history, regular-tab session metadata, DownloadManager integration, settings, and a GitHub Actions debug-APK workflow.

## Delivery phases
1. **Core stability:** URL resolution, page lifecycle, navigation, WebView renderer recovery, Mobile/Desktop mode, error states, responsive toolbar.
2. **Browsing:** retained per-tab WebView lifecycle, tab restoration, tab groups, private-profile storage isolation.
3. **Library:** bookmark folders and editing, history filters and per-entry deletion, search history.
4. **Downloads and media:** download lifecycle UI, media session integration, fullscreen cleanup, Picture-in-Picture where supported.
5. **Privacy:** private profile isolation, site permissions, tracking protection, safe browsing and secure defaults.
6. **Assistant:** page-content extraction with user consent, backend API contract, streaming answers, summary/translation actions, context and privacy controls.
7. **Release quality:** unit/instrumentation tests, lint, CI APK artifact, physical-device QA, accessibility and performance checks.

## Non-negotiable engineering rules
- Do not display controls that do nothing.
- Do not claim private browsing is isolated until cookies, cache, DOM storage, service workers, and downloads are handled in an isolated profile.
- Do not bypass TLS certificate errors.
- Do not expose JavaScript bridges to untrusted origins.
- Do not store AI provider API keys in Android resources, BuildConfig, or source control.
- Do not call a feature complete until its acceptance tests pass on a physical Android device.

## Definition of done
A feature is complete only when its UI action, state handling, persistence (if applicable), error handling, accessibility labels, automated tests, and physical-device behavior have been checked.
