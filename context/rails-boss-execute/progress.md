# Rails Boss Execute Progress — Milestone 2

## Run: 2026-07-27 — addendum Stories 21-23. Parallelism: 0 (serial). Workers: Opus. See `briefing.md`.

Execution order 22 → 21 → 23 (Story 21 hard-depends on Story 22; see briefing).

| Status | Story | Source | Persona | Commit | Notes |
| --- | --- | --- | --- | --- | --- |
| DONE | STORY 2.22 | `context/implementation-plans/milestone-2/story-22-ai-settings-screen/plan.md` | none | 02ec0391 | AiSettings DataStore + runtime-config precedence (user→BuildConfig→domain default); scanners rewired; Validate one-token probe w/ verbatim errors; persistence + failure paths + no-key-in-logcat emulator-verified; green-tick path needs real key; aiConfigured gates left for 2.21 (recorded in plan log) |
| DONE | STORY 2.21 | `context/implementation-plans/milestone-2/story-21-three-layer-ai-prompts/plan.md` | none | 4ec602d4 | prompts → pure machinery (AiScanPrompt domain object, grep-proven zero personal tokens); layer-2 system msg on both calls; hint+Submit (in-memory, scan-only); inherited aiConfigured gates fixed via ObserveAiConfigured; emulator-verified incl. reactive gate + 401 path; real steering/success round-trips need real key |
| DONE | STORY 2.23 | `context/implementation-plans/milestone-2/story-23-provider-website-links/plan.md` | none | ebb28703 | shared WebsiteChip + 4 static URIs in AppConfig/FoodYouConfig (AFCD reuses SOURCE_URL); all four outbound VIEW intents emulator-verified; one extra file over plan (LocalAppConfig anonymous default) forced by the interface change, recorded in plan log |

## Run: 2026-07-25 — Stories 1-19. Parallelism: 0 (serial). Workers: Opus.

(Previous Milestone 1 run table is preserved in this file's git history.)

| Status | Story | Source | Persona | Commit | Notes |
| --- | --- | --- | --- | --- | --- |
| DONE | STORY 2.1 | `context/milestones/milestone-2.md#story-1` | none | dc683e1d | appId→com.acme.foodapp (namespace untouched); fork v1.0.0 layered over upstream 3.4.9; About attribution; emulator-verified |
| DONE | STORY 2.2 | `context/milestones/milestone-2.md#story-2` | none | 0d853d16 | Sponsor button moved Settings→About (component relocated to about pkg, orphaned wavy divider removed); emulator-verified incl. sponsor flow |
| DONE | STORY 2.3 | `context/milestones/milestone-2.md#story-3` | none | 5feda67a | privacyPolicyUri → fork PRIVACY.md placeholder (owner must supply real URL); emulator-verified new intent target |
| DONE | STORY 2.4 | `context/milestones/milestone-2.md#story-4` | none | 0ace5923 | email/source/issues links → owner+fork; new upstream-attribution button (maksimowiczm GitHub); What's-new untouched; emulator-verified via intent capture |
| DONE | STORY 2.5 | `context/milestones/milestone-2.md#story-5` | none | 332e4cae | 4-button family (Search/QuickAdd/SmartToy/EditNote) on own row; stub routes FoodDiaryAiScan+FoodDiaryFastText(epochDay,mealId) for Stories 6/8; emulator-verified |
| DONE | STORY 2.6 | `context/milestones/milestone-2.md#story-6` | none | ef7421d8 | reusable `ai` slice + OpenRouter Ktor scanner; key via local.properties→BuildConfig (never committed); camera/preview/discard/no-key emulator-verified; 401-path proved seam; 3/3 parser tests; result-card Tick/Align not E2E (no real key) |
| DONE | STORY 2.7 | `context/milestones/milestone-2.md#story-7` | none | d4e2e38f | PickVisualMedia gallery button in androidMain actual; same downscale→preview→AskAI flow; emulator-verified with pushed JPEG |
| DONE | STORY 2.8 | `context/milestones/milestone-2.md#story-8` | none | 9fc241fb | ManualDiaryEntry + isPlaceholder/description cols, Room 32→33 migration (upgrade-verified w/ surviving data); form + 0-kcal entry emulator-verified |
| DONE | STORY 2.9 | `context/milestones/milestone-2.md#story-9` | none | 8b7d70e7 | Resolve-placeholder meta screen (branch on isPlaceholder); search/quick-add/AI routes; AI query-gen reuses ai slice; initialQuery on search route; explicit Remove placeholder; 3/3 tests; emulator-verified |
| DONE | STORY 2.10 | `context/milestones/milestone-2.md#story-10` | none | ab39e193 | Graphs setting (Personalization dropdown, DataStore ordinal) + Canvas macro pie; composition-local provider; 3/3 angle tests; restart-persistence emulator-verified |
| DONE | STORY 2.11 | `context/milestones/milestone-2.md#story-11` | none | 787ff547 | Week layout setting; FixedWeekStrip pager (Mon-first, whole-week swipes) additive beside untouched scroller; date-math tests pass; emulator-verified incl. restart |
| DONE | STORY 2.12 | `context/milestones/milestone-2.md#story-12` | none | a831dbb7 | 96 upstream issues triaged; 5 bugs adopted into backlog-1.md (#437 #420 #364 #325 #312); triage table appended; no app code |
| DONE | STORY 2.13 | `context/milestones/milestone-2.md#story-13` | none | 0e699bbb | MealTemplate+Item tables (Room 33→34); snapshot semantics — apply recreates manual entries; meal-card overflow menu UX; save/apply/delete loop emulator-verified; tests pass |
| DONE | STORY 2.14 | `context/implementation-plans/milestone-2/story-14-provider-quickadd-architecture-spike/plan.md` | none | 5ab699ed | wiki/provider-quickadd-architecture.md; all 15 §13 decisions resolved; diary snapshots confirmed immutable; FoodSwitch=licence-BLOCKED (gates 2.16); AFCD feasible (xlsx, CC BY-SA) |
| DONE | STORY 2.15 | `context/implementation-plans/milestone-2/story-15-australian-food-composition-database-provider/plan.md` | none | 1c615992 | AFCD provider live: dep-free xlsx reader, transactional replace, ProviderMetadata (Room 34→35), enable/disable filter; 16/16 tests; emulator E2E — 1,588 foods imported, vegemite found, disable/re-enable clean |
| BLOCKED | STORY 2.16 | `context/implementation-plans/milestone-2/story-16-foodswitch-provider/plan.md` | none | 5819da3e | external licence blocker recorded per story's designed gate (blocked path IS the story's fallback deliverable); unblock = owner data-licence agreement; see logs/story-16-foodswitch-blocked.md |
| DONE | STORY 2.17 | `context/implementation-plans/milestone-2/story-17-provider-update-check-refresh-ui/plan.md` | none | 40d1c7b4 | check-for-updates (HEAD Last-Modified cascade) + explicit download-and-replace; failure path proven (lastSuccessfulCheck unchanged, data kept); full refresh E2E (AFCD-only re-insert, diary preserved, FTS in sync); 5/5 tests |
| DONE | STORY 2.18 | `context/implementation-plans/milestone-2/story-18-quickadd-expansion-fields/plan.md` | none | e50987fa | description/fiber/servings/weight in Quick Add; Room 35→36 (plan's 32→33 renumbered — schema had moved); servings/weight metadata-only (no scaling, proven); 9/9 validation tests; migration byte-identical prior rows |
| DONE | STORY 2.19 | `context/implementation-plans/milestone-2/story-19-quickadd-promotion-workflow/plan.md` | none | 761fc966 | Promote to Product/Recipe via overflow menu; per-100g mapping; placeholder-ingredient recipe seeding w/ cancel cleanup; snapshot preservation proven; 4/4 tests; all E2E paths pass |
