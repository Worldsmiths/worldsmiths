# Rollball sample

D8 3D probe: roll a ball on a pad, collect pickups, win or fall/bounds-fail, restart without
relaunching. Uses engine `D3` (camera / lights / primitive meshes / batch); scene wiring uses
`Transform3D` → ModelInstance sync each frame. Score / WIN / FAIL appear in the window title
(`Rollball — score: N`).

Desktop, browser (TeaVM / gdx-teavm), Android, and iOS (RoboVM) share the same `:game` sources.

## Controls

- **Move (desktop / keyboard):** WASD or arrow keys (ignored while WIN/FAIL)
- **Move (Android / iOS / touch):** drag-stick — finger-down is origin; drag while held steers;
  release zeros intent
- **Restart after WIN/FAIL:** `R` or Space (keyboard); tap (touch)

## Requirements

- JDK 21 or newer to run Gradle (same as the engine monorepo).
- The game module compiles for **Java 8** (`options.release = 8`), as required by the engine plugin.
- Browser run: a modern Chromium-based browser (Chrome recommended).
- iOS package / simulator: **macOS + Xcode** (RoboVM). Windows/Linux can still configure and generate `:ios`.

This sample is a nested Gradle project. It applies `io.github.worldsmiths.engine` via `includeBuild` of the monorepo `gradle-plugin`, and resolves `ws-core` / `backends-desktop` / `backends-web` / `backends-android` / `backends-ios` from the monorepo root with bare `includeBuild` (not Maven Central; Gradle auto-substitutes matching project names). Android needs Google Maven (`google()`) in both `pluginManagement.repositories` (AGP on the engine plugin classpath) and `dependencyResolutionManagement` (Android artifacts). The consumer snippet is in [`docs/includebuild-substitutions.md`](../../docs/includebuild-substitutions.md). Game sources must not import `com.badlogic.gdx` (including core); input, graphics meta, and 3D draw go through `EngineServices`. They must also not import `com.badlogic.gdx.backends`, `com.github.xpenatan.gdx.teavm.backends`, or any `*ApplicationConfiguration` type.

## Manual validation (desktop + web + Android + iOS)

From the monorepo root (`worldsmiths/`). On Windows use `.\gradlew.bat` instead of `./gradlew`.

### Desktop

```bash
./gradlew -p samples/rollball :desktop:run
```

Smoke: window opens → WASD/arrows move the ball → collect gold pickups → title score increments →
collect all 8 → title shows `WIN` → (or drive off the pad / leave arena → `FAIL`) → `R`/Space
restarts → close window (dispose frees Models / ModelBatch).

Compile-only (no window): `./gradlew -p samples/rollball :desktop:compileJava`

**CI compile/test ≠ play confirmation.** There is no GPU / Playwright CI for this sample.

### Web (browser)

```bash
# Build JS (+ Wasm compile) then serve JS at http://localhost:8080
./gradlew -p samples/rollball :web:gdx_teavm_web_js_build :web:gdx_teavm_web_wasm_build
./gradlew -p samples/rollball :web:gdx_teavm_web_js_run
```

Open the URL Gradle prints (usually `http://localhost:8080`). Smoke: canvas visible → keyboard
and/or touch drag-stick move → collect / win or fail → title updates → tap or `R`/Space restarts
when terminal. Stop the server with Ctrl+C.

### Android

See [Build / install Android](#build--install-android) below (`installDebug` + drag-stick / tap play).

### iOS

See [Build / install iOS](#build--install-ios) below (`robovmInstall` / `launchIPhoneSimulator` + manual sim play).

---

## Build / compile (no window)

From the monorepo root:

```bash
./gradlew -p samples/rollball :desktop:compileJava
```

Or from this directory (using the monorepo wrapper):

```bash
../../gradlew :desktop:compileJava
```

Unit tests (controller / intent / touch gestures / camera / sync):

```bash
./gradlew -p samples/rollball :game:test
```

## Run desktop (detail)

Opens a real desktop window — run locally, not in CI:

```bash
./gradlew -p samples/rollball :desktop:run
```

On macOS the plugin adds `-XstartOnFirstThread` for LWJGL. The run task uses the sample
`assets/` directory as its working directory (`worldsmiths.assetsDir`, default `"assets"`).

Optional desktop attrs in `settings.gradle.kts`: `vsync` / `resizable` (default `true`),
and `icon` (path relative to `assetsDir`). Web: `storagePrefix` (default `"app"`). See the
root [README](../../README.md#target-settings-curated-dsl).

Close the window to exit.

## Build / install Android

Requires an Android SDK (`ANDROID_HOME` or `ANDROID_SDK_ROOT`). With the SDK present:

```bash
# Assemble a debug APK (CI / local proof)
./gradlew -p samples/rollball :android:assembleDebug

# Install on a connected device or emulator, then launch manually
./gradlew -p samples/rollball :android:installDebug
```

APK output: `.worldsmiths/gen/android/build/outputs/apk/debug/` (also uploaded by the
rollball Android CI ship job when present). Same controls as desktop once running (touch
drag-stick; tap after WIN/FAIL to restart). Soft keyboard should stay hidden. If
`ANDROID_HOME` is unset, configure still succeeds and generates `:android`, but
`assembleDebug` / `installDebug` need the SDK.

### Founder manual play gate (human)

CI only proves `assembleDebug` + `ws-ship-marker.txt` membership in the APK. Playability
on a real device or emulator is a **manual founder gate**, not automated:

1. Start an emulator or connect a device with USB debugging.
2. `./gradlew -p samples/rollball :android:installDebug` (or `adb install -r` the debug APK).
3. Launch Rollball; drag to steer, tap after WIN/FAIL to restart.
4. Confirm score / WIN / FAIL updates in the title the same as desktop.

Do not treat a green rollball Android ship job as device play evidence. **CI ≠ play.**

## Build / install iOS

Requires **macOS + Xcode**. The sample `gradle.properties` raises the Gradle daemon heap to
`-Xmx2g` so RoboVM AOT does not OOM (same deviation as `fixtures/ios-phase0`). Default
`worldsmiths.assetsDir` is `"assets"` (ships `ws-ship-marker.txt` into RoboVM resources).

```bash
# Package an unsigned sim-oriented install tree (CI / local proof)
./gradlew -p samples/rollball :ios:robovmInstall

# Zip the install tree for ship upload (peer of distZip / itch zip / debug APK)
./gradlew -p samples/rollball :ios:wsRobovmInstallZip

# Launch on the iPhone simulator (local Mac; not CI)
./gradlew -p samples/rollball :ios:launchIPhoneSimulator
```

Install tree: `.worldsmiths/gen/ios/build/robovm/` (must contain `ws-ship-marker.txt` when
`assets/` is present). Zip: `.worldsmiths/gen/ios/build/distributions/ios-robovm-install.zip`.
Configure still succeeds and generates `:ios` on non-macOS hosts, but `robovmInstall` /
`launchIPhoneSimulator` need macOS + Xcode.

### Founder manual play gate (human)

CI iOS ship proves `robovmInstall` + marker membership + zip upload.
**CI ≠ play.** Simulator playability is a **manual founder gate**:

1. On a Mac with Xcode, boot an iPhone simulator (or let RoboVM pick the default).
2. `./gradlew -p samples/rollball :ios:launchIPhoneSimulator` (or install from the
   `robovmInstall` tree and launch manually).
3. Drag to steer; tap after WIN/FAIL to restart.
4. Confirm score / WIN / FAIL updates the same as desktop / Android.

Do not treat a green iOS package job as simulator play evidence.

## Ship archives (desktop + web)

Desktop uses the Application plugin distribution tasks. Web packaging is a Worldsmiths itch-style
zip of the TeaVM JS webapp (`index.html` at the zip root):

```bash
# Desktop install layout + zip (includes assets/ contents such as ws-ship-marker.txt)
./gradlew -p samples/rollball :desktop:installDist :desktop:distZip

# TeaVM JS build + itch-style zip
./gradlew -p samples/rollball :web:wsItchWebZip
```

Outputs:

- Desktop zip: `.worldsmiths/gen/desktop/build/distributions/*.zip`
- Web itch zip: `.worldsmiths/gen/web/build/distributions/web-itch.zip`

## Build / run web (detail)

Both JS and Wasm TeaVM flavors are configured (`reflectionEnabled=false`). Manual smoke steps are under [Manual validation](#manual-validation-desktop--web--android--ios). Outputs land under `.worldsmiths/gen/web/build/dist/{js,wasm}/webapp/`.

There is **no** automated GPU / Playwright browser check for rollball in D8 — founder web play is a human gate. **CI ≠ play.**
