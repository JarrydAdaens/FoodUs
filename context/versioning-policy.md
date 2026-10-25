---
name: versioning-policy
description: FoodUs four-part product version policy, tracker ownership, and Android release transition.
metadata:
  version: "3.0"
  owner: "Jarryd Adaens"
  repo: "FoodUs (fork of maksimowiczm/FoodYou)"
---
# FoodUs Versioning Policy

FoodUs uses **Generation.Milestone.StoriesClosed.ReleaseCount**, adapted from the
Agentic Rails starter's product policy 1.1 by the owner's decisions below. This policy
supersedes the earlier
`<milestone>.<story>.<build>` convention prospectively. Published versions, tags, and
historical records keep their original identities. Document metadata versions and
the starter version are independent of the app version.

## Components and tracker ownership

| Component | FoodUs rule |
| --- | --- |
| Generation | `1` identifies the original author's Food You; `2` identifies this FoodUs fork; `3` identifies FoodUs's multiplayer generation, including the foodus-relay server. On 2026-10-25 the owner explicitly advanced current development to Generation `3`. This records the product direction, not completion of multiplayer or deployment of the server. Future generation changes require an explicit product decision. |
| Milestone | The owner-selected delivery milestone recorded alongside [Design's Milestones Index](design.md#milestones-index), currently Milestone 2. Starting work in a higher numbered milestone does not automatically advance this component. Generation and milestone are independent; adopting Generation 3 does not close Milestone 2 or select Milestone 3. Milestone B does not change this component. |
| StoriesClosed | Closed Stories in the selected delivery milestone **plus closed Stories in Milestone B**, counting each distinct Story once. Closed statuses remain Done, Complete, Transferred, or Abandoned. When the delivery milestone advances, use its closed count plus the cumulative B contribution. |
| ReleaseCount | Cumulative published FoodUs releases across the fork's lifetime, including its historical three-part releases and future generations. Count each distinct published release once, including published prereleases; exclude upstream Food You releases, draft releases, tags without a published release, local builds, downloads, and installs. Never reset when Generation, Milestone, or StoriesClosed changes. Development retains the count already published; the next publication uses that total plus `1`. |

The owner revised generation identity, delivery-milestone selection, and release counting
on 2026-10-25. These rules replace the earlier generation-1 assumption, highest-active-
milestone selection, and per-coordinate release counter. The three existing published
FoodUs releases contribute to the cumulative counter without renaming their tags or
reinterpreting the digits of their historical versions. They do not need to have used
Obtainium on every phone to count as publications.

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
milestone does not change the version unless it is the selected delivery milestone.

## Shared source and consumers

[`gradle/libs.versions.toml`](../gradle/libs.versions.toml) is the single shared version
source. After the transition below, `fork-version-name` holds the canonical four-part
value, using nonnegative decimal integers without leading zeros. Do not maintain a
separate running Story count or duplicate app version in documentation.

| Consumer | Mapping and constraints |
| --- | --- |
| Android APK | `app/build.gradle.kts` takes `versionName` directly from `fork-version-name`. Four components are kept verbatim; Android versionName is a string. |
| Product display | BuildConfig `FORK_VERSION_NAME` comes from the same catalog property; `FoodYouConfig.forkVersionName` supplies About and contact details. |
| Release tag | Tag is exactly `v<fork-version-name>`; `.github/workflows/release-apk.yml` checks equality. No zero removal or component truncation is permitted. |
| APK filenames | Replace dots in the catalog version with underscores for filenames only. Debug is `foodus-dev-<version>.apk`; unsigned release is `foodus-<version>-unsigned.apk`; signed release and GitHub asset are `foodus-<version>.apk`. Other app variants include their lowercased build-type name. AGP records the generated filename in its output metadata. Filenames do not change application IDs, embedded version names, or signing identity; release tags keep their dots. |
| Android installation order | `android-versionCode` is a separate installation sequence, not a packed four-part version. The build consumes it with `toInt()`: use a positive Kotlin Int (`1` through `2147483647`). Increase above every installed or distributed code for each distributed build; on a shipped upstream merge use at least `max(upstream, ours) + 1`. |
| Upstream attribution | `version-name` and BuildConfig `VERSION_NAME` retain the upstream Food You version. They do not track FoodUs delivery. |
| Other package formats | No four-part mapping is adopted for a separate iOS, SemVer, or other packaged release. Define bounds, ordering, normalization, and collision handling before shipping one; build metadata alone is not a unique package release identity. |

Android uses versionCode for downgrade protection and allows a descriptive versionName
string. See [Android's versioning contract](https://developer.android.com/studio/publish/versioning).
Debug and preview builds retain their configured suffixes; the household artifact is
the release build. The application ID `io.github.jarrydadaens.foodus` and release signing
certificate remain unchanged for in-place updates.

## Transition from existing releases

The **temporary legacy exception ended on 2026-10-25**, when the owner explicitly
requested the draft-version transition as part of Milestone B delivery. The catalog
now holds the tracker-derived four-part value and an advanced versionCode. Historical
three-part versions retain their original identities and publication status.

The owner's later decision on 2026-10-25 adopts Generation 3 while retaining Milestone 2
as the delivery milestone: its 22 closed Stories combine with Milestone B's ten.
Milestone 3's partially delivered Stories do not contribute until that milestone is
selected. Its relay contract and deployment gates remain open.

Maintain development versions when selected Stories close or reopen or the owner changes
generation or delivery milestone. These events and local builds do not authorize a
release or increment ReleaseCount. Before an authorized publication, reconcile the
lifetime release ledger and use the next unused cumulative count; increase versionCode
again if needed for its distribution sequence. An unpublished candidate does not become
a published release merely because its intended count is in the catalog. The draft
transition does not satisfy the publication and household-rollout gates below.

### Historical publication baseline

The complete GitHub Releases listing inspected on 2026-10-25 contains three published,
non-draft FoodUs releases:

| Release | Published (UTC) |
| --- | --- |
| [v2.19.0](https://github.com/JarrydAdaens/FoodUs/releases/tag/v2.19.0) | 2026-07-26 |
| [v2.19.1](https://github.com/JarrydAdaens/FoodUs/releases/tag/v2.19.1) | 2026-07-26 |
| [v2.23.0](https://github.com/JarrydAdaens/FoodUs/releases/tag/v2.23.0) | 2026-07-27 |

This is a dated baseline, not a separate live version source. The local legacy development
value `3.5.0` and the earlier four-part development value were not published releases.
Retain publication receipts in the ledger so deleting a remote record or aborting a
candidate never causes reuse of an already published count.

### First four-part publication

Before the first four-part publication:

1. Read the authoritative trackers afresh and derive the generation, selected delivery
   milestone, and combined closed count from that milestone and Milestone B. Include
   terminal closures only; do not use the highest Story ID.
2. Check the complete GitHub Releases/tag history and codes installed on both phones.
   Local Git tags are only a partial ledger. Do not reuse a published identifier or code.
3. Preflight Obtainium's selected-release and installed-version comparisons against an
   isolated test source containing representative legacy and four-part releases. Creating
   test releases requires separate authorization. The adopted Generation 3 places this
   transition above the published milestone-2 legacy tags. Check any installed legacy
   development version separately. A higher generation does not by itself prove
   Obtainium's handling of four components or its selected-release settings. Resolve
   selection before household publication; tag/name equality and a higher versionCode
   alone do not prove discovery compatibility.
4. Follow the [update runbook](wiki/foodyou-update-procedure.md) for signing and a
   data-preserving signed-candidate update on a test device or emulator. Do not uninstall
   to change version schemes. After authorized publication, verify actual Obtainium
   discovery and update on the first household phone before rolling out to the second;
   retain the same-signature sideload fallback. Preflight is not evidence of that rollout.

The GitHub release notes are the publication ledger: record the canonical version,
exact tag, cumulative ReleaseCount, Android versionCode, and source revision for each
new release. Existing
release records remain immutable; do not invent missing historical mappings. Publication,
tag creation, and distribution still require their own authorization.

## Reopened and supplementary Stories

A genuinely reopened Story becomes nonterminal and stops contributing to its milestone's
closed count. Recompute the development version and retain the cumulative ReleaseCount.
Prefer a distinct follow-up Story if the original acceptance remains
complete. Count distinct closed Stories, not closure events.

Before any publication, including a revisited coordinate, inspect the lifetime release
ledger and choose the next unused cumulative ReleaseCount. If the resulting name would sort
below the installed or selected release, resolve and verify the distribution channel's
ordering before publishing; Android versionCode must still increase. Never rewrite a
published release or silently increment Generation to hide a tracker change. A higher
final component does not compensate for a decrease in an earlier component.

The owner adopted this supplementary mapping for [Milestone B: Bugs](milestones/milestone-b.md):

`StoriesClosed = count(distinct closed Stories in selected delivery milestone ∪ Milestone B)`

The numbered milestone document owns its Stories' statuses; Milestone B owns its
`FOODUS-xxx` statuses. B's entire closed total contributes to the current version,
including when B itself is not In Progress. This contribution carries forward when
the numbered milestone advances; only the numbered milestone's contribution changes.
For example, 5 closed numbered-milestone Stories plus 2 closed B Stories gives `7`;
advancing to a new numbered milestone with none closed gives `2`, retaining those B closures.

A B closure or reopening changes this combined total and retains ReleaseCount under
the existing development/publication and collision rules. Reopening a B Story removes
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
