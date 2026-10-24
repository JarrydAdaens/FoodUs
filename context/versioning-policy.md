---
name: versioning-policy
description: FoodUs four-part product version policy, tracker ownership, and Android release transition.
metadata:
  version: "2.1"
  owner: "Jarryd Adaens"
  repo: "FoodUs (fork of maksimowiczm/FoodYou)"
---
# FoodUs Versioning Policy

FoodUs adopts **Generation.Milestone.StoriesClosed.ReleaseCount** from the supplied
Agentic Rails starter's product policy 1.1. This policy supersedes the earlier
`<milestone>.<story>.<build>` convention prospectively. Published versions, tags, and
historical records keep their original identities. Document metadata versions and
the starter version are independent of the app version.

## Components and tracker ownership

| Component | FoodUs rule |
| --- | --- |
| Generation | `1` for the existing FoodUs product generation. A substantial rebuild or successor starts a new generation by an explicit product decision; a channel change or tracker correction does not. |
| Milestone | The highest numbered milestone actively In Progress in [Design's Milestones Index](design.md#milestones-index). Milestone B does not change this component. |
| StoriesClosed | Closed Stories in the current numbered milestone **plus closed Stories in Milestone B**, counting each distinct Story once. Closed statuses remain Done, Complete, Transferred, or Abandoned. When Milestone advances, use the new numbered milestone's closed count plus the cumulative B contribution. |
| ReleaseCount | Published releases at that exact generation, milestone, and closed-count coordinate. An unpublished coordinate starts at `0`; the first publication uses `1`. Increment for each publication; local builds do not increment it. Reset to `0` when any preceding component changes, subject to the collision rule below. |

The [milestone documents](milestones/README-MILESTONES.md) own Story status; their
Story Index and detailed Story sections must agree. Resolve any disagreement before
deriving a version. Plans record execution evidence and do not independently close a
Story. Count each stable Story identity once, regardless of its number or completion
order. Partial implementation, a blocked dependency, or moving a backlog entry does
not establish closure.

FoodUs's **Externalized** relay Stories remain open while awaiting their contract or
deployment gate. Externalized is not a synonym for the terminal Transferred state.
The relay repository owns server delivery; its closed Stories do not contribute to
the FoodUs app's count. Reopening a Story that belongs only to an older numbered
milestone does not change the version while a higher numbered milestone remains active.

## Shared source and consumers

[`gradle/libs.versions.toml`](../gradle/libs.versions.toml) is the single shared version
source. After the transition below, `fork-version-name` holds the canonical four-part
value, using nonnegative decimal integers without leading zeros. Do not maintain a
separate running Story count or duplicate app version in documentation.

| Consumer | Mapping and constraints |
| --- | --- |
| Android APK | `app/build.gradle.kts` takes `versionName` directly from `fork-version-name`. Four components are kept verbatim; Android versionName is a string. |
| Product display | BuildConfig `FORK_VERSION_NAME` comes from the same catalog property; `FoodYouConfig.forkVersionName` supplies About and contact details. |
| Release tag and asset | Tag is exactly `v<fork-version-name>`; `.github/workflows/release-apk.yml` checks equality and names the asset `FoodUs-<tag>.apk`. No zero removal or component truncation is permitted. |
| Android installation order | `android-versionCode` is a separate installation sequence, not a packed four-part version. The build consumes it with `toInt()`: use a positive Kotlin Int (`1` through `2147483647`). Increase above every installed or distributed code for each distributed build; on a shipped upstream merge use at least `max(upstream, ours) + 1`. |
| Upstream attribution | `version-name` and BuildConfig `VERSION_NAME` retain the upstream Food You version. They do not track FoodUs delivery. |
| Other package formats | No four-part mapping is adopted for a separate iOS, SemVer, or other packaged release. Define bounds, ordering, normalization, and collision handling before shipping one; build metadata alone is not a unique package release identity. |

Android uses versionCode for downgrade protection and allows a descriptive versionName
string. See [Android's versioning contract](https://developer.android.com/studio/publish/versioning).
Debug and preview builds retain their configured suffixes; the household artifact is
the release build. The application ID `io.github.jarrydadaens.foodus` and release signing
certificate remain unchanged for in-place updates.

## Transition from existing releases

This context update preserves the current catalog values as a **temporary legacy
exception**. It neither publishes a release nor reinterprets a historical three-part
value as a four-part value. When preparing the next release, replace `fork-version-name`
with the tracker-derived four-part value and advance versionCode. From that point,
maintain draft versions when tracked Stories close or reopen or the active milestone
changes; those events alone do not authorize a release or increment ReleaseCount.

Before the first four-part publication:

1. Read the authoritative trackers afresh and derive the generation, active numbered
   milestone, and combined closed count from that milestone and Milestone B. Include
   terminal closures only; do not use the highest Story ID.
2. Check the complete GitHub Releases/tag history and codes installed on both phones.
   Local Git tags are only a partial ledger. Do not reuse a published identifier or code.
3. Preflight Obtainium's selected-release and installed-version comparisons against an
   isolated test source containing representative legacy and four-part releases. Creating
   test releases requires separate authorization. A generation-1 name can sort below a
   historical milestone-first tag. Resolve selection before household publication; never
   raise Generation just to disguise ordering. Tag/name equality and a higher versionCode
   alone do not prove discovery compatibility.
4. Follow the [update runbook](wiki/foodyou-update-procedure.md) for signing and a
   data-preserving signed-candidate update on a test device or emulator. Do not uninstall
   to change version schemes. After authorized publication, verify actual Obtainium
   discovery and update on the first household phone before rolling out to the second;
   retain the same-signature sideload fallback. Preflight is not evidence of that rollout.

The GitHub release notes are the publication ledger: record the canonical version,
exact tag, Android versionCode, and source revision for each new release. Existing
release records remain immutable; do not invent missing historical mappings. Publication,
tag creation, and distribution still require their own authorization.

## Reopened and supplementary Stories

A genuinely reopened Story becomes nonterminal and stops contributing to its milestone's
closed count. Recompute the draft version and reset ReleaseCount when that changes the
current coordinate. Prefer a distinct follow-up Story if the original acceptance remains
complete. Count distinct closed Stories, not closure events.

Before publishing a revisited coordinate, inspect the release ledger and choose the next
unused ReleaseCount above all releases already published there. This is a continuation
of its release sequence, not reuse of an old identity. If the resulting name would sort
below the installed or selected release, resolve and verify the distribution channel's
ordering before publishing; Android versionCode must still increase. Never rewrite a
published release or silently increment Generation to hide a tracker change.

The owner adopted this supplementary mapping for [Milestone B: Bugs](milestones/milestone-b.md):

`StoriesClosed = count(distinct closed Stories in current numbered milestone ∪ Milestone B)`

The numbered milestone document owns its Stories' statuses; Milestone B owns its
`FOODUS-xxx` statuses. B's entire closed total contributes to the current version,
including when B itself is not In Progress. This contribution carries forward when
the numbered milestone advances; only the numbered milestone's contribution changes.
For example, 5 closed numbered-milestone Stories plus 2 closed B Stories gives `7`;
advancing to a new numbered milestone with none closed gives `2`, retaining those B closures.

A B closure or reopening changes this combined total and resets ReleaseCount under
the existing draft/legacy-transition and collision rules. Reopening a B Story removes
it from the total; closing it again counts that Story once, not another closure event.
If one Story is referenced in both trackers or transferred between them, preserve
its stable identity, record the alias/transfer and single status owner, and count it
once. Different Stories with similar names or numeric suffixes remain distinct.

The letter B is never a numeric version component. Creating or planning B advances
neither the active numbered milestone nor the product generation. Before counting
any other supplementary or lettered milestone, explicitly define its tracker,
receiving-milestone, identity, deduplication, transfer, and carry-forward rules.
Backlog entries and the separately versioned foodus-relay product do not contribute
without such an explicitly adopted mapping.
