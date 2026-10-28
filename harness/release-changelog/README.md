# Release changelog gate

Trigger: before creating or moving a release tag, or when updating What's New.
The tag-triggered Release APK workflow also runs this gate before compilation.

## Contract

Run `python harness/release-changelog/check.py` from the repository root.
Python 3, standard library only. No device, network or credentials are used.

- `0`: PASS; the newest entry matches the catalog version and has dated highlights.
- `1`: FAIL; missing/stale/duplicate entry, invalid date or missing highlights.
- `2`: invalid input or unavailable source files.

Outputs one compact verdict and `runs/<UTC timestamp>/report.json` (ignored).
The version comes only from the shared catalog. Project paths and the entry pattern
live in `config.json`, the module's tuning seam. The gate reads the static Kotlin
data format: `Version` calls with literal `version`, `LocalDate` and highlight lists.
It checks presence and structure, not factual release claims or writing quality.
Common/native app tests independently cover actual history and version matching.

## Self-test and seam

Run `python harness/release-changelog/check.py --self-test` for isolated missing,
stale, duplicate, empty and invalid-date fixtures plus a valid candidate. No app or
repository files are changed. `--root` selects a fixture checkout. If the static
data format changes, update config and self-tests together; change the parser only
when its contract changes. Writing and release
maintenance rules live in `context/wiki/changelog.md`.
