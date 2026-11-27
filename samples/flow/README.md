# Flow sample

Multi-scene replace proof for ticket 03: stub menu → gameplay → second SceneCodec room via
`services.scenes().load(...)`. SPACE / ENTER / touch advances one hop per rising edge. Packaged
JSON under `assets/scenes/` (menu, gameplay, room_b).

Desktop, browser (TeaVM / gdx-teavm), and Android share the same `:game` sources. Backends draw
`scenes().current()`; sample launchers compile with the current listeners. Headless `:game:test`
under FakeEngineServices is the T4 Done gate.

## Requirements

- JDK 21 or newer to run Gradle (same as the engine monorepo).
- The game module compiles for **Java 8** (`options.release = 8`), as required by the engine plugin.
- Browser run: a modern Chromium-based browser (Chrome recommended).
- Android package: Android SDK (`ANDROID_HOME` or `ANDROID_SDK_ROOT`).

This sample is a nested Gradle project. It applies `io.github.worldsmiths.engine` via `includeBuild` of the
monorepo `gradle-plugin`, and resolves `ws-core` / backends from the monorepo root with bare `includeBuild`
(ADR-0003). Game sources must not import `com.badlogic.gdx` — enforced by `FlowGameNoGdxImportTest`.

## Assets

| Path | Role |
| --- | --- |
| `assets/scenes/menu.json` | Stub menu scene (initial `scenes().load`) |
| `assets/scenes/gameplay.json` | First hop via `scenes().load` |
| `assets/scenes/room_b.json` | Second hop via `scenes().load` |
| `assets/ws-ship-marker.txt` | Ship/package marker (same convention as other samples) |

## Headless tests

```bash
./gradlew -p samples/flow :game:test
```

Covers FakeFiles seed + FakeInput menu→game→room hops, FakeScenes ≥2 replaces after initial load, and
the no-GDX import gate.

## Manual validation (desktop + web + Android)

From the monorepo root (`worldsmiths/`). On Windows use `.\gradlew.bat` instead of `./gradlew`.

### Desktop

```bash
./gradlew -p samples/flow :desktop:run
```

Smoke: window opens titled `Flow` → blue menu rect → SPACE → green gameplay → SPACE → orange room B →
close window.

Compile-only (no window): `./gradlew -p samples/flow :desktop:compileJava`

### Web

```bash
./gradlew -p samples/flow :web:gdx_teavm_web_js_build :web:gdx_teavm_web_wasm_build
./gradlew -p samples/flow :web:gdx_teavm_web_js_run
```

Open the URL Gradle prints (usually `http://localhost:8080`). Smoke: same hop sequence as desktop.
Stop the server with Ctrl+C.

### Android

```bash
./gradlew -p samples/flow :android:assembleDebug
./gradlew -p samples/flow :android:installDebug
```

There is no `:android:run` task — launch from the device/emulator launcher (or `adb`) after install.
APK output: `.worldsmiths/gen/android/build/outputs/apk/debug/`. Requires Android SDK.

## Build / compile (no window)

```bash
./gradlew -p samples/flow :game:test
```
