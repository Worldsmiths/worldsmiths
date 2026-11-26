# AsyncLoad sample

Async asset-loading proof for ticket 02: a packaged heavy texture under `assets/textures/heavy.png`
is requested via `assets().request(..., AssetKind.TEXTURE)`. Load signals go through `services.log()`
(info on start/complete; warn on FAILED). The engine itself stays quiet on fail — sample-only logging.

Desktop, browser (TeaVM / gdx-teavm), and Android share the same `:game` sources. iOS launcher is not
required here; the shared bag/listener pump from T2 already covers the API wire.

## Requirements

- JDK 21 or newer to run Gradle (same as the engine monorepo).
- The game module compiles for **Java 8** (`options.release = 8`), as required by the engine plugin.
- Browser run: a modern Chromium-based browser (Chrome recommended).
- Android package: Android SDK (`ANDROID_HOME` or `ANDROID_SDK_ROOT`).

This sample is a nested Gradle project. It applies `io.github.worldsmiths.engine` via `includeBuild` of the
monorepo `gradle-plugin`, and resolves `ws-core` / backends from the monorepo root with bare `includeBuild`
(ADR-0003). Game sources must not import `com.badlogic.gdx` — enforced by `AsyncLoadGameNoGdxImportTest`.

## Assets

| Path | Role |
| --- | --- |
| `assets/textures/heavy.png` | Async TEXTURE key `textures/heavy.png` (≥256 px; much larger than 141 B sprites assets) |
| `assets/ws-ship-marker.txt` | Ship/package marker (same convention as other samples) |

## Headless tests

```bash
./gradlew -p samples/asyncload :game:test
```

Covers FakeAssets seed → pump → COMPLETE, FakeLog ≥1 load-related emit, and the no-GDX import gate.

## Manual validation (desktop + web + Android)

From the monorepo root (`worldsmiths/`). On Windows use `.\gradlew.bat` instead of `./gradlew`.

### Desktop

```bash
./gradlew -p samples/asyncload :desktop:run
```

Smoke: window opens titled `AsyncLoad` → load completes (console / GdxLog info for start + complete) → close window.

Compile-only (no window): `./gradlew -p samples/asyncload :desktop:compileJava`

### Web

```bash
./gradlew -p samples/asyncload :web:gdx_teavm_web_js_build :web:gdx_teavm_web_wasm_build
./gradlew -p samples/asyncload :web:gdx_teavm_web_js_run
```

Open the URL Gradle prints (usually `http://localhost:8080`). Smoke: canvas loads; heavy texture is packaged under the web dist. Stop the server with Ctrl+C.

### Android

```bash
./gradlew -p samples/asyncload :android:assembleDebug
./gradlew -p samples/asyncload :android:installDebug
```

There is no `:android:run` task — launch from the device/emulator launcher (or `adb`) after install. APK output: `.worldsmiths/gen/android/build/outputs/apk/debug/`. Requires Android SDK. Packaging should include `textures/heavy.png` from `assetsDir`.

## Build / compile (no window)

```bash
./gradlew -p samples/asyncload :desktop:compileJava :game:test
```
