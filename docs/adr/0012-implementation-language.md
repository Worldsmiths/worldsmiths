# ADR-0012: Implementation language

- Status: accepted
- Date: 2026-11-12
- Deciders: founder
- Related: HLD Q2, [R02 §1.3], [R04 KTX]

## Context

The engine targets TeaVM (which compiles Kotlin) and RoboVM, and explicitly not GWT (which cannot compile Kotlin). KTX, the usual Kotlin layer over libGDX, has not published a release for libGDX 1.14. A Kotlin engine would put the Kotlin standard library on the classpath of every game, including Java ones.

## Options considered

1. **Java for the engine. Kotlin is a supported language for game code.**
2. **Kotlin for the engine**, compiled to Java 8 bytecode.
3. **Java runtime with Kotlin tooling** (plugin, CLI, editor). Splits the codebase in two for a small gain.

## Decision

Option 1. Engine modules are Java. A game may be Java or Kotlin, and the TeaVM web build is the one that has to accept both. KTX is not a dependency.

## Consequences

- Code style, Spotless and the public API are Java.
- Kotlin samples live in `samples` and are part of the web and desktop CI once the Kotlin template exists.
- A Kotlin DSL for the Gradle plugin is still fine: Gradle plugin code is tooling, not the runtime, and it is not shipped in games.
