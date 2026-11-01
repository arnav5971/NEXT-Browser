# NEXT Browser QA Acceptance Matrix

| Area | Acceptance check |
|---|---|
| URL bar | A valid URL opens directly; plain text uses the selected search engine |
| Navigation | Back, forward, reload, stop, and home reflect actual WebView state |
| Mobile mode | Uses the device WebView's mobile identity and mobile viewport behavior |
| Desktop mode | Uses a desktop identity and wide viewport; horizontal page layout remains usable |
| Mode switching | Reloads the current URL once and preserves the selected mode after recreation |
| Tabs | Switching tabs does not route actions to the wrong tab |
| Renderer crash | A dead WebView is discarded and recreated without crashing the Activity |
| History | Only successful top-level regular-tab visits are recorded |
| Private mode | No history/session persistence; do not claim storage isolation until implemented |
| Downloads | HTTP(S) download status, failure, completion, and opening are verified on-device |
| Permissions | Camera, microphone, location, and file access are user-approved per origin |
| TLS | Certificate errors are cancelled; no proceed-through-error shortcut |
| AI | Page text is sent only after explicit user confirmation; API secrets stay on backend |
| Accessibility | All icon controls have meaningful content descriptions and usable touch targets |
| Release | Gradle help, unit tests, lint, debug APK build, and physical-device smoke tests pass |
