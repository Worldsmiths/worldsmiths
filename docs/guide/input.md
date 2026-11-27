# Input

Poll keyboard and the primary pointer through `EngineServices.input()`, and named digital actions through `EngineServices.actions()`. Package: `io.github.worldsmiths.core`. No LibGDX types in the contract.

## Raw input API

| Method | Meaning |
| --- | --- |
| `isKeyDown(Key key)` | Key currently held |
| `isTouched()` | Primary pointer currently down |
| `justTouched()` | Primary pointer went down this frame |
| `getX()` / `getY()` | Primary pointer in screen pixels (origin top-left) |

Keys are the engine `Key` enum (`LEFT`/`RIGHT`/`UP`/`DOWN`, letters, digits, `SPACE`, `ENTER`, modifiers, …). Backends map these to platform codes; games never import GDX input constants.

Raw `input()` stays available forever for pointer coords and author-owned gestures (Snake swipe helpers still use `getX`/`getY` / touch).

## Actions API

| Method | Meaning |
| --- | --- |
| `bind(action, Control…)` | Replace-all bindings for the name |
| `isDown(action)` | Any bound control currently held (soft-false if unknown) |
| `justPressed(action)` | Transitioned up→down on the most recent `tick()` |
| `tick()` | Advance one frame of justPressed edges |

Controls are built with `Controls.key(Key)`, `Controls.gamepadButton(PadButton)`, and `Controls.pointerButton(PointerButton)`. Multi-bind is OR; a later `bind` for the same name drops the previous list (remap).

Engine-seeded UI names: `ui_focus_next` (TAB), `ui_focus_prev` (Shift+TAB), `ui_accept` (Enter / Space).

```java
public void create(EngineServices services) {
  Actions a = services.actions();
  a.bind("move_up", Controls.key(Key.UP), Controls.key(Key.W),
      Controls.gamepadButton(PadButton.DPAD_UP));
  a.bind("restart", Controls.key(Key.R), Controls.key(Key.SPACE),
      Controls.gamepadButton(PadButton.START));
}

public void update(float delta) {
  Actions a = services.actions();
  a.tick(); // once per frame before justPressed polls
  if (a.justPressed("restart") && dead) { restart(); }
  else if (a.isDown("move_left")) { turn(LEFT); }
  // gestures still use raw pointer:
  Input in = services.input();
  int x = in.getX();
  int y = in.getY();
}
```

Remap is replace-bind again: `a.bind("move_up", Controls.key(Key.I))` — old UP/W/pad silent; I fires.

## Tests (Fake device path)

`FakeEngineServices` shares one `DeviceState` between `input()` and `actions()`:

- `input().setKeyDown(Key, boolean)` and `actions().setKeyDown(Key, boolean)` both drive key Controls
- `actions().setPadButtonDown(PadButton, boolean)` / `setPointerButtonDown(PointerButton, boolean)` for other device classes
- Dual-device and remap proofs must actuate bound Controls this way — not only `actions().setActionActive` (that shortcut is for logic-only tests)

```java
FakeEngineServices services = new FakeEngineServices();
game.create(services);
services.input().setKeyDown(Key.UP, true);           // key alone through bindings
services.actions().setPadButtonDown(PadButton.DPAD_UP, true); // pad alone
services.actions().bind("move_up", Controls.key(Key.I));      // remap
```
