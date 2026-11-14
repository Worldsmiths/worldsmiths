# ADR-0003: Coordinates now, registration and publishing at the last step before v1

- Status: accepted
- Date: 2026-11-12
- Deciders: founder
- Related: HLD Q3, [Central Portal namespace docs](https://central.sonatype.org/register/namespace/), [coordinates](https://central.sonatype.org/publish/requirements/coordinates/)

## Context

The engine has to build and run long before v1, and the founder does not want to buy a domain or register a publishing account yet. Maven Central releases are immutable, so the published `groupId` is permanent once the first release is uploaded. Nothing forces that upload to happen before v1.

Two different names get confused here:

- The **Java package** (`io.github.worldsmiths`) appears in every source file. Renaming it gets more expensive with every week of code.
- The **Maven `groupId` and Gradle plugin id** appear in the version catalog, the BOM and the plugin descriptor. Renaming those before anyone depends on a published build is a one-line change.

Central Portal grants `io.github.<username>` automatically only to the personal GitHub account that signs in. It does not automatically grant `io.github.worldsmiths` to the GitHub organization. An organization namespace is a separate verification, done by proving control of the org, and it needs no domain. A custom group such as `dev.worldsmiths` needs a domain and a DNS TXT record.

## Options considered

1. **Don't publish. Build through Gradle included builds.** Template and samples depend on the engine with `includeBuild`, not on Maven coordinates. No Central account, no Plugin Portal account, no domain. This is how the engine runs until the first public release.
2. **Publish under `io.github.worldsmiths`** (no purchase). Verify the namespace through the Worldsmiths GitHub org when the time comes. Treat it as permanent if chosen, because users will depend on it.
3. **Buy a domain and publish under its reverse name** (for example `dev.worldsmiths`). Cleaner brand. Costs a domain and a DNS TXT record, and has to happen before the first Central upload, never after.
4. **GitHub Packages only.** Works with `GITHUB_TOKEN` and any groupId, but every user must add a repository URL. Weaker than Central for a library people are meant to find.

## Decision

Until the last step before v1, option 1.

- Java package is `io.github.worldsmiths`, chosen now and kept even if the published groupId later differs. It needs no registration.
- Working groupId is `io.github.worldsmiths`. Working plugin id is `io.github.worldsmiths.engine`. Both live in one place, `gradle/libs.versions.toml`, so they can still be changed before the first upload.
- Template and samples consume the engine with `includeBuild`. Their CI checks out the monorepo. No artifact repository is required to build, test or run.
- No Central Portal account, Plugin Portal account, or domain until the pre-v1 publishing step.

At that step, pick option 2 or option 3 and record the choice in this ADR:

- **Option 2** if a domain is still unwanted. Confirm that Central will verify `io.github.worldsmiths` for the organization (the automatic namespace is the personal user, not the org). If verification fails, fall back to option 3 rather than publishing under a personal username.
- **Option 3** if a branded groupId is worth a domain by then. Buy it then, not before. Keep the Java package as `io.github.worldsmiths` either way, so the choice does not rewrite the source tree.

Option 4 is a fallback only if Central verification is blocked. It is not a target.

## Consequences

- Phase 0 does not include buying a domain or opening publishing accounts. Those items move to the v1 release checklist.
- The HLD diagram shows Maven Central as the steady state after that step. Before it, nothing is published and samples do not download Worldsmiths artifacts.
- A groupId change after the first release someone depends on is a migration, not a rename. Don't publish snapshots for anyone outside the included builds before the choice is made.
