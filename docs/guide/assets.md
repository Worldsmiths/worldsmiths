# Assets

Async packaged-asset load through `EngineServices.assets()`. Package: `io.github.worldsmiths.core`. No LibGDX types in the contract.

Use this when a heavy packaged asset should not block the frame for the whole open/decode — **textures** and **glTF meshes** today. Sync warm/resolve stays on [`textures()`](textures.md). File meshes bind into drawable [`Model`](d3.md) only after MESH `COMPLETE` via [`d3().createModelFromLoaded`](d3.md).

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

`onTerminal` for loads that enqueue (`QUEUED` / `LOADING`) fires on the game-thread pump path (after the frame pump, before `game.update`). Immediate terminals (null/blank path, already-cached COMPLETE, **MUSIC stub**) become terminal during `request` on the calling thread — registering `onTerminal` afterward may run the listener on that same calling thread, not only after pump. Poll `state()` / `progress()` if you prefer not to register a listener.

Re-requesting the same `(kind, path)` is idempotent: shares an in-flight handle or returns `COMPLETE` if already cached.

### AssetKind

| Kind | Status |
| --- | --- |
| `TEXTURE` | Supported (Fake + GDX hybrid) |
| `MESH` | Supported — glTF (`.gltf` + external images); completes into a private backend registry; bind via [`d3().createModelFromLoaded`](d3.md) after `COMPLETE` |
| `MUSIC` | Stub — handle reaches `FAILED` with an unsupported-kind message (ticket 07) |

### MESH packaging honesty

- Prefer **`.gltf` with external image files`** packaged beside the glTF under `assets/` — portable desktop **and** web; **embedded GLB is not required** (and is not the portable AC path).
- **Out of this slice:** `.obj` / `.mtl` (05i), embedded-only GLB workflows, PBR texture slots / metalness-roughness-normal (05h), procedural mesh generation (05d), SceneCodec mesh components (05f).

## Usage

### TEXTURE

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

### MESH → D3

```java
LoadHandle mesh = services.assets().request("meshes/demo.gltf", AssetKind.MESH);
mesh.onTerminal(() -> {
  if (mesh.state() == LoadState.COMPLETE) {
    Model m = services.d3().createModelFromLoaded("meshes/demo.gltf");
    // createInstance, setDiffuse/setEmissive, ModelBatch — see D3 guide
  } else {
    services.log().warn("Assets", mesh.failureMessage());
  }
});
```

Paths use forward slashes and match how assets are packaged (typically under the game `assets/` tree). The `asyncload` sample requests `textures/heavy.png` and emits load signals through [`log()`](log.md). The **`mesh3d`** sample (`samples/mesh3d`) requests a packaged glTF + images, pumps to `COMPLETE`, binds, and toggles diffuse/emissive for UAT — polls `state()` in `update` are equally valid next to `onTerminal`.

## Single cache with textures

On `TEXTURE` `COMPLETE`, the backend merges into the same engine-owned texture cache that [`textures()`](textures.md) uses. After complete, `textures().resolve(path)` is a cache hit — no second FileHandle open/decode.

While a key is in-flight (async request not yet terminal), retained scene draw **skips** that sprite — it does **not** fall back to a sync FileHandle→Texture hitch for that key. Sync `textures().preload` / `resolve` for that same key also fail-closed with `EngineException` (message notes async load in flight) — backends reject a sync open that would dual-load.

MESH `COMPLETE` does **not** merge into a public `meshes()` bag — only a private registry feeding `createModelFromLoaded`.

## Fail vs sync throw

| Path | Miss / unloadable behaviour |
| --- | --- |
| `assets().request(...)` | Handle → `FAILED`; `request` itself does not throw |
| `textures().preload` / `resolve` | Unchecked `EngineException` (unchanged) |
| `d3().createModelFromLoaded(...)` | Unchecked `EngineException` before MESH `COMPLETE` or for unknown path (unchanged sync-throw shape) |

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
| Desktop / Android / iOS | `GdxJvmEngineServices` + LibGDX `AssetManager` | Prepare can run off the GL thread; one pump per frame before `game.update` (textures + mesh queue share the pump) |
| Web (TeaVM) | `GdxEngineServices.createCooperative()` | Cooperative queue — at most one heavy open/decode **or mesh parse/GPU build** step per frame |

**Honesty:** web may hitch for one heavy cooperative step in a frame — including glTF parse/build when MESH advances. That is documented cooperative progress, not claimed parallel decode. JVM platforms use real AssetManager prepare threads for textures; mesh loads use gdx-gltf behind `core.gdx` only (author sources stay GDX-free). The hybrid factory keeps **AssetManager / AsyncExecutor off the web-reachable graph**.

## Fake vs GDX harness

| Backend | Seed / assert |
| --- | --- |
| `FakeAssets` | `putTexture` / `putMesh`, then explicit `pump()` — no sleeps. MESH miss → `FAILED` + message; seeded MESH → `COMPLETE` into private registry |
| GDX | Real packaged assets under sample `assets/`; headless tests cover load/bind where the repo allows |

Game mains must not import `com.badlogic.gdx` or `net.mgsx.gltf` — loaders live behind the firewall (`samples/mesh3d` gate).

## Cancel / dispose

There is **no author cancel API**. Listener / bag dispose tears down in-flight loads safely (idempotent). Retries after `FAILED` are a new `request` (safe; shares cache when already complete). Author `Model.dispose()` from bind drops the wrapper; registry GPU lifetime follows bag dispose rules on GDX.

## Logging

The engine does **not** emit `log()` lines on handle `FAILED` by default. Authors (and samples) use [`services.log()`](log.md) for start / complete / fail signals.

## Honesty limits

- **Internal paths only.** Same packaging rules as Files / Textures — classpath / assetsDir; no external disk, CDN, or Addressables roots through this facade.
- **No author cancel.** Dispose tears down in-flight; no mid-load cancel handle.
- **Coarse progress only.** Discrete states ± `progress()` in `[0, 1]`; not byte-level.
- **MUSIC stub.** Request reaches `FAILED` with unsupported-kind until ticket 07.
- **Web cooperative hitch.** One heavy step per frame may stall that frame (texture decode or mesh step); not free parallel load.
- **In-flight draw skip.** No sync hitch fallback for an async in-flight texture key.
- **glTF scope.** Minimal materials via instance `setDiffuse` / `setEmissive` on bound meshes — not full PBR (05h) or custom GLSL (05e).
- **Sample-only FAILED logging.** Engine stays quiet on fail unless the game calls `log()`.

## Tests

`FakeEngineServices.assets()` returns `FakeAssets`. Seed with `putTexture(path)` or `putMesh(path)`, then call `pump()` explicitly — no sleeps. Assert `LoadState` transitions, `failureMessage` path/kind text, `onTerminal` order (after pump, before a Fake update marker), and command recordings. Missing TEXTURE or MESH → `FAILED` without throw; `MUSIC` → unsupported `FAILED`. Pair MESH `COMPLETE` with `FakeD3.createModelFromLoaded` / material setter commands in [`D3`](d3.md) tests.