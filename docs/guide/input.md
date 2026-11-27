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
| `tick()` | Advance one frame of justPressed edges (harness / bag only — see below) |
| `load(Files, path)` | Replace-listed-only bindings from a packaged table |

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
  // restart is edge-triggered: justPressed, not isDown (held R while dying does not auto-restart)
  if (a.justPressed("restart") && dead) { restart(); }
  else if (a.isDown("move_left")) { turn(LEFT); }
  // gestures still use raw pointer:
  Input in = services.input();
  int x = in.getX();
  int y = in.getY();
}
```

Remap is replace-bind again: `a.bind("move_up", Controls.key(Key.I))` — old UP/W/pad silent; I fires.

### `tick()` ownership

- **GDX bags** sync devices and call `actions().tick()` once per frame inside `GdxEngineServices.pumpAssets` (before `game.update`).
- **Fake tests** must call `actions().tick()` themselves after setting device state and before asserting `justPressed`.
- **Games must not call `tick()`** — a second tick in `update` clears the bag's `justPressed` edges for that frame.

### Packaged `load`

`actions().load(files, path)` reads a UTF-8 packaged internal file. Line format: `action kind TOKEN` where `kind` is `key`, `pad`, or `pointer`, and `TOKEN` is a `Key` / `PadButton` / `PointerButton` enum name. Blank lines and `#` comments are ignored. Multiple lines for the same action OR together; **only listed actions are replaced** — names omitted from the file stay as-is. Missing path (or null/blank args / malformed lines) throws `EngineException` before mutating the map.

```text
# example bindings.txt
move_up key W
move_up pad DPAD_UP
restart key R
```

## Controllers / platforms

| Platform | Gamepad |
| --- | --- |
| Desktop | Controllers wired — digital buttons via `PadButton` |
| Web (TeaVM) | Controllers wired; pads may appear only after the **first press** (browser discovery) |
| Android | Controllers wired |
| iOS | Soft-absent — `EmptyGamepadSource` (bind OK; every pad poll false). No hardware pad AC in ticket 04 |

Missing or soft-absent pads never throw on bind; polls stay false until a pad is present (Fake) or Controllers reports one (GDX).

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
services.actions().tick(); // required for justPressed on Fake (do not also tick from game code)
```
