# NEXT Browser
### Browse Beyond Limits.

NEXT Browser is an Android browser project focused on a premium, minimal experience, privacy-conscious defaults, and a modular architecture that can evolve over time.

## Project status

**Phase 1 — foundation and UI shell**

- [x] Android/Kotlin project structure
- [x] Jetpack Compose + Material 3 design foundation
- [x] Light/dark theme support
- [x] Initial home-screen UI
- [ ] Embedded browsing engine and navigation
- [ ] Tabs, bookmarks, history, downloads, privacy controls

> The current app is a Phase 1 UI foundation. It does not yet contain a browser engine. The first implementation will use Android System WebView; it will not be described as a bundled Chromium engine.

## Technology

- Kotlin
- Jetpack Compose and Material 3
- Single-activity Android app
- Minimum Android 10 (API 29)
- Clean Architecture / MVVM direction
- Gradle Kotlin DSL and version catalog

## Open the project

1. Open this repository in Android Studio (a recent stable version).
2. Allow Gradle sync to complete.
3. Select the `app` run configuration and run it on an Android 10+ device or emulator.

This initial repository intentionally starts with the app source and Gradle configuration. Android Studio can generate the Gradle wrapper for the installed Gradle distribution if one is not present.

## Planned architecture

```
app/
  src/main/
    java/com/nextbrowser/
      MainActivity.kt
      ui/
        components/
        screens/home/
        theme/
      core/
      data/
      domain/
```

As features are added, UI, domain logic, and data implementations will be separated into their own packages/modules where that improves maintainability.

## Development principles

- Keep every phase buildable before starting the next.
- Do not ship controls that pretend to work.
- Keep WebView behind a browser-engine abstraction so a future engine migration is possible.
- Store secrets only with platform-backed secure storage.
- Review permissions and privacy implications before adding capabilities.

## License

No license has been selected yet. All rights reserved by default until a license is added.
