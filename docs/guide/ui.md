# Ui

Bitmap font load and draw through `EngineServices.ui()`. Package: `io.github.worldsmiths.core`. No LibGDX types in the contract. Widgets are out of scope.

Loads use packaged **internal** asset paths only (same ceiling as [Files](files.md)).

## Ownership / dispose

`Ui` from the bag is a **factory**, not a disposable resource. Authors own dispose of every `Font` they create (`font.dispose()` when finished). Safe to call more than once.

## API

| Type / method | Meaning |
| --- | --- |
| `createFont(internalPath)` | Load a bitmap font; returns an author-owned `Font` |
| `Font.drawString(text, x, y)` | Draw text (best-effort; must not fail the game) |
| `Font.dispose()` | Release native resources when finished |

Missing or unloadable paths throw unchecked `EngineException` with a message that includes the path. Do not call `drawString` on a failed create. SpriteBatch is not part of this API.

## Usage

```java
private Font hud;

public void create(EngineServices services) {
  hud = services.ui().createFont("fonts/hud.fnt");
}

public void render() {
  hud.drawString("1:23", 8f, 24f);
}

public void dispose() {
  if (hud != null) {
    hud.dispose();
    hud = null;
  }
}
```

## Honesty limits

- **Draw is best-effort.** Device or GPU glitches must not throw out of the facade to fail the game.
- **Internal paths only.** Same packaging rules as Files — classpath / assetsDir; no external disk paths through this facade.
- Coordinate system / ortho are backend-defined (typically screen pixels with origin at bottom-left).
- No widgets / layout API in this slice.

## Miss behaviour

```java
// throws EngineException — message contains the path
services.ui().createFont("does-not-exist.fnt");
```

## Tests

`FakeEngineServices.ui()` returns `FakeUi`. Seed with `putFont(path)`, then assert command kinds (`CREATE_FONT`, `DRAW_STRING`, `DISPOSE`) via `commands()` without a display.
