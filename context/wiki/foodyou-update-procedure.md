---
name: foodyou-update-procedure
description: Owner runbook for building, signing, and shipping FoodUs fork updates to both household phones without losing local data.
metadata:
  version: "1.0"
  owner: "Jarryd Adaens"
  repo: "FoodUs (fork of maksimowiczm/FoodYou)"
---

# FoodUs Update Procedure

[Back to Wiki Home](home.md) | [Story 11 plan](../implementation-plans/milestone-1/story-11-app-update-mechanism/plan.md)

This is the complete runbook for shipping an update of the fork to the owner's Galaxy S22 Ultra
and his wife's phone. It exists so any future session (or the owner alone) can produce and deliver
a data-safe update from documentation, not memory.

**The three rules that make an update preserve data** (violate any one and Android forces an
uninstall, which deletes the Room database and all preferences):

1. Same `applicationId` — `io.github.jarrydadaens.foodus`. Never ship the `preview` build type
   (it appends `.preview` and is a different app).
2. Same signing certificate — every shipped build must be signed with the fork's release
   keystore. A mismatch is refused with `INSTALL_FAILED_UPDATE_INCOMPATIBLE` (verified on the
   emulator, 2026-07-25).
3. `versionCode` never lower than the installed one — bump it for every distributed build.

---

## 1. One-Time: Create the Release Keystore (owner-executed)

The keystore is the fork's permanent signing identity. **Losing it permanently breaks in-place
updates for both phones — there is no recovery.** It must never be committed, logged, or copied
into context files (laws.md §2). `.gitignore` covers `*.jks` and `*.keystore`.

Generate it **outside the repository** (e.g. a secure folder that is not under
`D:\forked-projects\`):

```powershell
& "C:\Java\jdk-21.0.12+8\bin\keytool.exe" -genkeypair -v `
    -keystore <SECURE-PATH>\foodus.jks `
    -alias foodus `
    -keyalg RSA -keysize 4096 -validity 10000
```

keytool prompts for the keystore password and identity fields. Choose a strong password and
record it only in the custody location.

### Custody (owner decision — still open, plan Q2)

Candidate custody arrangements; the owner must pick before the key signs anything:

- Password manager holds the passwords **and** a copy of the keystore file (attachment).
- A second offline copy (e.g. encrypted USB or household backup drive) outside the dev machine.
- GitHub Actions repository secrets hold the CI copy only (see §6) — never the sole copy.

After generating, record the certificate SHA-256 digest here (it is public information and lets
any future candidate keystore be confirmed as the right one):

```powershell
& "C:\Users\Jarry\AppData\Local\Android\Sdk\build-tools\36.0.0\apksigner.bat" verify --print-certs <signed.apk>
```

> **Release certificate SHA-256:** _to be recorded by the owner after the first real signing._

## 2. Per Release: Bump versionCode

Edit `gradle/libs.versions.toml`:

```toml
android-versionCode = "123"   # bump to at least installed + 1 for every shipped build
```

- Simple scheme (plan Q4): before each shipped build, set it to `installed + 1`.
- On upstream merges: take `max(upstream, ours) + 1` if shipping. This single line is the one
  recurring upstream-file conflict this fork accepts; resolve it with that rule.

## 3. Per Release: Build and Sign

From the repo root (`D:\forked-projects\FoodYou`), with `JAVA_HOME` at the JDK 21 install:

```powershell
$env:JAVA_HOME = 'C:\Java\jdk-21.0.12+8'
.\gradlew.bat :app:assembleRelease
```

Output: `app\build\outputs\apk\release\app-release-unsigned.apk` (R8-minified `release` build
type — the same configuration upstream ships; plan Q3). The build takes several minutes.

Then sign with the helper script (wraps zipalign + apksigner, same pattern as
`.github/workflows/release-apk.yml`):

```powershell
$env:FOODUS_KEYSTORE = '<SECURE-PATH>\foodus.jks'
$env:FOODUS_KEY_ALIAS = 'foodus'
$env:FOODUS_KEYSTORE_PASSWORD = '<from password manager — never typed into files or history>'
.\jarryd\scripts\sign-apk.ps1
```

The script refuses to run if the keystore is inside the repository, and finishes with
`apksigner verify --print-certs` — check the SHA-256 digest matches the one recorded in §1.
Output: `app\build\outputs\apk\release\app-release-signed.apk`.

Manual equivalent (what the script does), using build-tools 36.0.0 at
`C:\Users\Jarry\AppData\Local\Android\Sdk\build-tools\36.0.0`:

```powershell
zipalign -f -p 4 app-release-unsigned.apk aligned.apk
apksigner sign --alignment-preserved --ks <SECURE-PATH>\foodus.jks --ks-key-alias foodus --out app-release-signed.apk aligned.apk
apksigner verify --print-certs app-release-signed.apk
```

(apksigner prompts for the password when `--ks-pass` is omitted — preferable to putting it on a
command line.)

## 4. Distribution: GitHub Releases + Obtainium (Option A; manual sideload fallback)

Decided channel (plan Q1): the Story 15 release workflow publishes the signed APK to the fork's
GitHub Releases on tag push; both phones run
[Obtainium](https://github.com/ImranR98/Obtainium), which detects and installs new releases.

One-time setup per phone:

1. Install Obtainium (from its GitHub releases or F-Droid).
2. Grant Obtainium the "install unknown apps" permission when prompted.
3. Add app → source URL `https://github.com/JarrydAdaens/FoodUs` → confirm it detects the
   latest release.

Per release afterwards: Obtainium notifies; tap update; the app updates in place.

**Fallback (Option B, always available):** download the signed APK from the GitHub Release in
the phone's browser (or copy it over), tap to install — or `adb install -r app-release-signed.apk`
from the dev machine. Never uninstall first.

**Milestone 2 forward constraint:** once builds carry the baked-in AI key, public GitHub
Releases can no longer be the channel — a private channel must be chosen before any key-bearing
build exists (plan Q6).

## 5. One-Time: Debug → Release Signature Migration (owner's phone)

The owner's phone currently runs a **debug-signed** build (Story 1). The first release-signed
install requires exactly one uninstall, because the certificates differ — Android refuses the
in-place install with `INSTALL_FAILED_UPDATE_INCOMPATIBLE` (observed and verified on the
emulator, 2026-07-25).

**Data timing warning:** the original "free" migration window (before any real data existed) has
passed — Story 10's import and Story 12's daily use are complete, so the phone may hold real
diary data. Before uninstalling:

1. Export whatever the app supports in-app (Settings → export/backup of the product database).
2. Know that the imported product catalog is regenerable: rerun the Story 10 exporter
   (`jarryd/scripts/export_foodyou_csv.py`) and re-import the CSV.
3. Accept that anything not exportable (diary entries logged since import) is lost with the
   uninstall — do the migration on a day when that loss is smallest.

Then: uninstall FoodUs → install the release-signed APK (§3/§4) → re-import data. This happens
**once**; every later update is in-place.

The wife's phone never migrates — its first install is release-signed from day one (Q5 open:
confirm her Android version is ≥ 9 / minSdk 28 and she is comfortable with the one-time
"install unknown apps" grant).

## 6. CI Signing (Story 15 hand-off)

The reworked `.github/workflows/release-apk.yml` signs with the same keystore via fork
repository secrets (owner adds them under Settings → Secrets and variables → Actions):

| Secret | Value |
| --- | --- |
| `KEYSTORE` | base64 of `foodus.jks` (`[Convert]::ToBase64String([IO.File]::ReadAllBytes('<SECURE-PATH>\foodus.jks'))`) |
| `KEY_ALIAS` | `foodus` |
| `KEYSTORE_PASSWORD` | the keystore password |

CI-built and locally-built APKs are then interchangeable on the phones — confirm with
`apksigner verify --print-certs` that both show the §1 digest.

## 7. Data-Preservation Verification (per plan step 7; once on real hardware)

Prove an update preserves data before trusting the mechanism with daily use:

1. In the installed app, log a marker diary entry and create a marker custom food.
2. Bump `android-versionCode`, build and sign (§2–§3).
3. Install **over** the existing app (Obtainium update or `adb install -r`) — no uninstall.
4. Open the app: the marker entry and food must still be there and the version must be the new
   one (`adb shell dumpsys package io.github.jarrydadaens.foodus | Select-String versionCode`, or
   the in-app About screen).

Emulator evidence (2026-07-25): the full cycle was validated with a throwaway test keystore —
release-signed install over debug-signed correctly refused; after uninstall + release install +
completed onboarding, a same-signature `adb install -r` updated in place (`firstInstallTime`
unchanged, `lastUpdateTime` advanced) and the app relaunched straight to the diary screen with
its onboarding state intact. Real-hardware verification with the real keystore remains
owner-executed.
