# Fresh browser project

This branch starts a clean Android browser implementation rather than carrying over the previous app source.

Technology: Kotlin, Jetpack Compose, Android System WebView, Android DownloadManager, SharedPreferences.

Current scope: URL/search, navigation, tabs, bookmarks, history, downloads, desktop-site mode, sharing and theme switch.

Privacy limitation: private tabs are not isolated from regular WebView cookies/cache/DOM storage yet. The app explicitly discloses this.

Build: Java 17 + Gradle 8.13. GitHub Actions runs unit tests and assembles a debug APK artifact.
