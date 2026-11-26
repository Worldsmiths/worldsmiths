# Assets

Async packaged-asset load through `EngineServices.assets()`. Package: `io.github.worldsmiths.core`. No LibGDX types in the contract.

Use this when a heavy packaged texture should not block the frame for the whole open/decode. Sync warm/resolve stays on [`textures()`](textures.md).

Loads use packaged **internal** asset paths only (same ceiling as [Files](files.md) / [Textures](textures.md)).

## API

| Method | Meaning |
| --- | --- |
| `request(path, kind)` | Enqueue (or share) a load; returns a non-null `LoadHandle` |

`request` never throws for a missing, unloadable, or unsupported path/kind — observe `LoadState.FAILED` on the handle instead.

### LoadHandle

| Method | Meaning |
| --- | --- |
| `path()` / `kind()` | Echo of the request |
| `state()` | Discrete `QUEUED` / `LOADING` / `COMPLETE` / `FAILED` |
| `progress()` | Coarse progress in `[0, 1]` (not byte-level) |
| `failureMessage()` | Detail when `FAILED`; otherwise null/empty |
| `onTerminal(Runnable)` | Optional once-listener for `COMPLETE` or `FAILED` |

`onTerminal` for loads that enqueue (`QUEUED` / `LOADING`) fires on the game-thread pump path (after the frame pump, before `game.update`). Immediate terminals (null/blank path, MESH/MUSIC stub, already-cached COMPLETE) become terminal during `request` on the calling thread — registering `onTerminal` afterward may run the listener on that same calling thread, not only after pump. Poll `state()` / `progress()` if you prefer not to register a listener.

Re-requesting the same `(kind, path)` is idempotent: shares an in-flight handle or returns `COMPLETE` if already cached.

### AssetKind

| Kind | Status |
| --- | --- |
| `TEXTURE` | Supported (Fake + GDX hybrid) |
| `MESH` | Stub — handle reaches `FAILED` with an unsupported-kind message (ticket 05) |
| `MUSIC` | Stub — handle reaches `FAILED` with an unsupported-kind message (ticket 07) |

## Usage

```java
public void create(EngineServices services) {
  LoadHandle h = services.assets().request("textures/heavy.png", AssetKind.TEXTURE);
  h.onTerminal(() -> {
    if (h.state() == LoadState.COMPLETE) {
      services.log().info("Assets", "heavy texture ready");
      // textures().resolve hits the same cache — no second open/decode
      services.textures().resolve("textures/heavy.png");
    } else if (h.state() == LoadState.FAILED) {
      services.log().warn("Assets", h.failureMessage());
    }
  });
  services.log().info("Assets", "requested textures/heavy.png");
}
```

Paths use forward slashes and match how assets are packaged (typically under the game `assets/` tree). The `asyncload` sample requests `textures/heavy.png` and emits load signals through [`log()`](log.md) (info on start/complete; warn on fail) — it polls `state()` in `update`, which is equally valid next to `onTerminal`.

## Single cache with textures

On `TEXTURE` `COMPLETE`, the backend merges into the same engine-owned texture cache that [`textures()`](textures.md) uses. After complete, `textures().resolve(path)` is a cache hit — no second FileHandle open/decode.

While a key is in-flight (async request not yet terminal), retained scene draw **skips** that sprite — it does **not** fall back to a sync FileHandle→Texture hitch for that key. Sync `textures().preload` / `resolve` for that same key also fail-closed with `EngineException` (message notes async load in flight) — backends reject a sync open that would dual-load.

## Fail vs sync throw

| Path | Miss / unloadable behaviour |
| --- | --- |
| `assets().request(...)` | Handle → `FAILED`; `request` itself does not throw |
| `textures().preload` / `resolve` | Unchecked `EngineException` (unchanged) |

```java
// does not throw — poll or onTerminal for FAILED (message contains the path)
LoadHandle miss = services.assets().request("does-not-exist.png", AssetKind.TEXTURE);

// still throws EngineException — message contains the path
services.textures().preload("does-not-exist.png");
```

## Hybrid backends

Author code always calls `assets().request`. Backends differ:

| Platform | Assembly | Behaviour |
| --- | --- | --- |
| Desktop / Android / iOS | `GdxJvmEngineServices` + LibGDX `AssetManager` | Prepare can run off the GL thread; one pump per frame before `game.update` |
| Web (TeaVM) | `GdxEngineServices.createCooperative()` | Cooperative queue — at most one heavy open/decode step per frame |

**Honesty:** web may hitch for one heavy cooperative step in a frame. That is documented cooperative progress, not claimed parallel decode. JVM platforms use real AssetManager prepare threads.

## Cancel / dispose

There is **no author cancel API**. Listener / bag dispose tears down in-flight loads safely (idempotent). Retries after `FAILED` are a new `request` (safe; shares cache when already complete).

## Logging

The engine does **not** emit `log()` lines on handle `FAILED` by default. Authors (and the `asyncload` sample) use [`services.log()`](log.md) for start / complete / fail signals.

## Honesty limits

- **Internal paths only.** Same packaging rules as Files / Textures — classpath / assetsDir; no external disk, CDN, or Addressables roots through this facade.
- **No author cancel.** Dispose tears down in-flight; no mid-load cancel handle.
- **Coarse progress only.** Discrete states ± `progress()` in `[0, 1]`; not byte-level.
- **MESH / MUSIC stubs.** Request reaches `FAILED` with unsupported-kind until tickets 05 / 07.
- **Web cooperative hitch.** One heavy step per frame may stall that frame; not free parallel load.
- **In-flight draw skip.** No sync hitch fallback for an async in-flight texture key.
- **Sample-only FAILED logging.** Engine stays quiet on fail unless the game calls `log()`.

## Tests

`FakeEngineServices.assets()` returns `FakeAssets`. Seed with `putTexture(path)`, then call `pump()` explicitly — no sleeps. Assert `LoadState` transitions, `failureMessage` path/kind text, `onTerminal` order (after pump, before a Fake update marker), and command recordings. Missing TEXTURE → `FAILED` without throw; `MESH` / `MUSIC` → unsupported `FAILED`.
