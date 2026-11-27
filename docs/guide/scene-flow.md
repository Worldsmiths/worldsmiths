# Scene flow

Replace-only scene load/switch/unload through `EngineServices.scenes()`. Package: `io.github.worldsmiths.core`. No LibGDX types in the contract.

Use this instead of a sticky `createScene` retain (removed). The first scene becomes current inside `create(EngineServices)` via `load` or `switchTo`. Backends draw and update `scenes().current()` each frame (null-safe when unloaded).

Demo: [`samples/flow`](../../samples/flow/) — stub menu → gameplay → room_b via three packaged SceneCodec JSONs (`assets/scenes/menu.json`, `gameplay.json`, `room_b.json`). SPACE / ENTER / touch advances one hop per rising edge.

## API

| Method | Meaning |
| --- | --- |
| `load(path)` | Sync path helper: read → SceneCodec decode → preload distinct Sprite texture keys → exit previous → enter new → set current |
| `switchTo(scene)` | Replace with an already-built `Scene` (no Files/Codec; **no** sync texture preload — path `load` owns preload) |
| `unload()` | Exit current (if any) and clear `current()` to null |
| `current()` | Active scene, or null before the first successful load/switch and after `unload` |
| `setRegistry(registry)` | Component registry for path `load` / SceneCodec; defaults to `ComponentRegistries.builtins()` |
| `addTeardownHook(hook)` | Register a `SceneTeardownHook` invoked when a scene exits (replace or unload). List starts empty — seam for ticket 14 (ECS bake) |

Paths are packaged **internal** asset paths (forward slashes; same ceiling as [Files](files.md)).

## Usage

```java
public void create(EngineServices services) {
  this.services = services;
  // Optional: custom registry before the first path load
  services.scenes().setRegistry(ComponentRegistries.builtins());
  services.scenes().load("scenes/menu.json");
}

public void update(float delta) {
  // Later hop (e.g. stub menu activate):
  services.scenes().load("scenes/gameplay.json");
  // Or adopt an in-memory tree (author preloads textures if needed):
  // services.scenes().switchTo(built);
}
```

Lifecycle on a successful replace: exit previous (if any) → run teardown hooks → set current → enter new. Enter completes before `load` / `switchTo` returns. Loading the same path again is a full replace (exit + enter), not a cached no-op.

After `unload()`, `current()` is null and retained SceneWalk / listener draw yields no sprite commands.

## Fail keep previous

Missing file or undecodable JSON throws `EngineException` (message includes the path; cause may wrap codec/files failure). There is **no** exit/swap: `current()` stays the previous scene. Retry by calling `load` again.

```java
try {
  services.scenes().load("scenes/missing.json");
} catch (EngineException e) {
  // current() unchanged; log and retry or stay
  services.log().warn("Scenes", e.getMessage());
}
```

Texture preload failures during path `load` also fail closed (same throw; previous current kept) — see [Textures](textures.md).

## Sync preload on path load

`load(path)` walks the decoded tree for distinct non-blank `Sprite.texture` keys and calls `textures().preload` for each **before** the scene becomes current. `switchTo` does not preload — warm keys yourself or use path `load`.

Textures stay in the engine-owned cache across hops; this slice does **not** evict per scene (see honesty).

## Enter / exit and teardown hooks

Fake and production impls run exit then enter around every successful replace. `addTeardownHook` registers callbacks that run on exit (replace or unload), after the exit lifecycle step and before current is cleared or replaced.

The hook list starts empty. Ticket 14 owns ECS bake through this seam — do not invent bake behaviour here.

```java
services.scenes().addTeardownHook(leaving -> {
  // ticket 14: bake / release scene-owned resources
});
```

Do not keep stale `Node` / component references across a replace — rebind from `current()` after a hop.

## Honesty limits

- **Replace only.** One `current()` scene. No additive / stack scenes; pause overlays stay UiKit modal later.
- **No visual transitions.** Instant swap — no fades or cross-dissolves.
- **No async-gated switch.** Path load is sync (JSON + texture preload may hitch). Compose [`assets()`](assets.md) yourself if you need non-blocking warm-up before calling `load` / `switchTo`.
- **No per-scene texture eviction.** Cache grows with distinct keys across hops until the listener/bag tears down.
- **No ECS bake in this slice.** Teardown hooks are the empty seam; ticket 14 owns bake.
- **`createScene` is gone.** First scene loads in `create` via `scenes()`; backends never retain a create-time scene identity.

## Tests

`FakeEngineServices.scenes()` returns `FakeScenes`. Assert ordered `commands()` kinds (`LOAD`, `SWITCH`, `UNLOAD`, `EXIT`, `ENTER`, `FAIL_KEEP_PREVIOUS`) without a display. Seed scene JSON with `files().putInternal(...)`, texture keys with `textures().putTexture(...)`, then `load` / `switchTo` / `unload` and check `current()`. Call `clear()` between cases when reusing one fake.

See `samples/flow` (`FlowGameTest`) for menu → gameplay → room_b Fake hops (≥2 replaces after the initial load).
