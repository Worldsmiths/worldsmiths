# Worldsmiths author guide

How-to pages for engine facades. ADRs under [`../adr/`](../adr/) record decisions; this folder is the author-facing how-to.

## Pages

| Page | Status |
| --- | --- |
| [Quick start](quick-start.md) | Stub (fills as samples migrate) |
| [Services overview](services.md) | P1 bag + getters |
| [Input](input.md) | P1 + actions / remap |
| [Clock](clock.md) | P1 |
| [Graphics](graphics.md) | P1 |
| [Draw2D](draw2d.md) | D9a-S2 |
| [Files](files.md) | D9a-S3 |
| [D3](d3.md) | D9b + glTF bind / instance materials |
| [Audio](audio.md) | Jam facade (relay) |
| [Ui](ui.md) | Jam facade (relay) |
| [Textures](textures.md) | Sync texture cache (textured sprites) |
| [Assets](assets.md) | Async packaged load (TEXTURE + MESH glTF; MUSIC stub) |
| [Log](log.md) | Portable debug / info / warn / error |
| [Scene flow](scene-flow.md) | Replace-only load / switchTo / unload / current |
| [UiKit](uikit.md) | Engine UI slice (hybrid leaves) |

Each facade ships its guide page in the same change-set as the API. Cursor skills / agent knowledge later consume these pages (no second source of truth).
