# Audio

One-shot sound load and playback through `EngineServices.audio()`. Package: `io.github.worldsmiths.core`. No LibGDX types in the contract. Streaming Music is out of scope.

Loads use packaged **internal** asset paths only (same ceiling as [Files](files.md)).

## Ownership / dispose

`Audio` from the bag is a **factory**, not a disposable resource. Authors own dispose of every `Sound` they create (`sound.dispose()` when finished). Safe to call more than once.

## API

| Type / method | Meaning |
| --- | --- |
| `createSound(internalPath)` | Load a one-shot sound; returns an author-owned `Sound` |
| `Sound.play()` | Start playback (best-effort; must not fail the game) |
| `Sound.stop()` | Stop playback (best-effort) |
| `Sound.dispose()` | Release native resources when finished |

Missing or unloadable paths throw unchecked `EngineException` with a message that includes the path. Do not call `play` on a failed create.

## Usage

```java
private Sound cue;

public void create(EngineServices services) {
  cue = services.audio().createSound("audio/leg-complete.wav");
}

// on an event:
cue.play();

public void dispose() {
  if (cue != null) {
    cue.dispose();
    cue = null;
  }
}
```

## Honesty limits

- **Play / stop are best-effort.** Device or decode glitches must not throw out of the facade to fail the game.
- **Internal paths only.** Same packaging rules as Files — classpath / assetsDir; no external disk paths through this facade.
- No Music / streaming API in this slice.

## Miss behaviour

```java
// throws EngineException — message contains the path
services.audio().createSound("does-not-exist.wav");
```

## Tests

`FakeEngineServices.audio()` returns `FakeAudio`. Seed with `putSound(path)`, then assert command kinds (`CREATE_SOUND`, `PLAY`, `STOP`, `DISPOSE`) via `commands()` without a display or audio device.
