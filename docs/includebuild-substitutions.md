# includeBuild dependency substitutions

Until the engine is published to Maven Central (ADR-0003), external games and samples resolve Worldsmiths artifacts with a composite build: `includeBuild` of the engine checkout plus `dependencySubstitution` for each module the plugins and game sources request by Maven coordinate.

Gradle project paths (`:backends:desktop`, `:backends:web`, `:backends:android`, `:runtime:ws-core`) do **not** match the Maven artifact names (`backends-desktop`, `backends-web`, `backends-android`, `ws-core`). Do **not** treat `archivesName` as the fix; aligning project names waits for a later milestone (Da). Use the substitutions below.

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
includeBuild(engineRoot) {
    dependencySubstitution {
        substitute(module("io.github.worldsmiths:backends-desktop"))
            .using(project(":backends:desktop"))
        substitute(module("io.github.worldsmiths:backends-web"))
            .using(project(":backends:web"))
        substitute(module("io.github.worldsmiths:backends-android"))
            .using(project(":backends:android"))
        substitute(module("io.github.worldsmiths:ws-core"))
            .using(project(":runtime:ws-core"))
    }
}
```

| Maven module id | Engine project |
| --- | --- |
| `io.github.worldsmiths:backends-desktop` | `:backends:desktop` |
| `io.github.worldsmiths:backends-web` | `:backends:web` |
| `io.github.worldsmiths:backends-android` | `:backends:android` |
| `io.github.worldsmiths:ws-core` | `:runtime:ws-core` |

A desktop-only game may omit the `backends-web` / `backends-android` lines until it adds those targets. Games that depend on `ws-core` from the game module (and any target that pulls a backend) need the matching substitutes.

Plugin-declared notations and TestKit fixtures share `EngineModuleCoordinates` / `IncludeBuildSubstitutions` in `gradle-plugin`; a contract test fails if sample or documented substitutes drift from those ids.
