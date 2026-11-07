# Design System Rules: Ben Browser Liquid Glass (v2.0)

## 1. Visual References & Aesthetic
- Primary Aesthetic: Ben Browser Dark Liquid Glass — restrained, deep dark-first, luminous refraction glassmorphism with high-end physics.
- Visual References:
  - macOS Sonoma / iOS Liquid Glass floating capsule bars with dynamic specular highlights.
  - Arc Search & Minimalist Swiss typography for start pages.
- Hierarchy Principle: Dominant visual weight belongs to the web content. Navigation and controls are floating, restrained, ultra-smooth, and responsive.

## 2. Color Palette
Exact hex tokens:
- `bg_canvas`: `#0B0C0E` (Deep matte obsidian background)
- `glass_surface_base`: `#9916181D` (Translucent dark slate glass substrate)
- `glass_surface_pill`: `#352E3444` to `#1E1A1E28` (Refraction gradient fill)
- `glass_border`: `#2BFFFFFF` (Specular highlight border)
- `text_primary`: `#F5F5F7` (High-contrast clean white)
- `text_secondary`: `#8E8E93` (Muted metadata gray)
- `accent`: `#00E5FF` (Ben Electric Cyan brand signature)
- `accent_subtle`: `#1A00E5FF` (Luminous cyan aura tint)
- `disabled_tint`: `#545458` (Standard single-mechanism disabled tint)

## 3. Spacing Scale (8pt Grid)
- `space_4`: `4.dp` (compact icon margins, inner padding)
- `space_8`: `8.dp` (internal spacing between icon buttons and URL pill)
- `space_12`: `12.dp` (tile gaps)
- `space_16`: `16.dp` (bar horizontal edge padding, floating margin)
- `space_24`: `24.dp` (screen offsets)
- `space_32`: `32.dp`
- `space_48`: `48.dp`
- `space_64`: `64.dp`

## 4. Border Radius Tiers
- `radius_sm` (`8.dp`): Sub-inputs, query chips
- `radius_md` (`16.dp`): Secondary cards, dialog containers
- `radius_lg` (`24.dp`): Bottom sheets, modal overlays
- `radius_full` (`999.dp` / `CircleShape` / `Capsule`): Floating bar container, URL pill, round action buttons

## 5. Blur & Glass Tokens
- Backdrop Blur: `12.dp` with dynamic lens depth refraction
- Specular Stroke: `1.dp` gradient (`Color(0x60FFFFFF)` -> `Color(0x18FFFFFF)`)
- Drop Shadow: `20.dp` blur with `Color.Black.copy(alpha = 0.45f)`

## 6. Typography Scale
- `brand_title`: `22.sp`, Weight 700 (Bold), letter-spacing `0.2.sp`
- `url_text`: `14.sp`, Weight 500 (Medium), letter-spacing `0.1.sp`
- `caption`: `11.sp`, Weight 500 (Medium)
- Strict typography: System sans-serif stack, maximum 2 font weights on screen.

## 7. Component Usage Rules
- Bottom Bar: Floating pill shape (`radius_full`), docked above `navigationBarsPadding()` and `imePadding()`.
- Interactive Ratio: Icons at 18-20dp inside 40dp touch targets (clean ~45% ratio, no cramped or oversized mismatches).
- Performance: Animate transforms only on GPU RenderNodes (`graphicsLayer { ... }`) to avoid recomposition frame-drops.
- Disabled Handling: Single mechanism only via Compose `IconButton(enabled = ...)`, no manual double-dimming.
