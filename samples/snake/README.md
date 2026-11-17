# Snake sample

Fuller 2D Snake for D3: a visible grid, head + body segment nodes + food node with `Transform2D` +
colored `Sprite` (engine-drawn), discrete grid movement on a step timer, grow on eat, wall/self
collision ends the run, score in the window title (`Snake — score: N`), and restart without
relaunching.

Desktop, browser (TeaVM / gdx-teavm), Android, and iOS (RoboVM) share the same `:game` sources.

## Controls

- **Move (desktop / keyboard):** arrow keys or WASD (ignored while dead)
- **Move (Android / iOS / touch):** swipe left / right / up / down
- **Restart after death:** `R` or Space (keyboard); tap or swipe (touch)

## Requirements

- JDK 21 or newer to run Gradle (same as the engine monorepo).
- The game module compiles for **Java 8** (`options.release = 8`), as required by the engine plugin.
- Browser run: a modern Chromium-based browser (Chrome recommended). The Playwright check needs
  Google Chrome installed (or set `CHROME` to a Chromium binary).
- iOS package / simulator: **macOS + Xcode** (RoboVM). Windows/Linux can still configure and generate `:ios`.

This sample is a nested Gradle project. It applies `io.github.worldsmiths.engine` via `includeBuild` of the monorepo `gradle-plugin`, and resolves `ws-core` / `backends-desktop` / `backends-web` / `backends-android` / `backends-ios` from the monorepo root with `includeBuild` + dependency substitution (not Maven Central). Android needs Google Maven (`google()`) in both `pluginManagement.repositories` (AGP on the engine plugin classpath) and `dependencyResolutionManagement` (Android artifacts). The consumer snippet is in [`docs/includebuild-substitutions.md`](../../docs/includebuild-substitutions.md). Game sources may use LibGDX core (`Gdx`, shapes) but must not import `com.badlogic.gdx.backends`, `com.github.xpenatan.gdx.teavm.backends`, or any `*ApplicationConfiguration` type.

## Manual validation (desktop + web + Android + iOS)

From the monorepo root (`worldsmiths/`). On Windows use `.\gradlew.bat` instead of `./gradlew`.

### Desktop

```bash
./gradlew -p samples/snake :desktop:run
```

Smoke: window opens → arrows/WASD move → eat food → score in title (`Snake — score: N`) → die → `R`/Space restarts → close window.

Compile-only (no window): `./gradlew -p samples/snake :desktop:compileJava`

### Web (browser)

```bash
# Build JS (+ Wasm compile) then serve JS at http://localhost:8080
./gradlew -p samples/snake :web:gdx_teavm_web_js_build :web:gdx_teavm_web_wasm_build
./gradlew -p samples/snake :web:gdx_teavm_web_js_run
```

Open the URL Gradle prints (usually `http://localhost:8080`). Smoke: canvas visible → keyboard move/restart same as desktop → title score updates. Stop the server with Ctrl+C.

Automated JS check (Chrome / Playwright), after a JS build:

```bash
cd samples/snake/browser-check
npm ci
node check.mjs ../.worldsmiths/gen/web/build/dist/js/webapp
```

### Android

See [Build / install Android](#build--install-android) below (`installDebug` + swipe/tap play).

### iOS

See [Build / install iOS](#build--install-ios) below (`robovmInstall` / `launchIPhoneSimulator` + manual sim play).

---

## Build / compile (no window)

From the monorepo root:

```bash
./gradlew -p samples/snake :desktop:compileJava
```

Or from this directory (using the monorepo wrapper):

```bash
../../gradlew :desktop:compileJava
```

## Run desktop (detail)

Opens a real desktop window — run locally, not in CI:

```bash
./gradlew -p samples/snake :desktop:run
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
./gradlew -p samples/snake :android:assembleDebug

# Install on a connected device or emulator, then launch manually
./gradlew -p samples/snake :android:installDebug
```

APK output: `.worldsmiths/gen/android/build/outputs/apk/debug/` (also uploaded by the
`snake-android` CI job). Same controls as desktop once running (touch / hardware keys
depending on the device). If `ANDROID_HOME` is unset, configure still succeeds and
generates `:android`, but `assembleDebug` / `installDebug` need the SDK.

### Founder manual play gate (human)

CI only proves `assembleDebug` + `ws-ship-marker.txt` membership in the APK. Playability
on a real device or emulator is a **manual founder gate**, not automated:

1. Start an emulator or connect a device with USB debugging.
2. `./gradlew -p samples/snake :android:installDebug` (or `adb install -r` the debug APK).
3. Launch Snake; swipe to move, tap (or swipe) after death to restart. Soft keyboard should stay hidden.
4. Confirm score updates in the title / UI the same as desktop.

Do not treat a green `snake-android` job as device play evidence.

## Build / install iOS

Requires **macOS + Xcode**. The sample `gradle.properties` raises the Gradle daemon heap to
`-Xmx2g` so RoboVM AOT does not OOM (same deviation as `fixtures/ios-phase0`). Default
`worldsmiths.assetsDir` is `"assets"` (ships `ws-ship-marker.txt` into RoboVM resources).

```bash
# Package an unsigned sim-oriented install tree (CI / local proof)
./gradlew -p samples/snake :ios:robovmInstall

# Zip the install tree for ship upload (peer of distZip / itch zip / debug APK)
./gradlew -p samples/snake :ios:wsRobovmInstallZip

# Launch on the iPhone simulator (local Mac; not CI)
./gradlew -p samples/snake :ios:launchIPhoneSimulator
```

Install tree: `.worldsmiths/gen/ios/build/robovm/` (must contain `ws-ship-marker.txt` when
`assets/` is present). Zip: `.worldsmiths/gen/ios/build/distributions/ios-robovm-install.zip`.
Configure still succeeds and generates `:ios` on non-macOS hosts, but `robovmInstall` /
`launchIPhoneSimulator` need macOS + Xcode.

### Founder manual play gate (human)

CI proves `robovmInstall` + marker membership (+ zip upload once the `snake-ios` job lands).
**CI ≠ play.** Simulator playability is a **manual founder gate**:

1. On a Mac with Xcode, boot an iPhone simulator (or let RoboVM pick the default).
2. `./gradlew -p samples/snake :ios:launchIPhoneSimulator` (or install from the
   `robovmInstall` tree and launch manually).
3. Swipe to move; tap or swipe after death to restart.
4. Confirm score updates the same as desktop / Android.

Do not treat a green iOS package job as simulator play evidence.

## Ship archives (desktop + web)

Desktop uses the Application plugin distribution tasks. Web packaging is a Worldsmiths itch-style
zip of the TeaVM JS webapp (`index.html` at the zip root):

```bash
# Desktop install layout + zip (includes assets/ contents such as ws-ship-marker.txt)
./gradlew -p samples/snake :desktop:installDist :desktop:distZip

# TeaVM JS build + itch-style zip
./gradlew -p samples/snake :web:wsItchWebZip
```

Outputs:

- Desktop zip: `.worldsmiths/gen/desktop/build/distributions/*.zip`
- Web itch zip: `.worldsmiths/gen/web/build/distributions/web-itch.zip`

## Build / run web (detail)

Both JS and Wasm TeaVM flavors are configured (`reflectionEnabled=false`). Manual smoke steps are under [Manual validation](#manual-validation-desktop--web--android). Outputs land under `.worldsmiths/gen/web/build/dist/{js,wasm}/webapp/`.

The Playwright check serves the webapp, asserts a `canvas` is present, and waits until `document.title` matches `Snake — score: <digits>`. Mainline CI runs this on **ubuntu-latest** only (Chrome channel); Wasm is compile-checked in CI but not loaded in Playwright this milestone.
