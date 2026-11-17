# Minimal sample

Empty Worldsmiths game that opens a desktop window (clear/blank frame). Use this for the D1 **AC5** manual smoke: open a window with `:desktop:run`. CI does **not** open a window; it only needs the sample to configure and compile.

## Requirements

- JDK 21 or newer to run Gradle (same as the engine monorepo).
- The game module compiles for **Java 8** (`options.release = 8`), as required by the engine plugin.

This sample is a nested Gradle project. It applies `io.github.worldsmiths.engine` via `includeBuild` of the monorepo `gradle-plugin`, and resolves `ws-core` / `backends-desktop` from the monorepo root with bare `includeBuild` (not Maven Central; Gradle auto-substitutes matching project names). `pluginManagement` still needs `google()` because AGP is on the engine plugin classpath even for desktop-only samples. The consumer snippet is in [`docs/includebuild-substitutions.md`](../../docs/includebuild-substitutions.md).

## Build / compile (no window)

From the monorepo root:

```bash
./gradlew -p samples/minimal :desktop:compileJava
```

Or from this directory (using the monorepo wrapper):

```bash
../../gradlew :desktop:compileJava
```

## Run (manual AC5)

Opens a real desktop window — run locally, not in CI:

```bash
./gradlew -p samples/minimal :desktop:run
```

On macOS the plugin adds `-XstartOnFirstThread` for LWJGL.

Close the window to exit.
