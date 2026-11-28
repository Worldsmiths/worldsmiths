# Relay sample

P2 showcase scaffold: ordered **two-leg** race on the ground plane — touch the **+X** gate, then the
**−X** gate, under a hard run-par (`RelayController.RUN_PAR_SECONDS`). Beat the par → **CLEAR**;
elapsed ≥ par before the last gate → **FAIL**. Elapsed freezes on either terminal; restart retries
from leg 1. Checkpoint overlap uses an **AABB** volume (implementer choice for P2).

Uses engine `D3` (camera / lights / primitive meshes / batch), `Transform3D` → ModelInstance
sync, `Audio` / `Sound` for the one-shot cue at `audio/leg-complete.wav` (plays on rising **CLEAR**
only — not on intermediate gates, not on FAIL, not on restart), and `Ui` / `Font` for the single-line
HUD from packaged `fonts/hud.fnt` (+ page `fonts/hud.png`). HUD shows live elapsed + `N/M` (current
target vs 2) and appends `CLEAR` / `FAIL` when terminal; window title dual-writes thinly (`setTitle`)
while the on-screen HUD is the source of truth.

Desktop, browser (TeaVM / gdx-teavm), Android, and iOS (RoboVM) share the same `:game` sources.
P2 CI only runs desktop compile + `:game:test` — no relay ship / android / ios archive jobs yet.

## Controls

- **Move (desktop / keyboard):** WASD or arrow keys (ignored while terminal CLEAR/FAIL)
- **Move (Android / iOS / touch):** drag-stick — finger-down is origin; drag while held steers;
  release zeros intent
- **Restart when terminal:** `R` or Space (keyboard); tap (touch) — not available mid-run

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

Smoke: window opens → elapsed HUD ticks with `1/2` → WASD/arrows (or drag-stick) move the agent →
enter the **+X** gate (HUD → `2/2`, no cue) → enter the **−X** gate under par → HUD/title show
`CLEAR` and the short cue plays → dawdle past par instead → `FAIL` with no cue → `R`/Space or tap
restarts (elapsed 0; leg 1; no cue on restart).

**CI compile/test ≠ play confirmation.** There is no GPU / Playwright / real-audio / font-raster CI
for this sample; `:game:test` asserts FakeAudio / FakeUi / FakeUiKit markers and controller rules
headlessly (`seedMeasureAdvance` on kit label/button text before layout).
