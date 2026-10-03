# Design System Rules: Minimal Dark Glass Browser

## 1. Visual References & Aesthetic
- Primary Aesthetic: Apple/macOS restrained dark-first glassmorphism.
- Visual References:
  - Apple Safari (iOS 18 / iPadOS) floating glass address bar with specular edge highlights.
  - Arc Search minimal bottom floating pill.
- Hierarchy Principle: Dominant visual weight belongs to the WebView content. Bottom bar is quiet, restrained, floating, and glassmorphic.

## 2. Color Palette
Exact hex tokens:
- `bg_canvas`: `#0B0C0E` (Deep matte black background)
- `glass_surface_haze`: `#B316181D` (70% opacity dark slate base when Haze blur is active)
- `glass_surface_fallback`: `#E616181D` (90% opaque dark slate base for spike fallback)
- `glass_border`: `#1FFFFFFF` (12% specular white border stroke)
- `text_primary`: `#F5F5F7` (Apple off-white)
- `text_secondary`: `#86868B` (Apple secondary gray)
- `accent`: `#0A84FF` (Apple system blue)
- `disabled_tint`: `#545458` (Standard single-mechanism disabled tint)

## 3. Spacing Scale (8pt Grid)
- `space_4`: `4.dp` (compact icon margins, inner padding)
- `space_8`: `8.dp` (internal spacing between icon buttons and URL pill)
- `space_16`: `16.dp` (bar horizontal edge padding, floating margin)
- `space_24`: `24.dp` (screen offsets)
- `space_32`: `32.dp`
- `space_48`: `48.dp`
- `space_64`: `64.dp`

## 4. Border Radius Tiers
- `radius_sm` (`8.dp`): Sub-inputs, query chips
- `radius_md` (`16.dp`): Secondary cards, dialog containers
- `radius_lg` (`24.dp`): Bottom sheets, modal overlays
- `radius_full` (`999.dp` / `CircleShape`): Bottom bar container, URL pill, round action buttons

## 5. Blur & Glass Tokens
- `haze_blur_radius`: `20.dp`
- `glass_border_width`: `1.dp`
- Glass tint effect: `Color(0xB316181D)`
- Fallback surface opacity: `90%` (`0xE6` alpha, `Color(0xE616181D)`)

## 6. Typography Scale
- `url_text`: `15.sp`, Weight 400 (Regular), letter-spacing `0.1.sp`
- `caption`: `12.sp`, Weight 400 (Regular)
- Strict typography: System sans-serif stack, maximum 2 font weights on screen.

## 7. Component Usage Rules
- Bottom Bar: Floating pill shape (`radius_full`), docked above `navigationBarsPadding()` and `imePadding()`.
- Icons: `AutoMirrored` icons for directional controls (`Icons.AutoMirrored.Filled.ArrowBack`, `Icons.AutoMirrored.Filled.ArrowForward`).
- Disabled Handling: Single mechanism only via Compose `IconButton(enabled = ...)`, no manual double-dimming.
- Spike Fallback: If Haze does not blur native WebView on real device hardware, fall back cleanly to 90% opaque surface.
