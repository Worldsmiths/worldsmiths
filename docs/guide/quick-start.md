# Quick start

Minimal lifecycle once P1 services are available. Draw2D / sample GDX scrub come in later slices.

1. Implement `WorldsmithsGame` with a no-arg constructor (launchers keep `new Game()`).
2. Build a `Scene` in `createScene()`.
3. Receive portable services in `create(EngineServices)` — store the bag; use `input()` / `clock()` / `graphics()`.
4. Prefer engine facades over `com.badlogic.gdx` in game sources as each slice lands.

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
  public void render() {}
}
```

Headless tests: pass `new FakeEngineServices()` into `create`. See [Services](services.md), [Input](input.md), [Clock](clock.md), [Graphics](graphics.md).
