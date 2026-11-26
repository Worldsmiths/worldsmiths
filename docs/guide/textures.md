# Textures

Sync texture load/cache from packaged **internal** paths through `EngineServices.textures()`. Package: `io.github.worldsmiths.core`. No LibGDX types in the contract. Async / AssetManager loading is out of scope.

Loads use packaged **internal** asset paths only (same ceiling as [Files](files.md)).

## Ownership / dispose

The engine owns the texture cache for the listener / bag lifetime. Authors call `preload` / `resolve` to warm or ensure a key is cached; they do **not** dispose individual GPU textures. Backends dispose the cache when the listener shuts down.

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

Paths use forward slashes and match how assets are packaged for the sample (typically under the game `assets/` tree). Retained scene draw uses the same cache by `Sprite.texture` key.

## Honesty limits

- **Sync only.** No AssetManager / async load API in this slice.
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
