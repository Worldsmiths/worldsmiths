# `@WsComponent` field-type matrix (D4)

Companion to [ADR-0007](adr/0007-teavm-component-registry.md). Authors of engine and game components need this matrix before D5 TeaVM (`reflectionEnabled=false`).

The annotation processor (`:processor`) serializes only the field types below. Anything else is a **compile error**.

## Supported fields

| Java type | JSON shape | Notes |
| --- | --- | --- |
| `boolean` | JSON boolean | |
| `int` | JSON number | |
| `long` | JSON number | |
| `float` | JSON number | |
| `double` | JSON number | |
| `String` | JSON string or `null` | Missing fields keep the no-arg constructor default |

## Skipped fields

| Modifier | Behaviour |
| --- | --- |
| `static` | Not written or read |
| `transient` | Not written or read |

## Rejected at compile time

The processor fails the build when a non-skipped field is:

- `private` (use package-private or `public`, or mark `transient`)
- `final` (must be assignable on deserialize)
- an unsupported type (arrays, collections, enums, nested value types such as `Vector2` / `Color`, inheritance of fields from a superclass)

The class itself must be `public`, non-abstract, top-level or static nested, and have a public no-arg constructor.

## Out of scope for D4

Nested value types, arrays/collections, enums, asset references beyond a plain `String` key, inherited fields, and editor property schema are deferred (see ADR-0007 consequences).
