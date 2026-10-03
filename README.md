# Ben Browser

A minimal, lightweight WebView-based Android browser built with **Kotlin**, **Jetpack Compose**, and **Haze 2.0.1** for hardware-accelerated glassmorphism.

## Step 1 Spike Overview

This initial step implements the core minimal browser architecture:
- **Single Activity**: `MainActivity` running Compose edge-to-edge.
- **WebView**: Hardware-accelerated with algorithmic darkening (`WebSettingsCompat`), dark matte base (`#0B0C0E`), and DOM/JS capabilities.
- **Glass Bottom Bar**: Docked above system navigation bar insets (`navigationBarsPadding()`) and software keyboard (`imePadding()`).
- **Apple/macOS Dark Minimal Aesthetic**: Restrained palette, system typography (`15sp` URL text), specular edge borders, and full pill shape (`radius_full`).
- **Navigation Controls**: Back and forward using `AutoMirrored` icons (`Icons.AutoMirrored.Filled.ArrowBack`, `Icons.AutoMirrored.Filled.ArrowForward`) and reload (`Icons.Filled.Refresh`), governed by a single disabled mechanism (`IconButton(enabled = ...)`).

---

## Hardware Spike: Testing Haze on WebView

### The Hypothesis
In Android Jetpack Compose, `AndroidView` hosts a native Android `WebView`. Because Chromium rendering often bypasses Compose's canvas layer recording depending on OS version (API 26–30 vs API 31+ RenderNode), `Modifier.hazeSource` may or may not capture the WebView's dynamic content for blur.

### How to Verify on Device
1. Build and install the debug APK (locally or via GitHub Actions artifact).
2. Browse a colorful or high-contrast webpage (e.g. DuckDuckGo or Wikipedia).
3. Observe the bottom floating pill bar:
   - **Haze Active**: Content scrolling behind the pill appears frosted and blurred through Haze 2.0.1.
   - **Spike Toggle**: Long-press anywhere on the bottom bar to instantly switch between **Haze Glass Blur** and the **90% Opaque Fallback** (`#E616181D`). A toast message confirms the mode.

---

## CI / GitHub Actions

A workflow is configured at [`.github/workflows/build.yml`](.github/workflows/build.yml):
- Triggers on every `push` and `pull_request`.
- Runs `./gradlew assembleDebug` on Ubuntu with JDK 17.
- Uploads the resulting `app-debug.apk` as a workflow artifact named `ben-browser-debug-apk`.

---

## Design System Tokens

Recorded in [`.agent/rules/design.md`](.agent/rules/design.md):
- `bg_canvas`: `#0B0C0E`
- `glass_surface_haze`: `#B316181D` (70% opacity)
- `glass_surface_fallback`: `#E616181D` (90% opacity)
- `glass_border`: `#1FFFFFFF` (12% specular white highlight)
- `text_primary`: `#F5F5F7`
- `accent`: `#0A84FF`
