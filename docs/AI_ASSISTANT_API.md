# NEXT Browser AI Assistant — Backend Contract

## Security boundary
The Android app must call a controlled backend. Provider credentials remain server-side and are never bundled in the APK. Page content is sensitive: extraction and transmission require an explicit user action and a clear disclosure.

## Proposed endpoint
`POST /v1/browser/assistant`

Request:
```json
{
  "question": "Summarize this page in Bengali",
  "page": {
    "url": "https://example.com/article",
    "title": "Example article",
    "text": "User-approved extracted page text"
  },
  "locale": "bn-IN"
}
```

Successful response:
```json
{
  "answer": "A concise answer...",
  "requestId": "opaque-server-generated-id"
}
```

## Required backend behavior
- Authenticate and rate-limit requests.
- Enforce request-size limits and timeouts.
- Never log full page text or secrets by default.
- Treat page text as untrusted input, not as system instructions.
- Return structured errors for authentication, rate limit, unavailable provider, and invalid input.
- Support cancellation and bounded response sizes.
- Provide a retention policy and deletion mechanism.

## Android integration requirements
- User-initiated page extraction only.
- Show the destination/backend and the page title before sending.
- Exclude password fields, form values, cookies, local storage, and hidden inputs.
- Cancel requests when the dialog closes or the user navigates away.
- Display network/provider errors; never show placeholder answers as AI output.

## Current status
This file defines the contract only. A live AI assistant is not enabled until a backend endpoint and server-side provider configuration are supplied and tested.
