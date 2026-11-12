# ADR-0005: Object model, and no ECS in 1.0

- Status: accepted
- Date: 2026-11-12
- Deciders: founder
- Related: HLD Q5, [R03 §3.1], [R04 ECS]

## Context

The engine has to feel like a general game engine, hide its machinery, and also serve simulations with many similar objects. Godot, Unity, Unreal and Defold all present a tree of objects. Bevy is pure ECS, and the JVM ECS libraries that would back that choice are dormant except Fleks, which is Kotlin-first. libGDX runs game code on one GL thread, so a parallel ECS pays less back than it does in Rust.

## Options considered

1. **A scene tree of nodes that carry components, backed by services that own the data.** Users work with nodes. An ECS module is a later addition for simulations that need it.
2. **Fleks**, which forces a Kotlin-first engine.
3. **An in-house component store in 1.0**, before there is a benchmark showing the scene tree is too slow.

## Decision

Option 1. Version 1.0 ships the scene tree and services only. `ws-ecs` is post-1.0 and starts from benchmarks, not from a library choice.

## Consequences

- Requirements FR-S01 to FR-S06 are in scope. FR-S07 is out of 1.0.
- Services are the extension point for physics, rendering and audio, so a later ECS can sit beside them instead of replacing them.
