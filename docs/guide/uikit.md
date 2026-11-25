# UiKit

Session widget toolkit through `EngineServices.uiKit()`. Package: `io.github.worldsmiths.core.ui`. No LibGDX types in the contract.

Hybrid leaves (Button, Label, Panel, TraySlot, Swatch, ModalOverlay), logical top-left layout and hit, tab focus ring, and paint via existing [`Draw2D`](draw2d.md) / [`Font`](ui.md) facades — not Scene2D and not a new `GdxUiKit`. Public `Font` has no measure API; public `Draw2D` is unchanged (glyphs stay toolkit-internal).

## Ownership / session

`UiKit` from the bag is a **session factory**: one stable instance per bag lifetime (`assertSame` on repeated `uiKit()`). Authors build leaves per screen or mode, call `layout()`, then `hit` / `draw` each frame as needed.

Call `clear()` to drop all roots, reset focus, and record `DISPOSE` on Fake. Idempotent. Authors do **not** dispose individual leaves.

Keep using [`ui()`](ui.md) for author-owned fonts (`createFont` / `font.dispose()`). Pass a loaded `Font` into `draw(draw2d, font)` alongside `services.draw2d()`.

## Out of scope (this slice)

- Scene2D, Skin, or `GdxUiKit`
- Safe-area values on `Insets` (API present; platforms still pass zero)
- New public `Font.measure` or extra `Draw2D` author APIs — layout uses toolkit-internal measure only

## Theme, Insets, scale

| Type | Role |
| --- | --- |
| `Theme` | Panel / button / focus ring RGBA roles via `setTheme`; `Theme.defaults()` ships sensible colors |
| `TextStyle` | Named size role (`body()`) — typeshipped for future typography wiring; session draw uses fixed body sizing today |
| `Insets` | `top` / `left` / `bottom` / `right` logical px subtracted from viewport; `Insets.ZERO` until a backend feeds safe area |
| `uiScale` | Multiplier on content width/height inside insets (`setViewport`); values `<= 0` clamp to `1` |

## API

### Factory (`UiKit`)

| Method | Meaning |
| --- | --- |
| `setTheme(Theme theme)` | Replace color roles (null ignored) |
| `setViewport(width, height, insets, uiScale)` | Viewport + inset + scale; invalidates layout |
| `clear()` | Remove all roots and focus; idempotent |
| `button(id, label)` | Focusable action leaf |
| `label(id, text)` | Read-only text leaf |
| `panel(id)` | HUD region; `panel.label(childId, text)` for nested labels |
| `traySlot(id)` | Ordered dynamic row — `length(n)` then `slot(index, slotId, label)` |
| `swatch(id)` | Color sample — `color(r,g,b,a)` |
| `modalOverlay(id)` | Modal shell — `dismiss(dismissId, label)` for dismiss button |
| `focusScope()` | Tab ring — `add(button)`, `focusNext` / `focusPrev`, `activateFocused`, `focusedId` |
| `layout()` | Assign `UiBounds` in logical space (records `LAYOUT` on Fake) |
| `hit(x, y)` | Top-most target; min touch ≥ 48 logical px — records `HIT` on Fake |
| `draw(draw2d, font)` | `setOrtho2D` + single Y-flip into Draw2D space; records draw leaf kinds on Fake |

### Leaves (selected)

| Type | Key surface |
| --- | --- |
| `Button` | `id()`, `label()`, `enabled(boolean)` / `enabled()`, `bounds()` |
| `Label` | `id()`, `text()`, `bounds()` |
| `Panel` | `id()`, `label(labelId, text)`, `bounds()` |
| `TraySlot` | `id()`, `length(int)` / `length()`, `slot(index, slotId, label)`, `bounds()` |
| `Swatch` | `id()`, `color(r,g,b,a)`, `bounds()` |
| `ModalOverlay` | `id()`, `dismiss(dismissId, label)`, `bounds()` |
| `FocusScope` | `add(Button)`, `focusNext` / `focusPrev`, `activateFocused` (Enter/Space semantics on focused **enabled** button), `focusedId()` |
| `UiHit` | `id()`, `activated()` — `UiHit.none()` when miss |
| `UiBounds` | `x`, `y`, `width`, `height`, `centerX` / `centerY`, `contains` — logical top-left space |

### Layout measure (production)

`DefaultUiKit` uses fixed per-character advance (`8f` per glyph) and line height (`16f`). No font file required for layout in production.

## Coordinates: hit vs draw

- **Layout and `UiBounds`** — logical **top-left** origin, **Y down** (same as [`Input`](input.md) pointer: pass `input().getX()` / `getY()` directly to `hit`).
- **Draw** — one **Y-flip** inside the kit so `Draw2D` rects and `Font.drawString` land in backend ortho (typically bottom-left). Authors do not flip coordinates themselves.
- **Min hit** — interactive targets expand to at least **48×48** logical px before `contains`; `hit.activated()` is true only for enabled buttons under the point.

`hit` auto-runs `layout()` if needed. Disabled buttons still report `id` from `hit` but `activated()` is false; `activateFocused` does not record `ACTIVATE` when disabled.

## Null draw arguments

`draw(draw2d, font)` throws `NullPointerException` when `draw2d` or `font` is null (fail-closed, same policy as other engine facades). Load a font via `ui().createFont(...)` and obtain `Draw2D` from `services.draw2d()` before calling `draw`.

## Usage

```java
private EngineServices services;
private Font hudFont;

public void create(EngineServices services) {
  this.services = services;
  hudFont = services.ui().createFont("fonts/hud.fnt");
}

public void render() {
  int w = services.graphics().getWidth();
  int h = services.graphics().getHeight();
  UiKit kit = services.uiKit();
  kit.setTheme(Theme.defaults());
  kit.setViewport(w, h, Insets.ZERO, 1f);

  Button play = kit.button("play", "Play");
  kit.focusScope().add(play);
  kit.layout();

  UiHit hit = kit.hit(services.input().getX(), services.input().getY());
  if (hit.activated() && "play".equals(hit.id())) {
  // handle click
  }

  kit.draw(services.draw2d(), hudFont);
}

public void dispose() {
  if (hudFont != null) {
    hudFont.dispose();
    hudFont = null;
  }
}
```

Hold leaves for the session they belong to; call `kit.clear()` when tearing down a screen tree (Fake records `DISPOSE`). Re-create leaves after `clear()` if the screen returns.

### Modal + tray (shape)

```java
UiKit kit = services.uiKit();
kit.setViewport(w, h, Insets.ZERO, 1f);
Button action = kit.button("go", "Go");
TraySlot tray = kit.traySlot("tray").length(3);
tray.slot(0, "s0", "A");
tray.slot(1, "s1", "B");
tray.slot(2, "s2", "C");
ModalOverlay modal = kit.modalOverlay("modal");
modal.dismiss("ok", "OK");
kit.focusScope().add(action);
kit.layout();
```

## Tests

`FakeEngineServices.uiKit()` returns a recording `FakeUiKit`. Inspect `commands()` for leaf `Kind` values:

`CREATE`, `LAYOUT`, `HIT`, `FOCUS`, `ACTIVATE`, `DISPOSE`, and draw markers `DRAW_BUTTON`, `DRAW_LABEL`, `DRAW_PANEL`, `DRAW_TRAY_SLOT`, `DRAW_SWATCH`, `DRAW_MODAL` (each carries `id` and, on `CREATE`, `leafType`).

**Measure seed (Fake-only):** before `layout()` on Fake, call `seedMeasureAdvance(text, advance)` for every distinct label string the tree measures. Unseeded text throws `EngineException` on layout (fail-closed). Re-seeding the same `text` throws `EngineException`.

**`clear()` vs `clearCommands()`:** `clear()` resets the widget session and appends `DISPOSE` commands. `clearCommands()` wipes the interaction log only — use between test phases without destroying leaves.

**Dual depth on draw:** after `draw(fakeDraw2d, fakeFont)`, assert both `FakeUiKit` draw kinds and sparse paint on `FakeDraw2D` / `FakeUi` (`DRAW_STRING` on the font path). Deeper matrices live in `UiKitTest`.

```java
FakeEngineServices services = new FakeEngineServices();
FakeUiKit kit = services.uiKit();
services.graphics().setSize(800, 600);
kit.setViewport(800, 600, Insets.ZERO, 1f);
kit.seedMeasureAdvance("Play", 48f);
kit.button("play", "Play");
kit.layout();
kit.clearCommands();
// ... hit / focus / draw assertions on kit.commands()
```