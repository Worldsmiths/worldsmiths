# Mesh3d sample

glTF **MESH** proof for ticket 05: packaged `assets/meshes/demo.gltf` with **external** `demo_basecolor.png`
and `demo.bin` (no required embedded GLB). The game requests `AssetKind.MESH`, binds via `d3().createModelFromLoaded`
after `LoadHandle` reaches COMPLETE, draws with `ModelBatch`, and toggles diffuse/emissive every two seconds for a
visible tint change.

Desktop, browser (TeaVM / gdx-teavm), Android, and iOS share the same `:game` sources. Game sources must not import
`com.badlogic.gdx` or `net.mgsx.gltf` — enforced by `Mesh3dGameNoGdxImportTest`.

## Requirements

- JDK 21 or newer to run Gradle (same as the engine monorepo).
- The game module compiles for **Java 8** (`options.release = 8`), as required by the engine plugin.
- Browser run: a modern Chromium-based browser (Chrome recommended).
- Android package: Android SDK (`ANDROID_HOME` or `ANDROID_SDK_ROOT`).

This sample is a nested Gradle project with bare `includeBuild` of the monorepo (ADR-0003).

## Assets

| Path | Role |
| --- | --- |
| `assets/meshes/demo.gltf` | Async MESH key `meshes/demo.gltf`; references external image + bin |
| `assets/meshes/demo_basecolor.png` | External base-color texture (portable web AC) |
| `assets/meshes/demo.bin` | External geometry buffer |
| `assets/ws-ship-marker.txt` | Ship/package marker (same convention as other samples) |

## Headless tests

```bash
./gradlew -p samples/mesh3d :game:test
```

Covers FakeAssets MESH → pump → COMPLETE, FakeD3 bind/draw/tint commands, no-GDX/gltf import gate, and glTF packaging contract.

## Manual validation (desktop + web) — AC8

From the monorepo root (`worldsmiths/`). On Windows use `.\gradlew.bat` instead of `./gradlew`.

### Desktop

```bash
./gradlew -p samples/mesh3d :desktop:run
```

**UAT checklist (desktop):**

1. Window opens titled `Mesh3d`.
2. After a short async load, a shaded cube (or mesh) is visible in the center of the view.
3. Every ~2 seconds the mesh tint shifts noticeably (warm orange ↔ cool blue) via diffuse/emissive.
4. Close the window cleanly.

Compile-only: `./gradlew -p samples/mesh3d :desktop:compileJava`

### Web

```bash
./gradlew -p samples/mesh3d :web:gdx_teavm_web_js_build :web:gdx_teavm_web_wasm_build
./gradlew -p samples/mesh3d :web:gdx_teavm_web_js_run
```

Open the URL Gradle prints (usually `http://localhost:8080`).

**UAT checklist (web):**

1. Canvas loads without requiring an embedded GLB (assets are `.gltf` + external files).
2. Mesh becomes visible after async MESH completes (may hitch one frame on cooperative load).
3. Tint toggle is visible on the same ~2 s cadence as desktop.
4. Stop the server with Ctrl+C.

### Android / iOS (optional smoke)

```bash
./gradlew -p samples/mesh3d :android:assembleDebug
./gradlew -p samples/mesh3d :android:installDebug
```

There is no `:android:run` task — launch from the device/emulator launcher (or `adb`) after install. Same bind/draw/tint behaviour when MESH completes.

## Build / compile (no window)

```bash
./gradlew -p samples/mesh3d :desktop:compileJava :game:test
```