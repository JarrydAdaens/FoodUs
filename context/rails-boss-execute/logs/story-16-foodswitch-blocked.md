# STORY 2.16 — FoodSwitch provider: BLOCKED (external licence)

Date: 2026-07-25. Gate evaluated by Story 14's spike (commit `5ab699ed`,
`context/wiki/provider-quickadd-architecture.md`); blocker recorded in the story plan
(commit `5819da3e`).

## Blocker
No public FoodSwitch API or bulk download exists, and the FoodSwitch Global Terms of Use
(clauses 7.1, 7.2(a–b, e–f), 9.2/9.3, 9.10, 9.12) forbid reproducing, storing, distributing,
or deriving the data. A local offline-first provider cannot be built within the licence.

## Non-goal honored
No scraped supermarket-data substitute was built (explicit milestone non-goal).

## Unblock condition (owner-only)
Obtain a written data-licence agreement from The George Institute
(`foodswitch@georgeinstitute.org.au`). If granted, Story 16 reuses Story 15's provider
pipeline (commit `1c615992`).

## Note on status
The milestone story text defines this outcome as valid completion of the gate ("record the
concrete blocker and stop"), so the documentation deliverable is DONE while the feature
itself is BLOCKED externally. Retry only after the owner secures the licence.
