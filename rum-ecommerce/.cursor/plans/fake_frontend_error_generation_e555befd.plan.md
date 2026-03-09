---
name: Fake Frontend Error Generation
overview: Add a mix of fake frontend errors (JS exceptions, HTTP 404s/500s, bad form submissions, broken resources) to the load generator and frontend app, so they can later be captured by Datadog RUM Error Tracking for practice.
todos:
  - id: err-1
    content: Create DebugController with /debug/error, /debug/slow, /debug/not-found endpoints
    status: completed
  - id: err-2
    content: Add errorSession() to run.js with JS errors, HTTP errors, bad forms, and resource errors
    status: completed
  - id: err-3
    content: Wire errorSession into the main loop (once per 40s cycle)
    status: completed
isProject: false
---

# Fake Frontend Error Generation

## Goal

Generate a variety of realistic frontend errors so that once Datadog RUM + Error Tracking is instrumented, there's a rich set of errors to practice with: JS errors, HTTP errors, resource errors, and server-side exceptions.

## What will be added

### 1. Error route in the frontend (Java)

Add a `DebugController` with endpoints that produce different error types:

- `GET /debug/error` -- throws an unhandled exception (500)
- `GET /debug/slow` -- sleeps 5-10s then responds (simulates slow endpoint, useful for Datadog APM later)
- `GET /debug/not-found` -- returns 404 via `ResponseStatus`

File: `frontend/src/main/java/com/classicjazz/controller/DebugController.java`

### 2. Error session in the load generator (`run.js`)

Add an `errorSession(page)` function that runs once per 40s cycle and does:

- **JS errors**: `page.evaluate()` to throw uncaught exceptions, trigger `console.error()`, and create unhandled promise rejections
- **HTTP errors**: navigate to `/debug/error` (500), `/debug/not-found` (404), and a truly non-existent path like `/nonexistent-page` (404)
- **Bad form submissions**: submit checkout with an empty cart, submit signup with a duplicate email
- **Resource errors**: inject a broken `<img>` tag and a broken `<script>` tag so the browser generates resource loading errors

### 3. Files to create/modify

- **Create** `frontend/src/main/java/com/classicjazz/controller/DebugController.java` -- error endpoints
- **Modify** `load-gen/run.js` -- add `errorSession()`, call it once per run cycle