# ADR-0001: License now, contributor terms at the first outside contribution

- Status: accepted
- Date: 2026-11-12
- Deciders: founder
- Related: HLD Q6

## Context

The founder is the only author until v1. The other contributors until then are AI agents working in the founder's session, so every commit is the founder's. A contributor agreement has nothing to attach to until someone else sends a pull request. The license still matters now, because it covers the code the moment the repo is public.

## Options considered

1. **Apache-2.0 now, decide DCO or CLA later.** A `LICENSE` file is the only thing required to build, run and publish. The agreement is chosen before the first external pull request.
2. **DCO now.** GitHub's "Require contributors to sign off on web-based commits" setting. One checkbox, no account with a third party. Adds a `Signed-off-by` trailer to every commit, including the founder's.
3. **CLA now.** A contributor license agreement (CLA Assistant or EasyCLA) lets the project relicense or dual-license later. It is the wrong tool while there is only one author, and it cannot be applied backwards to someone who already contributed.

## Decision

Apache-2.0 from the first commit. No DCO and no CLA until the project is about to accept a contribution from someone other than the founder.

At that point, pick one:

- **DCO** (recommended). Enough for a permissive license, and it matches libGDX's peers that chose sign-off over a CLA. Turn on the GitHub org setting; no bot is required.
- **CLA**, only if commercial dual-licensing or relicensing is a real plan. It has to be in place before the first external pull request, because a contributor's code cannot be put under a CLA after the fact. Waiting is safe only while the founder is the sole author, which is true today.

## Consequences

- Add `LICENSE` (Apache-2.0) and a `NOTICE` when the monorepo is created. Do not add a sign-off check to CI.
- AI-assisted commits need no separate agreement. The founder is the author of record.
- Revisit on the day the first external contributor is invited, which is planned for around v1 and not before.
