# worldsmiths

Hobby 0.x 2D engine (unpublished). `./gradlew build` compiles a Java 8 runtime
(`ws-core`), LibGDX-backed desktop and web backends, a bill of materials, and the
included Gradle plugin.

The Gradle plugin id is `io.github.worldsmiths.engine`. A game applies it in
`settings.gradle.kts`. It generates `:desktop` and, when configured, `:web` and
`:android` for a game that compiles for Java 8. The engine's own build does not
apply it.

Group id and package: `io.github.worldsmiths`, from `gradle/libs.versions.toml`.

External consumers resolve engine modules with `includeBuild` +
`dependencySubstitution` (not Maven Central). See
[`docs/includebuild-substitutions.md`](docs/includebuild-substitutions.md).
Android opt-in also needs `pluginManagement { repositories { google() } }` (AGP on
the engine plugin classpath) plus DRM `google()` for Android artifacts.

Ship archives: desktop `:desktop:distZip` / `:desktop:installDist`, web
`:web:wsItchWebZip` (itch-style zip with `index.html` at the root), and Android
`:android:assembleDebug` (debug APK; CI uploads it). Game assets come from
`worldsmiths.assetsDir` (default `"assets"`). Decisions are recorded in
`docs/adr/`.

## Target settings (curated DSL)

In `settings.gradle.kts`, under `worldsmiths { targets { ... } }`:

```kotlin
targets {
    desktop {
        title.set("My Game")
        width.set(800)
        height.set(600)
        vsync.set(true)          // default true
        resizable.set(true)      // default true
        icon.set("icons/app.png") // optional; path relative to assetsDir
    }
    web {
        title.set("My Game")
        width.set(800)
        height.set(600)
        storagePrefix.set("app") // default "app"; browser prefs / local files
    }
    android {
        title.set("My Game")
        width.set(800)
        height.set(600)
    }
}
```

`icon` is relative to `worldsmiths.assetsDir` (desktop run working directory and
shipped assets root). Omit it for no custom window icon. `storagePrefix` matches
gdx-teavm `WebApplicationConfiguration.storagePrefix`. Android needs an SDK
(`ANDROID_HOME`); device/emulator play for samples is a manual founder gate (see
[`samples/snake/README.md`](samples/snake/README.md#founder-manual-play-gate-human)).

## Build

JDK 21 or newer. Then:

```bash
./gradlew build
```
