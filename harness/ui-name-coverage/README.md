---
name: ui-name-coverage
description: FoodUs source-call inventory for developer UI naming coverage.
metadata:
  version: "1.0"
  owner: "Jarryd Adaens"
  repo: "FoodUs (fork of maksimowiczm/FoodYou)"
---
# UI name coverage

Project-specific verifier of explicitly named application-owned Compose constructor call sites.
It masks comments and literals, balances argument lists, resolves configured framework imports
and discovered local composables, and checks declared modifier names and modal source scopes.

## Trigger

Run when adding or changing application UI call sites or the inspection naming API. Review new
framework APIs against the config catalogue and repeat runtime selection checks for changed surfaces.

## Contract

```powershell
.\harness\ui-name-coverage\run.ps1
```

| Exit code | Meaning |
| --- | --- |
| 0 | Every surveyed call site is named |
| 1 | One or more surveyed call sites lack a name/surface scope |
| 2 | Invalid config, source parse failure, or failed self-test |
| 3 | Python unavailable |

Output includes a compact PASS/FAIL line and JSON at `runs/<timestamp>/report.json`.
This is a lexical source inventory, not Kotlin compiler analysis or proof of runtime coverage.
Unknown external constructors, dynamic drawing subregions, expression-body ownership, clipping,
gesture behavior, repeated instances, and system-owned windows require review/runtime evidence.
Inspector chrome is deliberately excluded so selecting it cannot prevent Copy or field selection.

## Setup

Python 3 and PowerShell are required. No external Python packages. Source roots, explicit exclusions,
and framework constructor/surface names are configured in `config.json`.

## Self-test

```powershell
.\harness\ui-name-coverage\run.ps1 -SelfTest
```

The synthetic fixture includes nested named calls and a deliberately unnamed Text, plus misleading
constructor text in comments and strings. The test requires the omission to fail the naming check.

## Seam

`config.json` owns the surveyed roots/APIs/exclusions. `coverage.py` owns lexical parsing and reporting;
`run.ps1` is the single PowerShell entry point. Runtime evidence belongs in Story 29's plan.
