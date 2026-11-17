# Clock

Frame time and optional total elapsed via `EngineServices.clock()`. Package: `io.github.worldsmiths.core`.

## API

| Method | Meaning |
| --- | --- |
| `delta()` | Seconds since the previous frame |
| `total()` | Seconds since app start (or since the backend reset the clock) |

`WorldsmithsGame.update(float delta)` still receives delta each frame. Use `Clock` when you also want `total()`, or a second read of delta outside `update`.

## Usage

```java
public void update(float delta) {
  Clock clock = services.clock();
  // Prefer the update parameter for frame logic; clock.delta() matches it on real backends.
  float elapsed = clock.total();
}
```

## Tests

`FakeEngineServices.clock()` returns a mutable `FakeClock`: `setDelta`, `setTotal`.
