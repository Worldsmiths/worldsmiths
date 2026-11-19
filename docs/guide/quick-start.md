# Quick start

Minimal lifecycle with portable services. Snake and rollball are GDX-free (input / graphics / Draw2D / Thin3D via the bag).

1. Implement `WorldsmithsGame` with a no-arg constructor (launchers keep `new Game()`).
2. Build a `Scene` in `createScene()`.
3. Receive portable services in `create(EngineServices)` — store the bag; use `input()` / `clock()` / `graphics()` / `draw2d()` / `files()` / `thin3d()` / `audio()`.
4. Prefer engine facades over `com.badlogic.gdx` in game sources.

```java
public final class MyGame implements WorldsmithsGame {
  private EngineServices services;

  @Override
  public Scene createScene() {
    return new Scene();
  }

  @Override
  public void create(EngineServices services) {
    this.services = services;
    services.graphics().setTitle("My game"); // best-effort; Android may ignore
  }

  @Override
  public void update(float delta) {
    if (services.input().isKeyDown(Key.LEFT)) { /* … */ }
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

Headless tests: pass `new FakeEngineServices()` into `create`. See [Services](services.md), [Input](input.md), [Clock](clock.md), [Graphics](graphics.md), [Draw2D](draw2d.md), [Files](files.md), [Thin3D](thin3d.md), [Audio](audio.md).
