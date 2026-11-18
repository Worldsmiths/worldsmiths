# Draw2D

Immediate-mode 2D overlay drawing through `EngineServices.draw2d()`. Package: `io.github.worldsmiths.core`. No LibGDX types in the contract.

Use this for author-drawn grids, debug overlays, and simple shapes. Retained sprites still go through `Scene` / `SceneWalk` (backends draw them via their scene renderer).

## Ownership / dispose

The bag's `Draw2D` is **bag-owned**. Games must call `services.draw2d().dispose()` once at shutdown (GDX-backed impls own a `ShapeRenderer`). Do not create a second Draw2D; reuse the bag instance.

## API

| Method | Meaning |
| --- | --- |
| `setOrtho2D(x, y, width, height)` | Screen-space ortho (origin bottom-left) |
| `setColor(r, g, b, a)` | Color for subsequent `line` / `rect` |
| `begin(DrawMode mode)` | Start a batch (`LINE` or `FILLED`) |
| `line(x1, y1, x2, y2)` | Stroke a line segment |
| `rect(x, y, width, height)` | Draw a rect in the current mode |
| `end()` | End the current batch |
| `dispose()` | Release backend resources (call once at shutdown) |

## Usage

```java
public void create(EngineServices services) {
  this.services = services;
}

public void render() {
  Draw2D g = services.draw2d();
  g.setOrtho2D(0f, 0f, services.graphics().getWidth(), services.graphics().getHeight());
  g.begin(DrawMode.LINE);
  g.setColor(0.25f, 0.25f, 0.28f, 1f);
  g.line(0f, 0f, 320f, 0f);
  g.end();
}

public void dispose() {
  services.draw2d().dispose();
}
```

Hold the bag from `create(EngineServices)`. Pair every `begin` with `end`. Dispose the bag's Draw2D when the game shuts down.

## Tests

`FakeEngineServices.draw2d()` returns a recording `FakeDraw2D`: inspect `lastOrthoWidth` / `lastOrthoHeight`, `commands()`, and `disposed()`.
