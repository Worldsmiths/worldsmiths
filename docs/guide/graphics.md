# Graphics

Framebuffer size and a best-effort window title via `EngineServices.graphics()`. Package: `io.github.worldsmiths.core`. This is meta only — not a draw API (see [Draw2D](draw2d.md)).

## API

| Method | Meaning |
| --- | --- |
| `getWidth()` / `getHeight()` | Backbuffer / window size in pixels |
| `setTitle(String title)` | Best-effort window title |

## Honesty

`setTitle` may be applied on desktop/web and **ignored on Android** (and other platforms without a window title). It never throws for an unsupported platform — treat it as fire-and-forget UI chrome, not a required side effect.

## Usage

```java
public void create(EngineServices services) {
  this.services = services;
  Graphics g = services.graphics();
  g.setTitle("My game");
}

public void update(float delta) {
  Graphics g = services.graphics();
  int w = g.getWidth();
  int h = g.getHeight();
  g.setTitle("Score: " + score); // best-effort
}
```

## Tests

`FakeEngineServices.graphics()` returns a mutable `FakeGraphics`: `setSize`, plus `lastTitle()` for the most recent `setTitle` argument.
