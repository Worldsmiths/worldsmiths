# Input

Poll keyboard and the primary pointer through `EngineServices.input()`. Package: `io.github.worldsmiths.core`. No LibGDX types in the contract.

## API

| Method | Meaning |
| --- | --- |
| `isKeyDown(Key key)` | Key currently held |
| `isTouched()` | Primary pointer currently down |
| `justTouched()` | Primary pointer went down this frame |
| `getX()` / `getY()` | Primary pointer in screen pixels (origin top-left) |

Keys are the engine `Key` enum (`LEFT`/`RIGHT`/`UP`/`DOWN`, letters, digits, `SPACE`, `ENTER`, modifiers, …). Backends map these to platform codes; games never import GDX input constants.

## Usage

```java
public void update(float delta) {
  Input in = services.input();
  if (in.isKeyDown(Key.LEFT)) { /* … */ }
  if (in.justTouched()) {
    int x = in.getX();
    int y = in.getY();
  }
}
```

Hold the bag from `create(EngineServices)` (or call `services.input()` each frame). Polling only — no event/processor bridge yet.

## Tests

`FakeEngineServices.input()` returns a mutable `FakeInput`: `setKeyDown`, `setTouched`, `setJustTouched`, `setPointer`.
