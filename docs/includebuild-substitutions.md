# includeBuild (bare composite)

Until the engine is published to Maven Central (ADR-0003), external games and samples resolve Worldsmiths artifacts with a composite build: bare `includeBuild` of the engine checkout. Backend Gradle project names (`backends-desktop`, `backends-web`, `backends-android`, `backends-ios`) and `ws-core` match the Maven artifact names, so Gradle auto-substitutes module coordinates to included projects. Happy-path consumers do **not** write `dependencySubstitution`. Do **not** treat `archivesName` as a substitute for that rename.

## Snippet

Point `engineRoot` at a checkout of this repository (sibling clone, CI checkout, etc.). Apply the settings plugin via `pluginManagement { includeBuild("$engineRoot/gradle-plugin") }` (or an equivalent included plugin build).

**Android (`targets.android`):** AGP ships on the engine plugin classpath (TeaVM-style). Consumers must put Google Maven in **`pluginManagement.repositories`** so Plugin Portal alone is not the only resolution path for AGP, and again under `dependencyResolutionManagement` for Android artifacts:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    includeBuild("$engineRoot/gradle-plugin")
}
```

DRM `google()` alone is not enough. Missing `google()` when Android is opted in fails configuration with a clear message.

```kotlin
includeBuild(engineRoot)
// no dependencySubstitution { }
```

| Maven module id | Engine project (auto-sub) |
| --- | --- |
| `io.github.worldsmiths:backends-desktop` | `:backends-desktop` |
| `io.github.worldsmiths:backends-web` | `:backends-web` |
| `io.github.worldsmiths:backends-android` | `:backends-android` |
| `io.github.worldsmiths:backends-ios` | `:backends-ios` |
| `io.github.worldsmiths:ws-core` | `:runtime:ws-core` |

Plugin-declared notations live in `EngineModuleCoordinates` in `gradle-plugin`; a contract test locks those Maven id strings and asserts happy-path samples/docs use bare `includeBuild`.
