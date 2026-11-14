# Snake sample

Partial 2D Snake for D2: a visible grid, one head node with `Transform2D` + colored `Sprite` (engine-drawn), discrete grid movement on a step timer, and turns via arrow keys or WASD (`Gdx.input`). No body, food, or score yet.

## Requirements

- JDK 21 or newer to run Gradle (same as the engine monorepo).
- The game module compiles for **Java 8** (`options.release = 8`), as required by the engine plugin.

This sample is a nested Gradle project. It applies `io.github.worldsmiths.engine` via `includeBuild` of the monorepo `gradle-plugin`, and resolves `ws-core` / `backends-desktop` from the monorepo root with `includeBuild` + dependency substitution (not Maven Central). Game sources may use LibGDX core (`Gdx`, shapes) but must not import `com.badlogic.gdx.backends`.

## Build / compile (no window)

From the monorepo root:

```bash
./gradlew -p samples/snake :desktop:compileJava
```

Or from this directory (using the monorepo wrapper):

```bash
../../gradlew :desktop:compileJava
```

## Run (manual AC4)

Opens a real desktop window — run locally, not in CI:

```bash
./gradlew -p samples/snake :desktop:run
```

On macOS the plugin adds `-XstartOnFirstThread` for LWJGL.

Close the window to exit.
