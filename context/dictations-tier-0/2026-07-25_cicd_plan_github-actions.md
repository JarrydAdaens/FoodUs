# 2026-07-25 - CI/CD Plan: GitHub Actions for the Fork

## Source

- Captured from: owner-authored CI/CD plan (`foodyou-cicd-plan.md`, delivered from the owner's local Downloads folder on 2026-07-25)
- Related project area: Milestone 1 — fork CI/CD pipeline (synthesized into Story 15 and its implementation plan)
- Note: the plan's own VERIFY items were checked against the repository during synthesis; corrections (JDK 21 not 17, no static-analysis tooling, no `jarryd/develop` branch yet, docs deletion owned by Story 14) live in the Story 15 implementation plan, not here

## Raw Notes

The full source document is preserved verbatim below.

---

# FoodYou Fork — GitHub Actions CI/CD Plan

**Repo:** `github.com/JarrydAdaens/FoodYou` (fork of `maksimowiczm/FoodYou`)
**Date:** 25 July 2026
**Audience:** coding agent implementing this. Read the whole file before touching anything.

---

## 0. Ground rules for the implementing agent

1. **Do not guess Gradle task names, module paths, or plugin availability.** Every item marked `VERIFY` must be confirmed against the actual repo (`./gradlew tasks --all`, `settings.gradle.kts`, version catalogs) before it goes in a workflow file.
2. If a `VERIFY` item turns out not to exist, **leave the step out and note it** rather than inventing a substitute.
3. Do not modify anything under `app/` source, `build.gradle.kts`, or the package namespace as part of this work. This task is CI/CD only.
4. Assumptions are listed in §7. If any is wrong, stop and flag it rather than working around it.

---

## 1. Branch model

Work happens in a `jarryd/` branch namespace so upstream can be rebased onto cleanly.

| Branch | Role |
|---|---|
| `main` | Tracks upstream `maksimowiczm/FoodYou`. Pristine. **No CI should run here.** |
| `jarryd/main` | The fork's real trunk. Releasable code. CI/CD triggers off this. |
| `jarryd/develop` | Day-to-day integration branch. Merges into / syncs with `jarryd/main`. CI runs here too, but not CD. |
| `jarryd/*` | Feature/topic branches. |

**CI** (build + test) runs on `jarryd/main` and `jarryd/develop`, and on pull requests targeting either.
**CD** (signed APK + GitHub Release) runs only off `jarryd/main`, manually or by tag.

---

## 2. Current state — inherited from upstream

Three workflows exist in `.github/workflows/`:

| File | Trigger | Purpose | Decision |
|---|---|---|---|
| `docs.yml` | push to `main` | Builds docs site with `zensical`, deploys to GitHub Pages | **DELETE** |
| `release-apk.yml` | `workflow_dispatch` only | Builds, zipaligns, signs release APK, uploads as 7-day artifact | **KEEP + REWORK** |
| `validate-meals.yml` | push/PR touching meal JSON | Runs `dev/test-meals-localization.bash` with `jq` | **KEEP, retarget branches** |

**Gap:** nothing upstream verifies the app compiles or that tests pass. There is no CI. That is the main thing this plan adds.

---

## 3. Target state

```
.github/workflows/
├── ci.yml              # NEW  — compile + test on jarryd/main, jarryd/develop
├── release-apk.yml     # REWORKED — signed APK + real GitHub Release
└── validate-meals.yml  # RETARGETED — branch filter added
```

Plus repo configuration (§4) and optional hardening (§8).

---

## 4. Prerequisites — must happen before workflows will run

These are GitHub UI / account tasks. The agent should list them for Jarryd to do, not attempt them.

### 4.1 Enable Actions on the fork
Forked repositories have Actions **disabled by default**. Go to the repo's **Actions** tab and press the confirmation button to enable workflows. Until this is done, nothing runs.

### 4.2 Set the default branch to `jarryd/main`
This is load-bearing, not cosmetic. Per GitHub docs, the `workflow_dispatch` event *only* triggers if the workflow file is on the **default branch** — the manual "Run workflow" button will not appear otherwise.
Source: GitHub Docs, "Manually running a workflow" — https://docs.github.com/en/actions/how-tos/manage-workflow-runs/manually-run-a-workflow

Since `main` is reserved for upstream tracking, set **`jarryd/main` as the repository default branch** (Settings → General → Default branch).

### 4.3 Create the signing secrets
Secrets are **not** inherited from upstream. Create these under Settings → Secrets and variables → Actions:

| Secret | Value |
|---|---|
| `KEYSTORE` | The `.jks` keystore file, base64-encoded (`base64 -w0 release.jks`) |
| `KEY_ALIAS` | The key alias inside the keystore |
| `KEYSTORE_PASSWORD` | The keystore password |

**Keystore generation is Jarryd's job, done locally with `keytool`. Do not generate a keystore in CI, and do not commit one.**

**Gotcha:** the existing workflow passes only `--ks-pass` to `apksigner`. That works only if the *key* password matches the *keystore* password. If they differ, a fourth secret (`KEY_PASSWORD`) and a `--key-pass "pass:${KEY_PASSWORD}"` argument are required. Confirm which case applies before implementing.

---

## 5. Tasks

### Task 1 — Delete the documentation site

- Delete `.github/workflows/docs.yml`
- Delete the top-level `docs/` directory (contains `zensical.toml` and the site sources)
- **Before deleting:** grep the repo for references to `docs/` from outside that directory (README links, Gradle config, other scripts, `justfile` if present). Report anything found rather than silently breaking it.
- Do **not** enable GitHub Pages.

Rationale: the fork is not hosting upstream's marketing/docs site. Left in place, this workflow fails on every push to the default branch and generates noise.

### Task 2 — Add `ci.yml`

New file, `.github/workflows/ci.yml`. This is the compile-and-test safety net that upstream lacks.

```yaml
name: CI

on:
  push:
    branches:
      - jarryd/main
      - jarryd/develop
  pull_request:
    branches:
      - jarryd/main
      - jarryd/develop
  workflow_dispatch:

# Cancel superseded runs on the same branch instead of queueing them.
concurrency:
  group: ci-${{ github.ref }}
  cancel-in-progress: true

permissions:
  contents: read

jobs:
  build:
    name: Build & test
    runs-on: ubuntu-latest
    timeout-minutes: 45

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'gradle'

      - name: Setup Android SDK
        uses: android-actions/setup-android@v3
        with:
          accept-android-sdk-licenses: true
          log-accepted-android-sdk-licenses: false

      - name: Compile
        run: ./gradlew --no-daemon assembleDebug          # VERIFY task name

      - name: Unit tests
        run: ./gradlew --no-daemon testDebugUnitTest      # VERIFY task name

      - name: Upload test reports
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: test-reports
          path: app/build/reports/
          retention-days: 7
          if-no-files-found: ignore
```

**VERIFY before committing:**
- `assembleDebug` — this is a Kotlin Multiplatform project (`app/src/commonMain/...`). Confirm the Android debug assemble task name and that it is the cheapest full compile check available.
- `testDebugUnitTest` — confirm unit tests actually exist. If the project has no test sources, **drop the step and say so**; do not add a placeholder.
- Whether a static-analysis task exists (`lint`, `detekt`, `ktlintCheck`). If one does, add it as a separate step **after** compile. If not, do not invent one.
- JDK 17 is carried over from `release-apk.yml`. Confirm it still matches the project's toolchain config.

**Deliberately not included:** instrumented/emulator tests. They are slow and brittle, and there is no evidence the project has any. Revisit later if needed.

### Task 3 — Rework `release-apk.yml`

Keep the existing build-and-sign logic; it works. Change the four things below.

**3a. Trigger — add tag-based releases**

```yaml
on:
  workflow_dispatch:
  push:
    tags:
      - 'v*'
```

Manual dispatch stays for ad-hoc builds. Pushing a `v*` tag becomes the real release path.

**3b. Permissions**

Creating a Release requires write access to repo contents:

```yaml
permissions:
  contents: write
```

**3c. Harden the signing step**

Current step is functional but sloppy for something handling a private key:

- Replace `echo ${KEYSTORE_BASE64} | base64 -d > keystore` with
  `printf '%s' "$KEYSTORE_BASE64" | base64 -d > "$RUNNER_TEMP/release.jks"`
  (`echo` appends a newline; unquoted expansion is fragile).
- Write the keystore to `$RUNNER_TEMP`, not the workspace, so it can never be picked up by a later artifact-upload glob.
- Add a cleanup step with `if: always()` that does `rm -f "$RUNNER_TEMP/release.jks"`.
- Quote all secret expansions.
- Add `KEY_PASSWORD` / `--key-pass` if §4.3's gotcha applies.

**3d. Publish a real GitHub Release**

Currently the APK is an artifact that vanishes after 7 days. Replace with an actual Release, using the pre-installed GitHub CLI (no third-party action needed):

```yaml
      - name: Create GitHub Release
        if: startsWith(github.ref, 'refs/tags/')
        env:
          GH_TOKEN: ${{ github.token }}
        run: |
          mv signed.apk "FoodYou-${GITHUB_REF_NAME}.apk"
          gh release create "$GITHUB_REF_NAME" \
            "FoodYou-${GITHUB_REF_NAME}.apk" \
            --generate-notes
```

Keep the existing `upload-artifact` step as well, so manual (non-tag) dispatch runs still produce a downloadable APK. Raise its `retention-days` to something more useful (e.g. 30).

**Decision needed from Jarryd:** the repo is public, so GitHub Releases and their APKs are publicly downloadable. If distribution is meant to stay private, releases are the wrong channel — flag this rather than assuming.

### Task 4 — Retarget `validate-meals.yml`

Leave the job body alone. Add a branch filter so it can't fire on the upstream-tracking `main`:

```yaml
on:
  push:
    branches:
      - jarryd/main
      - jarryd/develop
    paths:
      - 'app/src/commonMain/composeResources/files/meals/meals*'
      - 'dev/test-meals-localization.bash'
      - '.github/workflows/validate-meals.yml'
  pull_request:
    branches:
      - jarryd/main
      - jarryd/develop
    paths:
      - 'app/src/commonMain/composeResources/files/meals/meals*'
      - 'dev/test-meals-localization.bash'
      - '.github/workflows/validate-meals.yml'
```

This workflow is dormant unless meal data changes. It stays until the fork's own food data model diverges, at which point it should be re-evaluated or removed.

---

## 6. Gotchas specific to this setup

1. **Workflows run from the branch version of the file.** A workflow edit only takes effect on branches that have the edit. Land workflow changes on `jarryd/develop` and `jarryd/main` — editing them on `main` does nothing useful.
2. **`workflow_dispatch` needs the default branch.** See §4.2. If the default branch is not switched, the Run workflow button will not appear for `release-apk.yml`.
3. **Rebasing onto upstream will conflict on these files.** `docs.yml` will keep coming back on every upstream sync until upstream drops it. Expect to re-delete it, or keep the deletion in a dedicated commit that's easy to replay.
4. **Public repo = free GitHub-hosted runner minutes.** No quota concern as things stand. If the repo is ever made private, minutes become metered.
5. **No signing config in Gradle.** `assembleRelease` produces `app-release-unsigned.apk`; signing happens afterwards via `apksigner`. Keep it that way — do not move signing into `build.gradle.kts` as part of this task.

---

## 7. Assumptions — correct these if wrong

- `jarryd/main` and `jarryd/develop` are the literal branch names.
- `jarryd/develop` merges into `jarryd/main`; `jarryd/main` is the release branch.
- `main` is kept as an upstream mirror and should never trigger CI.
- The `app` module is the Android application module (inferred from the existing APK output path `app/build/outputs/apk/release/`).
- Distribution is sideloaded APK, not Google Play. No Play Store publishing step is included here.
- The package rename to ACME has not happened yet, or is out of scope for this task.

---

## 8. Optional — not in scope, decide later

Listed so they're on record, not to be implemented now:

- **Branch protection on `jarryd/main`** requiring CI to pass before merge. This is what makes the CI actually enforce anything rather than just report.
- **Dependabot** (`.github/dependabot.yml`) to keep action versions patched.
- **Pinning actions to commit SHAs** instead of major-version tags.
- **A release-please or version-bump workflow** to automate `versionCode`/`versionName`.
- **Play App Signing**, if Google Play distribution ever happens.

---

## 9. Acceptance criteria

- [ ] `docs.yml` and `docs/` are gone; nothing else references them.
- [ ] `ci.yml` exists and runs green on a push to `jarryd/develop`.
- [ ] `ci.yml` does not run on a push to `main`.
- [ ] Every `VERIFY` item has been checked against the real project, with findings reported.
- [ ] `release-apk.yml` runs green from a manual dispatch and produces an installable signed APK.
- [ ] Pushing a `v*` tag produces a GitHub Release with the APK attached.
- [ ] No keystore material is written to the workspace or committed.
- [ ] `validate-meals.yml` stays dormant unless meal files change.
