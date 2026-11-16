# Snake sample

Fuller 2D Snake for D3: a visible grid, head + body segment nodes + food node with `Transform2D` +
colored `Sprite` (engine-drawn), discrete grid movement on a step timer, grow on eat, wall/self
collision ends the run, score in the window title (`Snake — score: N`), and restart without
relaunching.

Desktop, browser (TeaVM / gdx-teavm), and Android share the same `:game` sources.

## Controls

- **Move:** arrow keys or WASD (ignored while dead)
- **Restart after death:** `R` or Space

## Requirements

- JDK 21 or newer to run Gradle (same as the engine monorepo).
- The game module compiles for **Java 8** (`options.release = 8`), as required by the engine plugin.
- Browser run: a modern Chromium-based browser (Chrome recommended). The Playwright check needs
  Google Chrome installed (or set `CHROME` to a Chromium binary).

This sample is a nested Gradle project. It applies `io.github.worldsmiths.engine` via `includeBuild` of the monorepo `gradle-plugin`, and resolves `ws-core` / `backends-desktop` / `backends-web` / `backends-android` from the monorepo root with `includeBuild` + dependency substitution (not Maven Central). Android needs Google Maven (`google()`) in both `pluginManagement.repositories` (AGP on the engine plugin classpath) and `dependencyResolutionManagement` (Android artifacts). The consumer snippet is in [`docs/includebuild-substitutions.md`](../../docs/includebuild-substitutions.md). Game sources may use LibGDX core (`Gdx`, shapes) but must not import `com.badlogic.gdx.backends`, `com.github.xpenatan.gdx.teavm.backends`, or any `*ApplicationConfiguration` type.

## Build / compile (no window)

From the monorepo root:

```bash
./gradlew -p samples/snake :desktop:compileJava
```

Or from this directory (using the monorepo wrapper):

```bash
../../gradlew :desktop:compileJava
```

## Run desktop (manual smoke)

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

APK output: `.worldsmiths/gen/android/build/outputs/apk/debug/`. Same controls as desktop once running
(touch / hardware keys depending on the device). If `ANDROID_HOME` is unset, configure still
succeeds and generates `:android`, but `assembleDebug` / `installDebug` need the SDK.

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

## Build / run web

Both JS and Wasm TeaVM flavors are configured (`reflectionEnabled=false`). From the monorepo root:

```bash
# Build artifacts
./gradlew -p samples/snake :web:gdx_teavm_web_js_build :web:gdx_teavm_web_wasm_build

# Serve the JS build at http://localhost:8080 (manual play)
./gradlew -p samples/snake :web:gdx_teavm_web_js_run
```

Outputs land under `.worldsmiths/gen/web/build/dist/{js,wasm}/webapp/`.

### Playwright browser check (JS)

After a JS build, from `samples/snake/browser-check`:

```bash
npm ci
node check.mjs ../.worldsmiths/gen/web/build/dist/js/webapp
```

The check serves the webapp, asserts a `canvas` is present, and waits until `document.title`
matches `Snake — score: <digits>`. Mainline CI runs this on **ubuntu-latest** only (Chrome channel);
Wasm is compile-checked in CI but not loaded in Playwright this milestone.
