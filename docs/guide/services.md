# Services overview

Games receive a non-null `EngineServices` bag from the backend via `WorldsmithsGame.create(EngineServices)` after `createScene()`.

Launchers stay `new Game()` — backends assemble the bag (real GDX-backed impls). Headless tests pass `FakeEngineServices`.

## Floor (D9a)

| Getter | Role | Slice |
| --- | --- | --- |
| `input()` | Poll keys / pointers | S1 |
| `clock()` | Delta + optional total time | S1 |
| `graphics()` | Size + best-effort `setTitle` | S1 |
| `draw2d()` | Imperative begin/line/rect/end | S2 |
| `files()` | Sync internal packaged read | S3 |

Thin unused `extensions()` stub reserved for later plugins / ECS — unused in D9.

## Honesty limits

- `graphics().setTitle(...)` is best-effort; Android ignores it.
- Files on web = internal packaged only (documented when Files ships).
