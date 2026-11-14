# `@WsComponent` field-type matrix (D4)

Companion to [ADR-0007](adr/0007-teavm-component-registry.md). Authors of engine and game components need this matrix before D5 TeaVM (`reflectionEnabled=false`).

The annotation processor (`:processor`) serializes only the field types below. Anything else is a **compile error**.

## Author wiring (registry + `SceneCodec`)

Authors only need `@WsComponent` and `SceneCodec`. Do not call `ComponentType.serializer()` or hand-wire libGDX `Json.Serializer` instances.

### Engine builtins (`ws-core`)

- Built-ins (`Transform2D`, `Sprite`, …) are annotated in `ws-core` and processed with:
  - `annotationProcessor(project(":processor"))` (or the published processor coordinate once released)
  - `-Aws.registry.package=io.github.worldsmiths.core`
- Generated FQCN: `io.github.worldsmiths.core.GeneratedComponentRegistry`
- Prefer the entry point: `ComponentRegistries.builtins()` (same instance as `GeneratedComponentRegistry.INSTANCE`)

```java
SceneCodec codec = SceneCodec.create(ComponentRegistries.builtins());
```

### Game modules (custom components)

1. Depend on `ws-core` and add the processor:
   - `annotationProcessor(project(":processor"))` in this monorepo, or the published processor artifact
2. Set a **module-owned** registry package so it does not collide with the engine class name:
   - e.g. `-Aws.registry.package=com.example.game`
3. Generated FQCN: `com.example.game.GeneratedComponentRegistry` (package from the option above; simple name is always `GeneratedComponentRegistry`)
4. **Combine** with builtins before creating the codec — a game registry does not include engine types:

```java
ComponentRegistry registry = ComponentRegistry.combine(
        ComponentRegistries.builtins(),
        com.example.game.GeneratedComponentRegistry.INSTANCE);
SceneCodec codec = SceneCodec.create(registry);
```

`combine` unions types in order and throws if the same tag appears more than once.

## Supported fields

| Java type | JSON shape | Notes |
| --- | --- | --- |
| `boolean` | JSON boolean | |
| `int` | JSON number | |
| `long` | JSON number | |
| `float` | JSON number | |
| `double` | JSON number | |
| `String` | JSON string or `null` | Missing fields keep the no-arg constructor default |

## Skipped fields

| Modifier | Behaviour |
| --- | --- |
| `static` | Not written or read |
| `transient` | Not written or read |

## Rejected at compile time

The processor fails the build when a non-skipped field is:

- `private` (use package-private or `public`, or mark `transient`)
- `final` (must be assignable on deserialize)
- an unsupported type (arrays, collections, enums, nested value types such as `Vector2` / `Color`, inheritance of fields from a superclass)

The class itself must be `public`, non-abstract, top-level or static nested, and have a public no-arg constructor.

## Out of scope for D4

Nested value types, arrays/collections, enums, asset references beyond a plain `String` key, inherited fields, and editor property schema are deferred (see ADR-0007 consequences).
