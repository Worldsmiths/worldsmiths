# Textures

Sync texture load/cache from packaged **internal** paths through `EngineServices.textures()`. Package: `io.github.worldsmiths.core`. No LibGDX types in the contract.

For non-blocking packaged loads, use [`assets().request`](assets.md) — completes merge into this same cache.

Loads use packaged **internal** asset paths only (same ceiling as [Files](files.md)).

## Ownership / dispose

The engine owns the texture cache for the listener / bag lifetime. Authors call `preload` / `resolve` to warm or ensure a key is cached; they do **not** dispose individual GPU textures. Backends dispose the cache (and tear down in-flight async loads) when the listener shuts down.

## API

| Method | Meaning |
| --- | --- |
| `preload(internalPath)` | Sync-warm the cache for a packaged internal path |
| `resolve(internalPath)` | Sync-resolve into the cache (loads on miss) |

Both methods are idempotent — repeated calls for the same key hit the engine-owned cache. Missing or unloadable paths throw unchecked `EngineException` with a message that includes the path (fail-closed; no half-loaded textured scene).

## Usage

```java
public void create(EngineServices services) {
  services.textures().preload("sprites/hero.png");
  // or resolve on demand when a Sprite.texture key is known:
  services.textures().resolve("sprites/tile.png");
}
```

Paths use forward slashes and match how assets are packaged for the sample (typically under the game `assets/` tree). Retained scene draw uses the same cache by `Sprite.texture` key. After an async [`assets()`](assets.md) TEXTURE reaches `COMPLETE`, `resolve` for that path is a cache hit (no second open/decode). While that key is still in-flight, retained draw skips the sprite — it does not sync-load as a hitch fallback.

## Honesty limits

- **Sync miss still throws.** `preload` / `resolve` throw `EngineException` on miss; async miss is observe-`FAILED` via [`assets()`](assets.md), not a throw on `request`.
- **Single cache with async.** Async TEXTURE completes merge here; do not expect a second GPU copy for the same path.
- **In-flight skip.** Retained draw skips async in-flight keys — no sync FileHandle→Texture for that key mid-load.
- **Internal paths only.** Same packaging rules as Files — classpath / assetsDir; no external disk paths through this facade.
- **Engine-owned cache.** Do not expect an author-facing dispose for retained sprite textures; lifecycle follows the backend listener.

## Miss behaviour

```java
// throws EngineException — message contains the path
services.textures().preload("does-not-exist.png");
services.textures().resolve("does-not-exist.png");
```

## Tests

`FakeEngineServices.textures()` returns `FakeTextures`. Seed with `putTexture(path)`, then assert command kinds (`CREATE`, `PRELOAD`, `RESOLVE`) via `commands()` without a display or GPU. Missing paths throw `EngineException`.
