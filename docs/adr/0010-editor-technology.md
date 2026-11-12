# ADR-0010: Editor technology

- Status: accepted
- Date: 2026-11-12
- Deciders: founder
- Related: HLD Q4, [R05 §1]

## Context

The editor needs a live view of the game scene, which is an OpenGL framebuffer, plus docking, inspectors and transform gizmos. Building that beside libGDX means bridging a second UI toolkit into the GL window.

## Options considered

1. **A standalone desktop app written in libGDX, with imgui-java for the panels.** The viewport is a framebuffer texture inside the same window. Docking and a 3D gizmo already exist. Mundus shows that a libGDX editor is viable.
2. **scene2d and VisUI.** The engine's own UI, but docking and gizmos would be built by hand. A reasonable later migration for panels.
3. **JavaFX, Compose Desktop, or a viewport embedded in an IntelliJ plugin.** Each needs an OpenGL bridge. The main JavaFX bridge is archived, and Compose's Swing embedding draws on top of the game view.

## Decision

Option 1 for the editor through v1. The IntelliJ plugin is a companion for new projects, run configurations and debugging. It does not host the scene view.

## Consequences

- imgui-java is a desktop editor dependency. It is not shipped inside games.
- Moving panels to scene2d stays possible later and is not a v1 goal.
- Spike S5 proves the framebuffer-plus-gizmo window before editor feature work starts.
