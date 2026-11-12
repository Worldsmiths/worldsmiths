# ADR-0007: Build-time component registry with generated libGDX Json serializers

- Status: accepted
- Date: 2026-11-13
- Deciders: founder
- Related: Spike S2 (#5), FR-S06, FR-B10, NFR-10, ADR-0004, ADR-0005, HLD §5.2, [R02 §2.4, risks 3 and 4]

## Context

FR-S06 asks for component (de)serialization without reflection, driven by registries generated at build time. NFR-10 bans runtime reflection and classpath scanning in runtime modules. ADR-0004 fixes engine and game code at Java 8 syntax with Java 7 APIs. The web target is TeaVM through gdx-teavm (FR-B10), and TeaVM only supports reflection for classes that are declared at build time. On gdx-teavm 1.6.x that means `reflection("pkg.**")` patterns. On the TeaVM 0.16 snapshot line it means a `ReflectionPolicy`. FR-B10 currently assumes the engine will generate that reflection configuration.

Spike S2 asked: can an annotation-processor registry plus libGDX `Json` serialize components on gdx-teavm with no reflection policy hacks? Done when a browser shows a deserialized scene.

libGDX `Json` (1.14.2) only reflects when it has to. `readValue` resolves a type tag through `addClassTag` before it tries `ClassReflection.forName`, and it uses a registered `Json.Serializer` before `newInstance` and field scanning. `writeValue` also checks registered serializers first. If every type on the scene path has a serializer and a tag, `Json` never reaches its reflective code.

## Options considered

1. **Annotation processor that generates one `Json.Serializer` per component plus a registry of tags, class literals and no-arg factories.** The output is plain Java that TeaVM, RoboVM and the JVM all compile the same way. Costs: a processor to maintain, and component fields limited to the types it supports.
2. **Keep libGDX `Json`'s reflective path and generate gdx-teavm reflection configuration** (`reflection(...)` patterns now, a `ReflectionPolicy` on TeaVM 0.16). Least engine code. It ties the web build to gdx-teavm's reflection model, which changes between TeaVM releases [R02 risk 3]. It does nothing for RoboVM tree-shaking, and it keeps reflection metadata in the output.
3. **Hand-written `Json.Serializer` or `Json.Serializable` per component.** No tooling, but boilerplate in every game class, field lists that drift from the class, and no schema for the editor.

## Decision

Option 1, accepted on the spike's evidence. M2 builds FR-S06 on an annotation processor that generates per-component `Json.Serializer`s and a `GeneratedComponentRegistry`. Engine scene serialization registers every type it writes, so libGDX `Json` never falls back to reflection.

### Evidence

Code: `spikes/teavm-registry/` (standalone Gradle build, not included from the root build). CI: `.github/workflows/spike-teavm.yml`, which runs only for changes under the spike and is not a required check.

Versions, checked on Maven Central on 2026-11-13: libGDX **1.14.2** (latest release), gdx-teavm **1.6.2** (latest release, plugin `com.github.xpenatan.gdx-teavm`, published on Maven Central rather than the Plugin Portal), which brings in TeaVM **0.15.0**. TeaVM 0.16.0 is released, but no gdx-teavm release targets it yet. Gradle 9.9.1 (the repo wrapper), JDK 21 in CI (JDK 24 locally), headless Google Chrome driven by playwright-core 1.65.0.

- **Processor (JVM, written test-first, 16 tests).** `WsComponentProcessorTest` compiles sources with javac and the processor at `--release 8`. It checks: the registry lists components sorted by tag, with class literals and fresh instances from the factories; every supported field type round-trips (boolean, int, long, float, double, String, including null and escaped strings); missing JSON fields keep constructor defaults; static and transient fields are skipped; package-private fields work; `@WsComponent("Tag")` overrides the tag; sources in the same compilation can reference the registry; generated code contains no `forName`, `reflect`, `getDeclared*`, `newInstance`, `getField` or `getMethod`; and clear compile errors for private fields, final fields, unsupported types, a missing public no-arg constructor, non-public or abstract classes, and duplicate tags.
- **Round-trip (JVM, written test-first, 5 tests).** `SceneRoundTripTest` serializes a two-node scene with a child node through `SceneJson` and the generated registry, deserializes it, and asserts every `Transform2D` and `Sprite` value. It also checks that JSON tags are registry tags (`"type":"Transform2D"`, no class names) and that an unknown tag throws `Unknown component type '…'` instead of reaching `Class.forName`.
- **Source guard (2 tests).** `NoReflectionTest` scans runtime, game, generated and web launcher sources for reflection APIs. It also requires `reflectionEnabled.set(false)` and `reflectionDefaults.set(false)`, no `reflection(...)` patterns, and no reflection config file in the web module.
- **TeaVM in a browser.** The web module sets `reflectionEnabled = false` and `reflectionDefaults = false`, so gdx-teavm generates no reflection metadata, not even its libGDX defaults. In `ApplicationListener.create()` the TeaVM-compiled program runs the same `DemoScene.roundTrip()` and `verify()` used by the JVM tests, then writes the decoded scene into `<pre id="ws-scene">` and sets the page title. `browser-check/check.mjs` loads the **JavaScript and the Wasm (GC) builds** in headless Chrome and requires `WS-S2-RESULT: PASS`, the decoded values (for example `Transform2D x=12.5 y=-3.25 rotation=90.0` and `Sprite texture=hero.png layer=2 alpha=0.75 visible=true`), and the title `WS-S2 PASS`. Both builds pass. A local run's screenshot and text dump are in `spikes/teavm-registry/evidence/`.
- **Negative control.** The same page also reads `Transform2D` through a plain `new Json()` with no registry. Under TeaVM this fails with `SerializationException: Field not found: x`, while the registry path passes in the same build. The pass therefore does not depend on reflection metadata.

## Consequences

What M2 should copy from the spike:

- **Processor shape.** Generate one `<Name>_WsSerializer` per component in the component's own package, so package-private fields work. Generate a single `GeneratedComponentRegistry` (tag, class literal, anonymous-class factory, serializer) in a package set by an `-A` option. Write the registry **in the round after the last component appears, not in the final round**: javac does not let same-compilation sources see types created in the final round, and that broke the first version. Declare the processor `aggregating` in `META-INF/gradle/incremental.annotation.processors`. Generated code uses anonymous classes, not lambdas, and only Java 7 APIs, so it suits RoboVM 2.3.
- **Scene format rule.** Every type on the scene path (scene, node, components) gets an explicit serializer, and tags come from the registry. An unknown tag is an error from engine code, never a `Class.forName` fallback.
- **Web module defaults.** Generated web modules set `reflectionEnabled = false` by default. The `NoReflectionTest`-style guard and the browser check with its negative control become an M2 CI job. The phrase "plus generated reflection policy" in FR-B10 becomes unnecessary for engine and game components. Keep gdx-teavm's `reflection(...)` as an opt-in escape hatch for third-party code that reflects, for example scene2d `Skin` JSON, which needs `reflectionDefaults = true`.
- **Java levels.** Runtime and game code compile at `--release 8`. The processor is tooling at `--release 21`. The TeaVM launcher module must compile at `--release 17` or higher because the gdx-teavm 1.6.2 backend jars are Java 17 class files (major version 61). That matches ADR-0004's split between runtime and platform launchers, but the generated web module cannot use the same release as the desktop launcher.

Limits and caveats:

- **The spike covers less than FR-S06.** Supported field types are boolean, int, long, float, double and String. Nested value types (`Vector2`, `Color`), arrays and collections, enums, asset references, inherited fields (only declared fields are read), schema versioning and migration, and the editor **property schema** are not generated yet. M2 extends the processor's field model so it emits the property schema from the same analysis.
- **Kotlin components (ADR-0012) need KSP or kapt.** The processor is javac-only.
- **JSON text differs by platform.** Under TeaVM, `JsonWriter` writes `90` and `-1` where the JVM writes `90.0`. Values round-trip on both, but saved files are not byte-identical across platforms. Tests compare values, not text.
- **gdx-teavm `pagehide` handling.** gdx-teavm disposes the current listener on `pagehide`. Chrome's `--dump-dom --virtual-time-budget` unloads the page before preloading finishes, so the stock preloader's `dispose()` throws on a null `Stage`. Use a real-time browser harness such as Playwright, not `--dump-dom`.
- **Gradle 10.** The gdx-teavm 1.6.2 and TeaVM 0.15 plugins use multi-string dependency notation, which is deprecated in Gradle 9.9 and fails in Gradle 10. Track upstream before the engine moves to Gradle 10.
- **TeaVM 0.16.** It switches gdx-teavm master to a `ReflectionPolicy` model. This design does not use reflection, so it should be unaffected. Re-run the browser check when gdx-teavm releases a TeaVM 0.16 build.

Fallback (not needed): if the registry path had failed on TeaVM, M2 would have kept libGDX `Json`'s reflective path on the web. The same processor would emit gdx-teavm `reflection(...)` patterns, or a `ReflectionPolicy` on TeaVM 0.16, that list only `@WsComponent` classes. That is Option 2, scoped to generated, explicit class lists rather than package wildcards.
