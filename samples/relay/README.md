# Relay sample

P2 showcase scaffold: move an agent into **one** active checkpoint, see run elapsed on-screen and
leg complete in the window title, hear a short packaged cue, then restart. An optional second gate
mesh is visible but inactive (does not complete the leg). Checkpoint overlap uses an **AABB**
volume (implementer choice for P2).

Uses engine `Thin3D` (camera / lights / primitive meshes / batch), `Transform3D` → ModelInstance
sync, `Audio` / `Sound` for the one-shot cue at `audio/leg-complete.wav` (plays on leg-complete
only; restart does not replay), and `Ui` / `Font` for the run-elapsed HUD from packaged
`fonts/hud.fnt` (+ page `fonts/hud.png`). Elapsed accumulates from `update(delta)` and resets on
restart; each frame draws seconds plus optional `1/1`. Window title still dual-writes status
(`setTitle`) alongside the on-screen HUD.

Desktop, browser (TeaVM / gdx-teavm), Android, and iOS (RoboVM) share the same `:game` sources.
P2 CI only runs desktop compile + `:game:test` — no relay ship / android / ios archive jobs yet.

## Controls

- **Move (desktop / keyboard):** WASD or arrow keys (ignored while leg complete)
- **Move (Android / iOS / touch):** drag-stick — finger-down is origin; drag while held steers;
  release zeros intent
- **Restart after leg complete:** `R` or Space (keyboard); tap (touch)

## Requirements

- JDK 21 or newer to run Gradle (same as the engine monorepo).
- The game module compiles for **Java 8** (`options.release = 8`), as required by the engine plugin.
- Browser run: a modern Chromium-based browser (Chrome recommended).
- iOS package / simulator: **macOS + Xcode** (RoboVM). Windows/Linux can still configure and generate `:ios`.

This sample is a nested Gradle project. It applies `io.github.worldsmiths.engine` via `includeBuild` of the monorepo `gradle-plugin`, and resolves `ws-core` / backends from the monorepo root with bare `includeBuild`. Game sources must not import `com.badlogic.gdx` (including core); input, graphics meta, 3D draw, audio, and UI text go through `EngineServices`.

## Build / compile / test

From the monorepo root (`worldsmiths/`). On Windows use `.\gradlew.bat` instead of `./gradlew`.

```bash
./gradlew -p samples/relay :desktop:compileJava :game:test
```

## Run desktop

```bash
./gradlew -p samples/relay :desktop:run
```

Smoke: window opens → elapsed HUD ticks → WASD/arrows (or drag-stick) move the agent → enter the
**+X** gate → title shows `leg complete` and the short cue plays → the **−X** gate is visible but
does nothing → `R`/Space or tap restarts (elapsed resets; no cue on restart).

**CI compile/test ≠ play confirmation.** There is no GPU / Playwright / real-audio / font-raster CI
for this sample; `:game:test` asserts FakeAudio and FakeUi kind-presence only.
