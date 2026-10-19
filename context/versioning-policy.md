---
name: versioning-policy
description: FoodUs product version sources and release compatibility rules.
metadata:
  version: "1.0"
  owner: "Jarryd Adaens"
  repo: "FoodUs (fork of maksimowiczm/FoodYou)"
---
# FoodUs Versioning Policy

FoodUs retains the version scheme already used by its published Android app. The authoritative values are in [`gradle/libs.versions.toml`](../gradle/libs.versions.toml); this document defines how to maintain them. The starter's optional four-part `Generation.Milestone.StoriesClosed.ReleaseCount` scheme has **not** been adopted. Changing to it would require a separate compatibility decision for existing releases and Android updates.

## Version components and owners

| Catalog property | Meaning | Consumer |
| --- | --- | --- |
| `fork-version-name` | FoodUs's shipped `<milestone>.<story>.<build>` version. The first two numbers follow the numbered milestone and delivered Story; the last number permits another release at that Story state, starting at `0`. | `app/build.gradle.kts` sets Android `versionName`; the release tag must be `v<fork-version-name>`. |
| `android-versionCode` | Monotonically increasing Android install order. Increase for every distributed build, above any version already installed on the household phones. | `app/build.gradle.kts` sets Android `versionCode`. |
| `version-name` | Upstream Food You version from which the fork derives. It is attribution and compatibility metadata, not FoodUs's shipped version. | BuildConfig `VERSION_NAME` and upstream attribution. |

The current values must be read from the catalog, not copied from older plans or examples. Design's Milestones Index and the owning milestone's Story statuses determine which Story has been delivered. Partial or blocked work does not close a Story or require a version bump. The build component increments when another release ships without advancing the first two components; reset it to `0` when they advance. Do not reuse a published version or lower an installed version's `versionCode`.

## Build and release rules

1. Before a distributed Android build, choose the next `fork-version-name` from the delivered milestone and Story state, then increase `android-versionCode` above the highest installed or shipped code. On an upstream merge that will be shipped, use at least `max(upstream, ours) + 1` for `android-versionCode`.
2. Keep `fork-version-name` and the release tag equal after the `v` prefix. [The release workflow](../.github/workflows/release-apk.yml) rejects a tag that disagrees with the catalog. Do not create a tag or release merely because a Story status changes; version values change when preparing a build to distribute.
3. Keep the Android `applicationId` `io.github.jarrydadaens.foodus` and the release signing certificate unchanged for in-place updates. The `debug` and `preview` build types add their own identity/version suffixes and are not the household release artifact.
4. Follow the [FoodUs update procedure](wiki/foodyou-update-procedure.md) for signing, distribution, and data-preservation checks. Do not place keystore material or credentials in this repository.

Project-document `metadata.version` values and the Agentic Rails starter's version are separate from the FoodUs app version. Historical plans and release examples retain the values they recorded at the time; the catalog is the current source of truth.
