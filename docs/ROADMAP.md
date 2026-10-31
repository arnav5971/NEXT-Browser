# NEXT Browser — Development Roadmap

## Phase 1: Foundation
- Android project configuration
- Compose design system and home screen shell
- Light/dark system theme

## Phase 2: First browsing experience
- Android System WebView adapter and address navigation
- Search fallback, loading progress, title, back/forward and reload
- Basic WebView security configuration

## Phase 3: Tab management foundation
- In-memory tab model, tab switcher, create and close actions
- Temporary private-labelled tabs
- Per-tab WebView instances and isolated private storage remain future work

## Phase 4: Regular session persistence (implementation added; build verification pending)
- Restore regular tab metadata (tab ID, last URL and title) after app restart
- Restore the previously active regular tab
- Exclude private-labelled tabs from persisted session metadata
- Private-labelled tabs are discarded on process restart
- Reload restored URLs; live page state, form state and scroll position are not persisted
- Private cookies/cache/DOM storage isolation and deterministic private-data cleanup are not implemented yet

## Phase 5: Personal library
- Room database for bookmarks, folders and history
- Search and clear-history controls

## Phase 6: Downloads and media
- Download manager integration and notifications
- Media playback and picture-in-picture where supported

## Phase 7: Privacy and security
- Site permission controls
- HTTPS-aware connection indicators
- Tracker/ad-block rules architecture with transparent limitations
- Secure storage design and privacy review

## Phase 8: Settings and polish
- Appearance, search provider, startup and privacy settings
- Accessibility, adaptive layouts and performance work

## Phase 9: Optional AI features
- AI assistance through a secured backend
- No provider secrets embedded in the Android application
- Clear consent before sending page content to an AI service

## Phase 10: Release readiness
- Unit, UI and device testing
- Crash and performance review
- Store listing, privacy disclosures and release signing

Every phase must be built and tested before being marked release-ready.