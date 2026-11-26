# Sprites sample

Textured 2D sprites proof for ticket 01: packaged PNGs + scene JSON under `assets/`, loaded on create via
`files().readString` → `SceneCodec.fromJson` → `textures().preload` of distinct keys. The backend draws
retained textured quads (and one colored-rect sprite with a null texture).

Desktop and browser (TeaVM / gdx-teavm) share the same `:game` sources.

## Requirements

- JDK 21 or newer to run Gradle (same as the engine monorepo).
- The game module compiles for **Java 8** (`options.release = 8`), as required by the engine plugin.
- Browser run: a modern Chromium-based browser (Chrome recommended).

This sample is a nested Gradle project. It applies `io.github.worldsmiths.engine` via `includeBuild` of the
monorepo `gradle-plugin`, and resolves `ws-core` / backends from the monorepo root with bare `includeBuild`
(ADR-0003). Game sources must not import `com.badlogic.gdx` — enforced by `SpritesGameNoGdxImportTest`.

## Assets

| Path | Role |
| --- | --- |
| `assets/scenes/demo.json` | Schema v1 scene with textured + plain sprites |
| `assets/hero.png` | Texture key `hero.png` |
| `assets/coin.png` | Texture key `coin.png` |
| `assets/ws-ship-marker.txt` | Ship/package marker (same convention as other samples) |

## Headless tests

```bash
./gradlew -p samples/sprites :game:test
```

Covers FakeFiles + FakeTextures preload/CREATE recording, SceneWalk textured draw keys, and the no-GDX
import gate.

## Manual validation (desktop + web)

From the monorepo root (`worldsmiths/`). On Windows use `.\gradlew.bat` instead of `./gradlew`.

### Desktop (AC1 / V7)

```bash
./gradlew -p samples/sprites :desktop:run
```

Smoke: window opens titled `Sprites` → see two textured quads (hero tint, coin tint) plus one blue
colored rect (null texture) → close window.

Compile-only (no window): `./gradlew -p samples/sprites :desktop:compileJava`

### Web (AC4 / V8)

```bash
./gradlew -p samples/sprites :web:gdx_teavm_web_js_build :web:gdx_teavm_web_wasm_build
./gradlew -p samples/sprites :web:gdx_teavm_web_js_run
```

Open the URL Gradle prints (usually `http://localhost:8080`). Smoke: canvas shows the same textured
sprites + colored rect as desktop. Stop the server with Ctrl+C.

Wasm is compile-checked with the build above; JS run is the interactive smoke path.

## Build / compile (no window)

```bash
./gradlew -p samples/sprites :desktop:compileJava :game:test
```
