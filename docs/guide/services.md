# Services overview

Games receive a non-null `EngineServices` bag from the backend via `WorldsmithsGame.create(EngineServices)` after `createScene()`.

Launchers stay `new Game()` — backends assemble the bag (GDX-backed impls). Headless tests pass `FakeEngineServices`.

Package: `io.github.worldsmiths.core`.

## Getters (shipped)

| Getter | Type | Role |
| --- | --- | --- |
| `input()` | [`Input`](input.md) | Poll keys (`Key` enum) + primary pointer |
| `clock()` | [`Clock`](clock.md) | Frame `delta()` + `total()` |
| `graphics()` | [`Graphics`](graphics.md) | Size + best-effort `setTitle` |
| `draw2d()` | [`Draw2D`](draw2d.md) | Immediate-mode lines/rects + ortho |
| `files()` | [`Files`](files.md) | Sync packaged internal text/bytes |
| `thin3d()` | [`Thin3D`](thin3d.md) | Camera, lights, primitive meshes, batch |
| `audio()` | [`Audio`](audio.md) | One-shot sound load / play / stop / dispose |
| `ui()` | [`Ui`](ui.md) | Bitmap font load / draw / dispose |
| `textures()` | [`Textures`](textures.md) | Sync preload / resolve; engine-owned cache |
| `log()` | [`Log`](log.md) | Portable debug / info / warn / error |
| `uiKit()` | [`UiKit`](uikit.md) | Session widgets — layout, hit, focus, draw |
| `extensions()` | `EngineServices.Extensions` | Empty stub; unused in D9 |

## Honesty limits

- `graphics().setTitle(...)` is best-effort; Android may ignore it.
- Files on web = internal packaged only (see [Files](files.md)).

## Tests

```java
FakeEngineServices services = new FakeEngineServices();
services.input().setKeyDown(Key.LEFT, true);
services.clock().setDelta(1f / 60f);
services.graphics().setSize(800, 480);
services.files().putInternal("levels/one.txt", "hello");
game.create(services);
game.render(); // asserts against services.draw2d() recordings
```
