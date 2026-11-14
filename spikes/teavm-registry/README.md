# Spike S2: TeaVM component registry without reflection

Question: can an annotation-processor registry plus libGDX `Json` serialize components on gdx-teavm with no
reflection policy hacks? Answer and evidence: [ADR-0007](../../docs/adr/0007-teavm-component-registry.md).

This is a standalone Gradle build. The root `worldsmiths` build does not include it.

| Module | Java | Contents |
| --- | --- | --- |
| `runtime` | `--release 8` | `@WsComponent`, `ComponentType`, `ComponentRegistry`, `Scene`, `Node`, `SceneJson` |
| `processor` | `--release 21` | `WsComponentProcessor`, which generates `<Name>_WsSerializer` and `GeneratedComponentRegistry` |
| `game` | `--release 8` | `Transform2D`, `Sprite`, `DemoScene` (build, round-trip, verify) |
| `web` | `--release 17` | gdx-teavm launcher; runs the round-trip in `create()` and writes the report into `#ws-scene` |
| `browser-check` | Node 22 | Playwright script that loads the JS and Wasm builds in headless Chrome and checks the report |

## Run it

```shell
cd spikes/teavm-registry
./gradlew build                                                # processor and round-trip tests on the JVM
./gradlew :web:gdx_teavm_web_js_build :web:gdx_teavm_web_wasm_build
cd browser-check && npm ci && node check.mjs ../web/build/dist/js/webapp ../web/build/dist/wasm/webapp
```

`./gradlew :web:gdx_teavm_web_js_run` serves the page at http://localhost:8080 for a manual look.
`check.mjs` uses the installed Google Chrome; set `CHROME=/path/to/chrome` to pick another binary.

`evidence/` holds a screenshot and text dump from a local run.
