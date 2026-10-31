# NEXT Browser — Development Roadmap

## Phase 1: Foundation
- Android project configuration, Compose design system and theme

## Phase 2: First browsing experience
- Android System WebView, search/address navigation and basic browser controls

## Phase 3: Tab management foundation
- In-memory tabs, tab switcher, create and close actions

## Phase 4: Regular session persistence
- Persist regular tab IDs, URLs, titles and active tab
- Private-labelled tabs excluded from session metadata
- Live page state and complete navigation stack are not restored

## Phase 5: Personal library (implemented; build verification pending)
- Room database entities, DAO and singleton database for bookmarks and history
- Add bookmarks from the browser toolbar; duplicate URLs replace the existing bookmark record
- Search, open and delete bookmarks
- Record regular-tab page visits; private-labelled tabs are excluded
- Search and clear all browsing history
- Folder field exists in bookmark records; folder creation/rename/move UI remains future work
- Per-entry history deletion and date filters remain future work

## Phase 6: Downloads and media
- Download manager integration and notifications
- Media playback and picture-in-picture where supported

## Phase 7: Privacy and security
- Site permission controls and HTTPS-aware connection indicators
- Tracker/ad-block architecture and properly isolated private browsing
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
