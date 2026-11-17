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
| `extensions()` | `EngineServices.Extensions` | Empty stub; unused in D9 |

`draw2d()` and `files()` are **not** on the bag yet — they land with their slices (see below).

## Upcoming

| Facade | Slice | Guide |
| --- | --- | --- |
| Draw2D | D9a-S2 | `draw2d.md` (upcoming) |
| Files | D9a-S3 | `files.md` (upcoming) |
| Thin 3D | D9b | later |

## Honesty limits

- `graphics().setTitle(...)` is best-effort; Android may ignore it.
- Files on web = internal packaged only (documented when Files ships).

## Tests

```java
FakeEngineServices services = new FakeEngineServices();
services.input().setKeyDown(Key.LEFT, true);
services.clock().setDelta(1f / 60f);
services.graphics().setSize(800, 480);
game.create(services);
```
