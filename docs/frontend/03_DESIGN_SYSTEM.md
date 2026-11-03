# 03 — Design System

## 1. Visual foundation

The design system must support two visual contexts:

### A. Space / 3D environment
- Dominant background: pitch black.
- Sparse, tiny stars with subtle twinkle.
- Occasional distant comets.
- Soft, restrained violet-blue and silver atmospheric light.
- A bright fictional central star with a white core and subtle violet-blue corona.
- Avoid filling the entire screen with coloured nebula haze; black space must remain dominant.

### B. Glass destination UI
- Familiar, minimalist mobile UI.
- Translucent dark glass surfaces over the space background.
- Low-opacity borders and soft depth.
- Background stars and occasional comets remain visible through/around surfaces.
- Text contrast and content legibility take priority over transparency.

## 2. Initial colour tokens

These are the current proposed starting tokens from the design exploration. Treat them as initial values to validate in rendered screens, not immutable final colour science.

| Token | Hex | Intended use |
|---|---|---|
| `spaceVoid` | `#000000` | Main universe background |
| `deepSpace` | `#080B18` | Destination background tint |
| `deepSurface` | `#10152A` | Dark glass surface base |
| `nebula` | `#22264A` | Secondary surface / atmospheric tint |
| `cosmicViolet` | `#7773C9` | Primary accent |
| `starlight` | `#D8D6F5` | Primary text / highlights |
| `moonlight` | `#A7A9C4` | Secondary text |
| `auroraBlue` | `#8BAFCB` | Secondary accent |
| `glassBorder` | `#FFFFFF` at low alpha | Hairline glass border |

Use semantic Compose colour tokens rather than scattering hex values through composables. Validate contrast, OLED appearance and device variation.

## 3. Glass surfaces

Use glass for playlist rows, search fields, cards where appropriate, settings panels and secondary controls.

Characteristics:
- Dark translucent fill; the cosmic background remains visible.
- Subtle blur only where performance permits.
- Thin low-opacity border.
- Soft ambient elevation rather than hard shadows.
- Rounded corners, initially around 16–22dp for list surfaces.
- Avoid excessive nested glass-on-glass layers.
- Do not put every text label inside a separate pill or card.

If real-time blur is expensive, use a translucent gradient/scrim approximation. Never sacrifice scrolling smoothness for blur.

## 4. Typography

Proposed direction:
- Clean, highly legible sans-serif for functional UI.
- Elegant display treatment may be used sparingly for destination titles or cinematic moments.
- Clear hierarchy between title, artist, metadata and section labels.
- Support Android font scaling.
- Do not use decorative fonts for controls, track metadata or long lists.

Candidate typefaces may be evaluated by the design skills; final typeface is an open decision.

## 5. Spacing and shape

- Use a consistent spacing scale based on 4dp increments.
- Maintain comfortable screen-edge padding.
- Use 48dp minimum touch targets.
- Playlist/list surfaces: approximately 16–22dp corner radius as a starting point.
- Avoid rigid fixed positioning; use responsive constraints and insets.

## 6. Iconography

- Use simple, consistent icons.
- Prefer clear familiar symbols for queue, shuffle, repeat, favourite, volume, search and settings.
- Signature controls (UFO, asteroids, planetary ring) are custom visual objects, but must still expose accessible semantics and touch targets.
- Do not use emoji as production icons.

## 7. States

Every interactive component must define:
- Default
- Pressed
- Selected/active
- Disabled
- Loading, where relevant
- Error, where relevant

Use restrained accent illumination for active states. Avoid bright neon borders around every selected item.

## 8. Theming

The intended primary theme is dark celestial. Do not introduce a bright white theme unless explicitly requested. A reduced-effects option is required; other theme customisation remains an open decision.
