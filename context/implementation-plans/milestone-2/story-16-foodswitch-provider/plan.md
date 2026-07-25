# Plan: FoodSwitch Provider (feasibility-gated)

## Metadata

- Task Type: `FEATURE`
- Status: `Draft`
- Owner: Jarryd Adaens
- Last Updated: 25 July 2026

## Linked Context

- Milestone: [context/milestones/milestone-2.md](../../../milestones/milestone-2.md)
- Story: [Story 16: FoodSwitch provider (feasibility-gated)](../../../milestones/milestone-2.md#story-16)
- Dictation source: [2026-07-25 Milestone 2 feature spec](../../../dictations-tier-0/2026-07-25_milestone-2_australian-providers-and-quickadd-spec.md) (Phase 3)
- Related Plans:
  - [Story 14 (spike)](../story-14-provider-quickadd-architecture-spike/plan.md) — **gate**: its feasibility verdict decides whether this story is built or recorded as a blocker.
  - [Story 15 (AFCD)](../story-15-australian-food-composition-database-provider/plan.md) — provides the reusable pipeline this story reuses.

## CER

- Complexity: 4
- Effort: 3
- Risk: 5
- Notes: Inline estimate. Effort/complexity are low *conditional on access existing* — implementation is largely "another provider on Story 15's pipeline." Risk is 5 and mostly external: FoodSwitch (The George Institute) may not offer permitted bulk/API access, in which case the correct deliverable is a documented blocker, not code. The forbidden fallback (scraping supermarkets) is an explicit spec non-goal.

## Objective

If — and only if — Story 14 confirms a usable and permitted FoodSwitch data-access mechanism, add `foodswitch_australia` as a provider on the shared pipeline (packaged-food dataset, local storage, search participation, barcode lookup where supplied). Otherwise, record a concrete, evidence-backed access blocker and stop.

## Scope

### In Scope (only if feasible)

- New provider identity `FoodSource.Type.FoodSwitchAustralia` (+ Room mirror + converters), reusing Story 15's download/validate/replace pipeline.
- FoodSwitch parser + `ProductField` mapper (barcode, brand, product name, package/serving size, energy, protein, fat, carb, fibre, per-serving + per-100 g/mL, category, provenance, version).
- Barcode import + lookup where the source supplies barcodes (strings, leading zeros preserved, no locale formatting); consider the `barcode` index deferred by Story 15.
- Enable/disable + search participation + import/update state UI.

### In Scope (if NOT feasible)

- A documented blocker: what was tried, the exact access terms/limits found, and why bulk acquisition is not permitted — appended to Story 14's note and reflected in this story's status.

### Out Of Scope

- Any supermarket scraping or loyalty-card circumvention (spec non-goal).
- The shared pipeline itself (Story 15) and the update-check UI (Story 17).

## Non-Goals

- Substituting scraped or alternative data if FoodSwitch access fails — the story becomes a blocker, not a workaround.
- Cross-provider duplicate merging with OFF/AFCD.

## Current Understanding

- FoodSwitch is an Australian packaged-food/barcode source from The George Institute. Public bulk-download or unrestricted API availability is **unconfirmed and believed restricted** — this is exactly what Story 14 must verify (auth, personal-use terms, rate limits, bulk permission, barcode/nutrition coverage, version metadata).
- If feasible, the implementation reuses Story 15's extracted pipeline (download→temp→validate→transactional replace) and the enum fan-out list documented there; barcode handling is the main provider-specific concern (the current `barcode` column is an unindexed `String?` with `LIKE` search — a barcode-heavy dataset likely needs the index Story 15 deferred).
- Diary entries are already immutable snapshots, so refreshes will not mutate history (same guarantee as AFCD).

## Questions / Unknowns

- Q: Does FoodSwitch expose a usable, permitted bulk download or API for personal use?
  Impact: Binary gate for this entire story.
  Assumption: Likely restricted; treat "blocked" as the probable outcome until Story 14 proves otherwise. Do not begin implementation before the verdict.
  Status: OPEN — owned by Story 14.
- Q: If accessible, what identifies a dataset version (explicit version, publication date, record update date)?
  Impact: Story 17's version comparison for this provider.
  Assumption: Use the strongest identifier the source exposes; fall back to checksum.
  Status: OPEN.
- Q: Add the `barcode` index as part of this story?
  Impact: Search/dedup performance on a barcode-heavy dataset.
  Assumption: Yes if implemented — this is the provider that makes the index worthwhile.
  Status: OPEN.

## Execution Steps

> All implementation steps are gated on Story 14 returning "feasible". If not, execute only Step 0.

0. Record the feasibility verdict.
   - Why: The spec requires access limitations documented either way.
   - Edits: update this plan's Status + the milestone story to "Blocked — <reason>" with evidence, or "Ready" to proceed.
1. Add the provider identity (enum fan-out per Story 15's checklist).
2. Implement the FoodSwitch provider slice on Story 15's pipeline (download source, parser, `ProductField` mapper, unit normalization, provenance + source-record-ID).
3. Barcode handling: import/index barcodes as strings (leading zeros preserved); add the `barcode` index; wire the existing barcode search path.
4. Enablement + settings + navigation UI + import/update state.
5. Tests per §10.1–10.2 (as Story 15), plus barcode preservation/lookup specifically.

## Validation

### Automated Checks

- KMP unit + integration tests (as Story 15) incl. barcode preservation and lookup.
- Build compiles (enum fan-out).

### Manual Checks

1. (If built) Enable, import, search a packaged food, scan/look up a barcode, confirm provenance.
2. (If built) Refresh; confirm only FoodSwitch rows replaced and history untouched.
3. (If blocked) Confirm the documented blocker names the access mechanism attempted and the exact limitation.

### Acceptance Criteria

- Feasibility is verified before implementation; access limitations documented.
- If built: provider uses the common pipeline, retains source identity, imports barcodes only when supplied, enablement controls search, refreshes don't mutate history, failed refreshes preserve the last-good dataset.
- If blocked: a concrete external blocker is recorded; **no** scraped data is introduced.

## Risk Mitigation

- Risk: Building before confirming access → wasted work / unusable feature.
  Mitigation: Hard gate on Story 14; Step 0 precedes all implementation.
- Risk: Pressure to substitute scraped data when access fails.
  Mitigation: Explicit non-goal; the correct deliverable is a documented blocker.
- Risk: Barcode-heavy dataset degrades search.
  Mitigation: Add the `barcode` index with this provider; profile.

## Evidence / References

- Planning input: 2026-07-25 provider recon (barcode storage, `insertUniqueProduct` dedup, enum fan-out) with `file:line`; spec Phase 3, §8.3.
- Unverified: FoodSwitch access feasibility — the central unknown, owned by Story 14.
