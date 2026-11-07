# Ben Browser (v2.0)

A high-performance, dark liquid-glass Android browser built with **Kotlin**, **Jetpack Compose**, **Mozilla GeckoView** (WebRender GPU multi-process engine), and **AndroidLiquidGlass** (`io.github.kyant0:backdrop`) for real-time hardware-accelerated refraction and blur.

---

## What's New in v2.0

### 1. 🏎️ 120 FPS Scroll & Animation Performance
- **Zero-recomposition Physics Layer**: Squash & stretch spring dynamics now execute entirely on GPU RenderNodes (`graphicsLayer`), eliminating recomposition churn.
- **Debounced View Updates**: Fixed `AndroidView` update callbacks and state loops that previously fired on scroll passes.
- **Optimized UI Allocation**: Cached interaction sources and eliminated repeated composable allocations.

### 2. ⚡ Distinctive Ben Identity & Brand
- **Signature Accent**: Electric Cyan (`#00E5FF`) on deep obsidian dark glass (`#0B0C0E`).
- **Brand Logomark**: Dynamic luminous "B" glass badge on Start Page and new vector app launcher icon with platinum highlights.
- **Refined Greeting**: Clean, elegant time-aware greeting header ("Good Morning", "Late Night Mode") with active search engine branding.

### 3. 🎨 Refined Vector Icon Suite
- **Uniform 1.6dp Stroke Weight**: Standardized across all navigation controls, search, bookmarks, settings, and reload.
- **Modern Bookmark Ribbon**: Replaced dated 5-point star with a sleek bookmark ribbon.
- **Outline Minimalist Home**: Replaced heavy chunky fill with a clean outline glyph.
- **45% Golden Touch Ratio**: 18-20dp crisp icons inside 40dp interactive touch targets.

### 4. 💎 True Liquid Glassmorphism
- **Backdrop Refraction & Lens Distortion**: Live real-time refraction, chromatic aberration, and lens depth using Kyant0 Backdrop.
- **Specular Highlights**: Specular edge borders (`#2BFFFFFF`) and ambient drop shadows.

---

## Architecture & Tech Stack

- **GeckoView Engine**: `org.mozilla.geckoview:geckoview-arm64-v8a:157.0` running with WebRender GPU acceleration and `BACKEND_TEXTURE_VIEW`.
- **UI Framework**: Jetpack Compose (BOM `2024.10.01`) + Material 3.
- **Liquid Glass**: `io.github.kyant0:backdrop:2.0.1` and `io.github.kyant0:shapes:1.2.1`.
- **Architecture**: MVVM with `BrowserViewModel`, StateFlow, and Jetpack Lifecycle.

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
- `glass_surface_base`: `#9916181D`
- `glass_border`: `#2BFFFFFF`
- `text_primary`: `#F5F5F7`
- `text_secondary`: `#8E8E93`
- `accent`: `#00E5FF` (Electric Cyan)
