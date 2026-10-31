# NEXT Browser — Development Roadmap

## Phase 1: Foundation (current)
- Android project configuration
- Compose design system and home screen shell
- Light/dark system theme

## Phase 2: Browser engine
- Define a `BrowserEngine` interface
- Implement the first adapter with Android System WebView
- Add address navigation, loading progress, back/forward, reload and error states
- Clearly document WebView limitations and engine ownership

## Phase 3: Tabs and sessions
- Tab model and tab switcher
- Private tab session isolation
- Session restoration and tab groups

## Phase 4: Personal library
- Room database
- Bookmarks, folders and history
- Search and clear-history controls

## Phase 5: Downloads and media
- Download manager integration
- Download notifications and file handling
- Media playback and picture-in-picture where supported

## Phase 6: Privacy and security
- Site permission controls
- HTTPS-aware connection indicators
- Tracker/ad-block rules architecture with transparent limitations
- Secure storage design and privacy review

## Phase 7: Settings and polish
- Appearance, search provider, startup and privacy settings
- Accessibility, adaptive layouts and performance work

## Phase 8: Optional AI features
- AI assistance through a secured backend
- No provider secrets embedded in the Android application
- Clear user consent before sending page content to an AI service

## Phase 9: Release readiness
- Unit, UI and device testing
- Crash and performance review
- Store listing, privacy disclosures and release signing

Each phase should be implemented and checked before the next phase begins.
