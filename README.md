# worldsmiths

Hobby 0.x 2D engine (unpublished). `./gradlew build` compiles a Java 8 runtime
(`ws-core`), LibGDX-backed desktop and web backends, a bill of materials, and the
included Gradle plugin.

The Gradle plugin id is `io.github.worldsmiths.engine`. A game applies it in
`settings.gradle.kts`. It generates `:desktop` and, when configured, `:web` for a
game that compiles for Java 8. The engine's own build does not apply it.

Group id and package: `io.github.worldsmiths`, from `gradle/libs.versions.toml`.

External consumers resolve engine modules with `includeBuild` +
`dependencySubstitution` (not Maven Central). See
[`docs/includebuild-substitutions.md`](docs/includebuild-substitutions.md).

Ship archives: desktop `:desktop:distZip` / `:desktop:installDist`, and web
`:web:wsItchWebZip` (itch-style zip with `index.html` at the root). Game assets
come from `worldsmiths.assetsDir` (default `"assets"`). Decisions are recorded in
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
}
```

`icon` is relative to `worldsmiths.assetsDir` (desktop run working directory and
shipped assets root). Omit it for no custom window icon. `storagePrefix` matches
gdx-teavm `WebApplicationConfiguration.storagePrefix`.

## Build

JDK 21 or newer. Then:

```bash
./gradlew build
```
