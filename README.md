# worldsmiths

Empty engine skeleton. `./gradlew build` compiles a Java 8 runtime module, an empty desktop launcher, a bill of materials, and the included Gradle plugin. Nothing is published. There is no game loop and no libGDX dependency yet.

The Gradle plugin id is `io.github.worldsmiths.engine`. Applying it does nothing until the settings-plugin spike.

Group id and package: `io.github.worldsmiths`, from `gradle/libs.versions.toml`.

A web upload zip (the page, the script and the assets, for itch.io and similar hosts) is not produced by this build yet. Decisions are recorded in `docs/adr/`.

## Build

JDK 21 or newer. Then:

```bash
./gradlew build
```
