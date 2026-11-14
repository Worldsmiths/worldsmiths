# ADR-0004: Java baseline

- Status: accepted
- Date: 2026-11-12
- Deciders: founder
- Related: HLD Q1, [R02 §1.2]

## Context

RoboVM 2.3, the stable libGDX iOS path, accepts Java 8 syntax and Java 7 APIs. RoboVM 3's newer runtime is experimental. Desktop, Android and TeaVM can all consume Java 8 bytecode. The build itself (Gradle, AGP, Construo) needs JDK 17 or newer, and JDK 21 is the practical default.

## Options considered

1. **Runtime at Java 8 syntax and Java 7 APIs. Tooling on Java 21.** Matches libGDX's own core and keeps the stable iOS backend available.
2. **Java 11 for the runtime**, depending on RoboVM 3.
3. **Java 17 for the runtime**, which drops stable iOS until RoboVM 3 or Multi-OS Engine is proven.

## Decision

Option 1. Modules under `runtime/` and the platform launchers compile with `--release 8` and stay within Java 7 APIs. The Gradle plugin, editor and CLI target Java 21. The build JDK is 21.

Revisit when RoboVM 3 leaves experimental status, using the iOS spike (S3) as the first evidence.

## Consequences

- CI compiles the runtime once per target and fails on Java 8+ library APIs (for example `java.util.List.of`, `var`, records).
- Kotlin game code is still allowed (ADR-0012); the engine's own sources stay Java.
