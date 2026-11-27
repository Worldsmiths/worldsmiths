# Quick start

Minimal lifecycle with portable services. Snake and rollball are GDX-free (input / graphics / Draw2D / Thin3D via the bag). Multi-scene hops: see [Scene flow](scene-flow.md) and the [`samples/flow`](../../samples/flow/) demo.

1. Implement `WorldsmithsGame` with a no-arg constructor (launchers keep `new Game()`).
2. In `create(EngineServices)`, establish the first scene via `services.scenes().load(...)` or `switchTo(...)` — there is no `createScene`.
3. Receive portable services in `create(EngineServices)` — store the bag; use `input()` / `clock()` / `graphics()` / `draw2d()` / `files()` / `thin3d()` / `audio()` / `ui()` / `textures()` / `assets()` / `log()` / `scenes()`.
4. Prefer engine facades over `com.badlogic.gdx` in game sources.

```java
public final class MyGame implements WorldsmithsGame {
  private EngineServices services;

  @Override
  public void create(EngineServices services) {
    this.services = services;
    services.scenes().load("scenes/menu.json"); // or switchTo(new Scene())
    services.graphics().setTitle("My game"); // best-effort; Android may ignore
  }

  @Override
  public void update(float delta) {
    if (services.input().isKeyDown(Key.LEFT)) { /* … */ }
    // Later: services.scenes().load("scenes/room_b.json");
  }

  @Override
  public void render() {
    Draw2D g = services.draw2d();
    g.setOrtho2D(0f, 0f, services.graphics().getWidth(), services.graphics().getHeight());
    g.begin(DrawMode.LINE);
    g.setColor(1f, 1f, 1f, 1f);
    g.line(0f, 0f, 100f, 0f);
    g.end();
  }

  @Override
  public void dispose() {
    services.draw2d().dispose();
  }
}
```

Headless tests: pass `new FakeEngineServices()` into `create`. See [Services](services.md), [Scene flow](scene-flow.md), [Input](input.md), [Clock](clock.md), [Graphics](graphics.md), [Draw2D](draw2d.md), [Files](files.md), [Thin3D](thin3d.md), [Audio](audio.md), [Ui](ui.md), [Textures](textures.md), [Assets](assets.md), [Log](log.md).
