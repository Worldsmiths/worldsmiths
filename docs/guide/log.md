# Log

Portable logging through `EngineServices.log()`. Package: `io.github.worldsmiths.core`. No LibGDX types in the contract. Not SLF4J or structured telemetry.

Use this instead of `System.out` or `Gdx.app` for game and engine log lines.

## API

| Method | Meaning |
| --- | --- |
| `debug(message)` / `debug(tag, message)` | Debug-level emit |
| `info(message)` / `info(tag, message)` | Info-level emit |
| `warn(message)` / `warn(tag, message)` | Warn-level emit (see honesty) |
| `error(message)` / `error(tag, message)` | Error-level emit |
| `error(message, Throwable)` / `error(tag, message, Throwable)` | Error with cause |

Untagged overloads omit an author tag. Tagged overloads pass the tag through to the sink (or record it on `FakeLog`).

## Usage

```java
public void create(EngineServices services) {
  services.log().info("scene ready");
  services.log().warn("Sprites", "missing optional overlay");
}
```

## Honesty limits

- **Warn shares the GDX info channel.** On the GDX-backed impl, `warn` becomes an info-channel call whose message starts with `WARN: `. There is no distinct OS warn channel in this slice.
- **Untagged → sink tag `WS`.** On the GDX-backed impl, untagged calls use default sink tag `"WS"`. `FakeLog` records a `null` author tag for untagged overloads so tests can tell them apart from an explicit tag.
- **Null/blank tagged tag → `WS` on GDX.** A tagged overload with a null or blank (trim-empty) tag still reaches the GDX sink as `"WS"`; `FakeLog` may record the null author tag as passed. Prefer the untagged overload instead of a null tag.
- **`error` overload null ambiguity.** `error(String, String)` vs `error(String, Throwable)` is ambiguous when the second arg is null — cast to `String` or `Throwable` to pick an overload; prefer the three-arg form when both tag and cause are present.
- **Best-effort null message.** A null message is treated as `""`; implementations must not throw from the emit path for a null message.
- **Standing rule:** do not use `System.out` or `Gdx.app` for game/engine logging — go through `services.log()`.

## Tests

`FakeEngineServices.log()` returns `FakeLog`. Assert ordered `commands()` (level, message, optional author tag, optional cause) without a platform console. Call `clear()` between cases when reusing one fake.
