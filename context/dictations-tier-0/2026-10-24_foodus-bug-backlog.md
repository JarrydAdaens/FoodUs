# FoodUs — Bug and UX Issue Backlog

**Project:** FoodUs (forked from FoodYou)  
**Source:** Issues dictated during the FoodUs app review, 20–24 October 2026  
**Scope:** 10 stories (`FOODUS-001` through `FOODUS-010`)  
**Status:** Ready for investigation and implementation planning; not verified against source code.

This document captures the reported behaviour and requested changes. Menu names, navigation routes, data models and implementation details should be confirmed in the codebase. Acceptance criteria describe the intended result rather than prescribing a technical implementation. Some items are UX enhancements rather than functional defects.

## Index

| ID | Story | Area | Type |
|---|---|---|---|
| FOODUS-001 | Australian Food Composition Database missing from onboarding | Onboarding | Missing functionality |
| FOODUS-002 | External database entry is buried in Settings | Settings navigation | UX |
| FOODUS-003 | Keyboard obscures AI Scanner hint input | AI Scanner | Bug |
| FOODUS-004 | AI Scanner collapses multiple foods into one result | AI Scanner | Enhancement |
| FOODUS-005 | Scan results are corrupted or dropped in Quick Add | AI Scanner → Quick Add | Data integrity bug |
| FOODUS-006 | Quick Add conversion actions are hidden in overflow menu | Quick Add | UX |
| FOODUS-007 | Saving from scan-derived Quick Add returns to scanner | AI Scanner → Quick Add | Navigation bug |
| FOODUS-008 | Tapping photo to retake does not launch camera | AI Scanner | Workflow bug |
| FOODUS-009 | What's New displays upstream FoodYou changelog | About / What's New | Content bug |
| FOODUS-010 | AI API key is visible by default | AI Settings | Security / privacy UX |

---

## FOODUS-001 — Include Australian Food Composition Database in initial setup

**User story:** As a first-time FoodUs user, I want to choose the Australian Food Composition Database during onboarding so I can set it up alongside other food data sources.

**Reported issue:** The first-run setup screen lists food data sources that users can add, but the Australian Food Composition Database is absent.

**Reproduction steps:**
1. Launch FoodUs in a fresh-install or first-run state.
2. Navigate to the onboarding screen for adding data sources.
3. Review the offered sources.

**Actual behaviour:** The Australian Food Composition Database is not offered.

**Expected behaviour:** The Australian data source appears as a selectable option in first-time setup and uses an appropriate configuration/installation flow.

**Acceptance criteria:**
- The Australian Food Composition Database is listed on the first-run data source selection screen.
- Users can select it and proceed through the appropriate setup process.
- Its availability and setup behaviour are consistent with the same data source when managed after onboarding.
- Existing onboarding options remain functional.

**Investigation notes:** Confirm the actual source name and whether the integration already exists elsewhere in the app. Reuse its existing setup path rather than creating duplicate configuration logic.

---

## FOODUS-002 — Move external database navigation to main Settings

**User story:** As a user, I want direct access to external database management from Settings without drilling down into an intermediate Database page.

**Reported issue:** The button leading to the external database screens is under **Settings → Database**, one level too deep.

**Reproduction steps:**
1. Open Settings.
2. Navigate into the Database page.
3. Find and use the button that opens the external database screens.

**Actual behaviour:** External database management is only reached through the nested Database page.

**Expected behaviour:** An entry for the external database screens is accessible directly from the main Settings page.

**Acceptance criteria:**
- Add the external database navigation entry to the main Settings page.
- The new entry opens the existing external database screens.
- Remove the obsolete nested entry from the Database page to avoid duplicate navigation.
- Preserve the underlying external database functionality and existing configuration.

**Investigation notes:** Confirm the labels of the current screens and navigation target in the codebase. This is a relocation of an existing action, not a redesign of database management.

---

## FOODUS-003 — Keep AI Scanner hint field visible while keyboard is open

**User story:** As a user entering a scanner hint, I want to see the input field and the text I'm typing while the on-screen keyboard is displayed.

**Reported issue:** On the AI Scanner screen, focusing **“Hint for the scanner” (optional)** opens the keyboard, which overlaps the text field. Entered text appears behind the keyboard.

**Reproduction steps:**
1. Open AI Scanner.
2. Tap the optional scanner hint input.
3. Type while the on-screen keyboard is open.

**Actual behaviour:** The keyboard covers the active field and the text being entered.

**Expected behaviour:** The active input remains visible above the keyboard through appropriate viewport resizing, scrolling or insets handling.

**Acceptance criteria:**
- Focusing the hint field brings it into the visible area above the keyboard.
- The field and entered text remain visible while typing.
- Users can scroll to and interact with the field while the keyboard is open.
- Dismissing the keyboard restores the screen layout without obscuring content or introducing awkward blank space.
- Behaviour is verified on different screen sizes and with varying amounts of text.

**Investigation notes:** Inspect keyboard insets, window resizing, scroll containers and focus handling. The user did not prescribe a specific layout strategy.

---

## FOODUS-004 — Support multiple foods in AI photo scan results

**User story:** As a user scanning a meal, I want the AI to represent multiple distinct foods separately when appropriate, so I can inspect and choose what to add rather than accepting one blended entry.

**Reported issue:** The AI photo scanner always returns one structured food object, even when the image contains multiple distinct foods (the example given included breads, pasta and chicken). This limits the user's ability to check and trust each item.

**Reproduction steps:**
1. Open the photo-based AI Scanner.
2. Supply a photo of a plate or collection containing multiple distinct foods.
3. Optionally supply a scanner hint.
4. Run the scan and inspect the structured result.

**Actual behaviour:** Multiple foods are consolidated into a single result.

**Expected behaviour:** The scanner can return **one food entry** for a single coherent item (such as a pie) or **multiple distinct entries** for a meal containing separable foods (such as a mixed plate). The user can review and choose which detected entries to retain before adding them.

**Acceptance criteria:**
- The scanner's result model supports a collection of food entries, including the single-entry case.
- Multiple clearly distinct foods can be represented as separate results rather than automatically merged.
- A single coherent food can still be treated as one entry; do not require unnecessary ingredient-level splitting.
- Users can inspect individual detected foods and select/deselect which ones to accept.
- The subsequent food-entry flow supports the accepted selection without silently merging or dropping items.
- Scanning an image that yields no usable entries is handled gracefully.

**Open design decisions:** Determine how much control the user has over splitting/merging food entries, how nutrition estimates are attached to each item, and how multiple accepted items transition into the existing entry-creation flow. The request was for appropriate grouping plus user choice, not for mandatory decomposition of every recipe into ingredients.

---

## FOODUS-005 — Preserve all AI scan data when opening Quick Add

**User story:** As a user reviewing a scan result, I want the Quick Add screen to show the same food name, description, serving/weight and nutrition values I accepted on the previous screen.

**Reported issue:** After accepting an AI scan and advancing to the Quick Add-style screen, some fields are blank and other values change.

**Observed example (from the review):**

| Field | AI scan result screen | Quick Add screen |
|---|---|---|
| Name | Present | Present |
| Description | Not specified in dictation | Empty |
| Serving/weight | Not specified in dictation | “1 serving”; weight empty |
| Energy | **780 kcal** | **482 kcal** |
| Protein | **35 g** | **35 g** |
| Fat | **38 g** | **38 g** |
| Carbohydrates | Not specified in dictation | Empty |
| Dietary fibre | **6 g** | Empty |
| Sugar | **12 g** | Not confirmed in dictation |

The sample demonstrates a material energy mismatch and loss of dietary fibre. The user also reported missing description, weight and carbohydrates; values for those fields on the previous screen were not fully dictated. Do not assume the AI supplied values that were not observed.

**Reproduction steps:**
1. Scan food with the photo-based AI Scanner.
2. Review the AI-generated food and nutrition details.
3. Accept the result to open the Quick Add-style editor.
4. Compare every field and value between the scan result and Quick Add.

**Actual behaviour:** Quick Add displays missing fields and different nutritional values (notably 780 kcal becoming 482 kcal in the observed case).

**Expected behaviour:** The data accepted by the user is passed into Quick Add intact, using consistent units and serving quantities. Fields that are genuinely unknown should not be fabricated; values that are available must not be silently discarded or recalculated incorrectly.

**Acceptance criteria:**
- The accepted food name is carried across unchanged unless the user edits it.
- Description, serving size and weight are populated whenever those values are available in the scan result.
- Energy, protein, fat, carbohydrates, fibre, sugar and other supported nutrition fields are transferred when present.
- Displayed values are consistent between scan preview and Quick Add for the same portion and units, subject only to explicit, accurate unit conversions or rounding.
- Missing values remain clearly unset rather than being replaced by misleading defaults.
- The saved entry reflects the reviewed values.
- Automated coverage includes the observed 780 kcal / 35 g protein / 38 g fat / 6 g fibre / 12 g sugar scenario, and catches blank fields or unexpected calorie changes.

**Investigation notes:** Audit mapping from AI response → structured scan preview → Quick Add model → persistence, including optional nutrition fields, serving multipliers, mass/portion conversions and calorie calculations. The root cause is not yet known.

---

## FOODUS-006 — Expose Quick Add conversion actions as buttons

**User story:** As a user in Quick Add, I want to see **Convert to Product** and **Convert to Recipe** directly rather than discovering them inside an overflow menu.

**Reported issue:** Quick Add has a top-right three-dot (ellipsis) menu containing **Convert to Product** and **Convert to Recipe**, next to a visible Save button. The hidden actions are confusing. The issue occurs both when Quick Add is opened directly and when the AI scanning workflow opens the same page.

**Reproduction steps:**
1. Open Quick Add directly (for example via the lightning/quick-add entry point), or reach it after accepting an AI scan.
2. Look at the top-right controls.
3. Open the three-dot overflow menu.

**Actual behaviour:** The two conversion actions are hidden in the menu while Save is directly visible.

**Expected behaviour:** Conversion actions are discoverable as visible buttons, alongside or in a similarly prominent action area as Save.

**Acceptance criteria:**
- **Convert to Product** and **Convert to Recipe** are accessible as visible, clearly labelled buttons on Quick Add.
- Neither action requires opening an ellipsis menu.
- Save remains available.
- Both conversion actions preserve their existing functional outcomes.
- The change applies consistently to all entry paths into the shared Quick Add page.
- Buttons remain accessible on smaller displays without clipping or overlapping content.
- Remove redundant overflow-menu entries if no longer needed.

**Investigation notes:** Choose placement based on responsive layout; “buttons like Save” denotes direct visibility, not necessarily a requirement to fit all three into the toolbar.

---

## FOODUS-007 — Finish AI scan workflow after saving in Quick Add

**User story:** As a user who has completed an AI scan and saved its food entry, I want to return to the main dashboard to continue using FoodUs.

**Reported issue:** Saving from the Quick Add screen reached through AI Scanner navigates back to the AI Scanner page, even though the add operation is complete.

**Reproduction steps:**
1. Open AI Scanner and scan food.
2. Accept the scan result.
3. Review the Quick Add page.
4. Tap Save.

**Actual behaviour:** The app returns to AI Scanner.

**Expected behaviour:** On a successful save, the AI scanning/add flow completes and the app returns to the main dashboard. The user can start another food-entry operation from the dashboard if desired.

**Acceptance criteria:**
- A successful Save persists the intended entry.
- After a successful Save initiated from the AI scanning flow, navigation returns to the main dashboard.
- The completed AI scan/Quick Add flow is removed from back navigation so the Back action does not reopen it.
- Failed saves keep the user in an appropriate editing state with feedback; do not navigate away as if saving succeeded.
- Direct Quick Add entry paths are tested separately; do not unintentionally change their expected navigation without checking existing behaviour.

---

## FOODUS-008 — Reopen the camera when tapping a photo to retake

**User story:** As a user who wants a better scanner photo, I want tapping the preview to start retaking it immediately.

**Reported issue:** AI Scanner shows the instruction **“Tap the photo to retake it.”** Tapping the photo instead discards it and returns to the scanner state, requiring the user to manually start the camera again.

**Reproduction steps:**
1. Open AI Scanner.
2. Capture a photo and return to the preview.
3. Tap the photo to retake it.

**Actual behaviour:** The preview is cleared, but the camera capture flow is not reopened.

**Expected behaviour:** Tapping the preview directly reopens the existing camera/photo-capture flow, enabling immediate replacement of the photo.

**Acceptance criteria:**
- Tapping the current image invokes the camera/capture flow automatically.
- Capturing a new image updates the preview with the replacement.
- Cancelling the retake leaves the prior image in place.
- No extra manual camera-launch step is required.
- Camera permissions or camera errors are handled without losing the existing image unnecessarily.

**Investigation notes:** Reuse the app's existing image acquisition mechanism. Confirm whether camera and gallery photo acquisition share UI before changing that interaction.

---

## FOODUS-009 — Replace inherited FoodYou changelog with FoodUs history

**User story:** As a FoodUs user, I want **About → What's New** to describe FoodUs releases rather than showing the release history inherited from upstream FoodYou.

**Reported issue:** On the About page, the upward zigzag/trending-style button opens What's New, but the entries belong to the original FoodYou author. The user wants an initial FoodUs-specific history of **three entries**: the 1.0 fork announcement, the previous release and the current release.

**Reproduction steps:**
1. Open the About page.
2. Tap the What's New/changelog icon.
3. Review the release notes.

**Actual behaviour:** The upstream FoodYou changelog is displayed.

**Expected behaviour:** Show FoodUs-specific notes with three initial entries. Preserve upstream attribution elsewhere where required by licence or credits.

**Proposed initial content (draft, not confirmed release facts):**

### Version 1.0 — Introducing FoodUs

FoodUs is a fork of the open-source FoodYou application, created to build on the original project and expand its functionality. This release marks the beginning of FoodUs and acknowledges the original FoodYou project and its contributors.

### Previous release — Version TBD

Continued development of FoodUs, with refinements to the application's features and usability. *Replace this generic placeholder with the actual version number, date and shipped changes.*

### Current release — Version TBD

The latest FoodUs release, bringing further fixes and improvements. *Replace this generic placeholder with the actual version number, date and shipped changes.*

**Acceptance criteria:**
- What's New displays FoodUs-specific entries, not the inherited upstream changelog.
- The initial history contains three releases: fork/1.0, previous release and current release.
- Entries display in reverse chronological order (newest first).
- Actual previous/current version identifiers and release notes are confirmed before publication; placeholder text is not shipped as factual release history.
- Future entries can be added through the existing release-notes mechanism without a UI redesign.
- Required copyright, licensing and attribution notices for upstream FoodYou remain intact.

**Open information:** Actual version numbers, dates and shipped changes for the previous and current releases were not provided during dictation.

---

## FOODUS-010 — Mask API key by default in AI Settings

**User story:** As a user configuring an AI provider API key, I want the secret hidden by default so it is not exposed when the settings screen is opened.

**Reported issue:** The AI Settings page has an API key input with an eye icon, but the key is displayed in plain text initially. The user wants the key obscured until they explicitly reveal it.

**Reproduction steps:**
1. Configure an API key in AI Settings.
2. Open or return to AI Settings.
3. Inspect the API key input before touching the visibility control.

**Actual behaviour:** The API key is visible by default.

**Expected behaviour:** The field is masked by default. The existing eye control allows deliberate reveal/hide, and the visibility state does not persist across screen re-entry.

**Acceptance criteria:**
- The API key field is masked when AI Settings first opens.
- The eye control reveals the key on demand.
- The same control can mask the key again.
- Navigating away and returning starts with the key masked, even if the user previously revealed it.
- Restarting or recreating the app/screen does not accidentally restore a visible secret.
- Masking does not interfere with editing, pasting or saving the key.

**Investigation notes:** This story covers on-screen masking. It does not assert that the current storage mechanism is insecure or request a storage-system redesign; storage security can be reviewed separately.

---

## Cross-story verification

Several issues share code paths and should be regression-tested together:

- **AI scan to Quick Add:** `FOODUS-004`, `FOODUS-005` and `FOODUS-007` affect the scanner-result model, its handoff to Quick Add and completion of the flow. Test both single-item and multi-item scans.
- **Shared Quick Add UI:** `FOODUS-006` must work when Quick Add is opened directly and when reached through AI Scanner. `FOODUS-007` should only change the navigation outcome for the completed scan-initiated workflow unless other behaviour is explicitly agreed.
- **AI Scanner interaction:** `FOODUS-003` (keyboard) and `FOODUS-008` (retake) should be checked across typical screen sizes and normal navigation paths.

## Details needing confirmation during implementation

1. Actual naming and configuration mechanism of the Australian food data source (`FOODUS-001`).
2. Whether the AI scanner already returns multiple entities internally or only one in its schema (`FOODUS-004`).
3. Which fields are present in the raw model and where the Quick Add value divergence is introduced (`FOODUS-005`).
4. Correct version numbers, dates and genuine change summaries for the last two FoodUs release entries (`FOODUS-009`).

**No additional issues were raised after `FOODUS-010`.** A repeated observation about the Quick Add ellipsis menu was folded into `FOODUS-006` rather than counted as a separate issue.
