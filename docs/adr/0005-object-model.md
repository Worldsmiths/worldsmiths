# ADR-0005: Object model — scene tree + services; ECS beside (opt-in)

- Status: accepted (amended 2026-11-19)
- Date: 2026-11-12
- Deciders: founder
- Related: HLD Q5, [R03 §3.1], [R04 ECS]; ladder D10; `.agent-docs/hld-planner-post-d9-roadmap/`

## Context

The engine has to feel like a general game engine, hide its machinery, and also serve simulations with many similar objects. Godot, Unity, Unreal and Defold all present a tree of objects. Bevy is pure ECS, and the JVM ECS libraries that would back that choice are dormant except Fleks, which is Kotlin-first. libGDX runs game code on one GL thread, so a parallel ECS pays less back than it does in Rust.

## Options considered

1. **A scene tree of nodes that carry components, backed by services that own the data.** Users work with nodes. An ECS module is a later addition for simulations that need it.
2. **Fleks**, which forces a Kotlin-first engine.
3. **An in-house component store in 1.0**, before there is a benchmark showing the scene tree is too slow.

## Decision

Option 1. The **default author model** is the scene tree + services (`EngineServices`). `ws-ecs` is an **optional module beside** services — not a replacement of the tree, and not a required bag getter.

**Amendment (2026-11-19):** The engine ladder may introduce `ws-ecs` in **0.x as D10** when a living probe (incremental showcase) needs it — still opt-in, game-owned World, bag floor unchanged. “Post-1.0 only” is no longer a hard calendar ban; **1.0 may still ship without requiring ECS** for every game. Library choice and benchmarks still gate *how* ECS is implemented, not whether the milestone may exist in 0.x.

## Consequences

- Requirements FR-S01 to FR-S06 stay the default path. FR-S07 (ECS) is opt-in, not the 1.0 floor.
- Services remain the extension point for physics, rendering and audio; ECS sits beside them.
- Minimal/Snake/rollball may stay non-ECS while a showcase opts in.
