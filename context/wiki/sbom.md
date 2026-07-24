---
name: sbom
description: Software bill of materials for the Food You fork - modules, toolchain, direct dependencies, and external data services.
metadata:
  version: "1.0"
  owner: "Jarryd Adaens"
  repo: "FoodYou (fork of maksimowiczm/FoodYou)"
  app_version: "3.4.9"
  generated: "2026-07-24"
---

# Software Bill of Materials

[Back to wiki home](home.md) | [Design Specification](../design.md)

Source of truth for versions is `gradle/libs.versions.toml`. Regenerate this page from the catalog
when dependencies change; do not hand-edit versions here without updating the catalog first.

## Product

| Field | Value |
| --- | --- |
| Name | Food You (fork) |
| Version | 3.4.9 (versionCode 123) |
| License | GPL-3.0 |
| Application ID | `com.maksimowiczm.foodyou` |
| Platforms | Android (min SDK 28, target/compile SDK 36); iOS targets (`iosArm64`, `iosSimulatorArm64`) built but not shipped |
| Upstream | [maksimowiczm/FoodYou](https://github.com/maksimowiczm/FoodYou) by Mateusz Maksimowicz |

## First-Party Modules

| Module | Purpose |
| --- | --- |
| `:app` | Main KMP application (feature slices, DI, navigation, UI) |
| `:shared:barcodescanner` | Camera barcode scanning |
| `:shared:resources` | Shared localized string/image resources |

## Toolchain

| Tool | Version | Role |
| --- | --- | --- |
| Kotlin (Multiplatform) | 2.3.10 | Language / KMP compiler |
| Gradle | 8.13 (wrapper) | Build system |
| Android Gradle Plugin | 8.13.2 | Android build |
| Compose Multiplatform plugin | 1.10.1 | Cross-platform UI framework |
| KSP | 2.3.4 | Annotation processing (Room compiler) |
| Room Gradle plugin | 2.8.4 | DB schema management (`app/schemas`) |
| gmazzo buildconfig | 6.0.7 | Generates `BuildConfig` (version name) |
| kotlinx.serialization plugin | 2.3.10 | Serialization codegen |
| JDK | 21 | Java toolchain (JVM target 21) |

## Runtime Dependencies (direct, from the version catalog)

### UI — Compose Multiplatform

| Component | Version | Purpose |
| --- | --- | --- |
| org.jetbrains.compose.{runtime, foundation, ui, components-resources} | 1.10.1 | Core Compose UI |
| org.jetbrains.compose.material3:material3 | 1.11.0-alpha03 | Material 3 (Expressive) components |
| org.jetbrains.compose.material:material-icons-extended | 1.7.3 | Icon set |
| org.jetbrains.androidx.navigation:navigation-compose | 2.9.2 | Navigation |
| org.jetbrains.androidx.navigationevent:navigationevent-compose | 1.0.1 | Predictive back/navigation events |
| com.materialkolor:material-kolor | 4.1.1 | Dynamic Material You color schemes |
| com.github.skydoves:colorpicker-compose | 1.1.3 | Color picker UI |
| com.valentinilk.shimmer:compose-shimmer | 1.3.3 | Loading shimmer effect |
| sh.calvin.reorderable:reorderable | 3.0.0 | Drag-to-reorder (home screen cards) |
| org.jetbrains.compose.ui:ui-tooling | 1.10.1 | Debug-only Compose tooling |

### Data & Persistence

| Component | Version | Purpose |
| --- | --- | --- |
| androidx.room:{room-runtime, room-paging, room-compiler} | 2.8.4 | Local SQLite ORM (all diary/food data) |
| androidx.paging:{paging-common, paging-compose} | 3.4.1 | Paged lists (food search results) |
| androidx.datastore:datastore-preferences-core | 1.2.0 | Preferences storage |
| com.github.requery:sqlite-android | 3.49.0 | Modern SQLite build on Android |
| org.jetbrains.kotlinx:kotlinx-serialization-json | 1.10.0 | JSON serialization |
| org.jetbrains.kotlinx:kotlinx-datetime | 0.7.1 | Cross-platform date/time |

### Networking

| Component | Version | Purpose |
| --- | --- | --- |
| io.ktor:{client-core, client-content-negotiation, serialization-kotlinx-json} | 3.4.0 | HTTP client for remote food databases |
| io.ktor:ktor-client-okhttp | 3.4.0 | Android HTTP engine |
| io.ktor:ktor-client-darwin | 3.4.0 | iOS HTTP engine |

### Dependency Injection

| Component | Version | Purpose |
| --- | --- | --- |
| io.insert-koin:{koin-compose, koin-compose-viewmodel} | 4.1.1 | DI + ViewModel wiring (common) |
| io.insert-koin:koin-android | 4.1.1 | Android DI integration |

### Android Platform

| Component | Version | Purpose |
| --- | --- | --- |
| androidx.activity:activity-compose | 1.12.4 | Compose activity host |
| androidx.appcompat:appcompat | 1.7.1 | Compatibility base |
| com.google.accompanist:accompanist-permissions | 0.37.3 | Camera permission handling |
| com.journeyapps:zxing-android-embedded | 4.3.0 | Barcode scanning (ZXing) |

### Test-Only

| Component | Version | Purpose |
| --- | --- | --- |
| org.jetbrains.kotlin:kotlin-test | 2.3.10 | Common unit tests |
| androidx.room:room-testing | 2.8.4 | Room migration/DB tests |
| androidx.sqlite:sqlite-bundled | 2.6.2 | JVM SQLite driver for common tests |
| androidx.test:{core, core-ktx, runner}, androidx.test.ext:junit | 1.7.0 / 1.7.0 / 1.7.0 / 1.3.0 | Android instrumented tests |

## External Data Services (opt-in at runtime)

| Service | Access | Purpose |
| --- | --- | --- |
| Open Food Facts | HTTPS, anonymous | Community food product database |
| USDA FoodData Central | HTTPS, user-supplied API key | US food composition data |
| Swiss Food Composition Database | Bundled/imported data | Swiss food composition data |

No other network endpoints. No analytics, crash reporting, ad, or account SDKs are present.

## Notes

- Dependency repositories: Google Maven, Maven Central, JitPack (for `requery/sqlite-android` and
  `skydoves/colorpicker-compose`).
- Third-party library licenses have not been individually audited in this pass; the app itself is
  GPL-3.0. A license audit is a candidate backlog story if fork distribution becomes a goal.
- Transitive dependencies are not enumerated here; use `gradlew :app:dependencies` for the full
  resolved graph.
