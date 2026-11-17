# Files

Sync read of packaged **internal** text/bytes through `EngineServices.files()`. Package: `io.github.worldsmiths.core`. No LibGDX types in the contract.

D9 covers packaged assets only (classpath / assetsDir). External disk paths, downloads, and AssetManager are out of scope.

## API

| Method | Meaning |
| --- | --- |
| `readString(path)` | UTF-8 text from an internal packaged path |
| `readBytes(path)` | Raw bytes from an internal packaged path |

Missing or unreadable paths throw unchecked `EngineException` with a message that includes the path.

## Usage

```java
public void create(EngineServices services) {
  String level = services.files().readString("levels/one.txt");
  byte[] blob = services.files().readBytes("data/blob.bin");
}
```

Paths use forward slashes and match how assets are packaged for the sample (typically under the game `assets/` tree).

## Honesty limits

- **Web = internal only.** TeaVM / browser builds expose packaged classpath assets; there is no local filesystem.
- Desktop/Android/iOS GDX-backed impls also use `Gdx.files.internal` only in D9 — do not rely on external/local handles through this facade.

## Miss behaviour

```java
// throws EngineException — message contains the path
services.files().readString("does-not-exist.txt");
```

## Tests

`FakeEngineServices.files()` returns `FakeFiles`. Seed content with `putInternal(path, text)` or `putInternal(path, bytes)`, then assert reads. Missing paths throw `EngineException` without a display.
