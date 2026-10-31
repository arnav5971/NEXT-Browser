# NEXT Browser — Development Roadmap

## Phase 1: Foundation
- Android project configuration
- Compose design system and home screen shell
- Light/dark system theme

## Phase 2: First browsing experience
- Home search connected to browser screen
- Android System WebView adapter
- Address navigation and search fallback
- Loading progress, page title, back/forward, reload and main-frame error feedback
- Basic WebView security configuration

## Phase 3: Tab management foundation (implemented; build verification pending)
- In-memory tab model and active-tab selection
- Tab counter and tab switcher dialog
- Create and close regular tabs
- Create temporary private-labelled tabs
- Explicitly disclose that per-tab WebView state, isolated private storage and process-death session restoration are not complete

## Phase 4: Real tab/session persistence
- Retain independent WebView instances and navigation state per tab
- Design isolated private browsing using a separate WebView data directory/process before any WebView is initialized there
- Do not call private browsing fully isolated until cookie, cache, DOM storage and history behavior are tested
- Persist regular tab metadata and restore sessions safely
- Ensure private tabs are excluded from persistent session data

## Phase 5: Personal library
- Room database
- Bookmarks, folders and history
- Search and clear-history controls

## Phase 6: Downloads and media
- Download manager integration
- Download notifications and file handling
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
- Clear user consent before sending page content to an AI service

## Phase 10: Release readiness
- Unit, UI and device testing
- Crash and performance review
- Store listing, privacy disclosures and release signing

Each phase should be implemented and checked before the next phase begins.
