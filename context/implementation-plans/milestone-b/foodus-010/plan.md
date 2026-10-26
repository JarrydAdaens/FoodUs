# Plan: FOODUS-010 — deliberate AI key visibility

Status: Implementation in progress; coordinator owns runtime acceptance and closure.
Authority: [FOODUS-010](../../../milestones/milestone-b.md#foodus-010), serial Milestone B delivery.

## Decision before implementation (2026-10-25)

The API key currently shares the generic state-based outlined editor. Replace only
that editor with Material 3 `OutlinedSecureTextField`, available in the already
resolved local Android Material 3 artifact. Preserve the same ViewModel-owned
`TextFieldState`; endpoint, model, system prompt, validation, saving, storage and
provider precedence stay unchanged. Reuse localized Show/Hide password actions.

Visibility belongs to screen-local `remember` state, never saveable state, the
ViewModel or preferences. Start with `TextObfuscationMode.Hidden`, with no automatic
last-character reveal. Only the explicit eye action uses `Visible`. Reset reveal on
lifecycle pause/stop so retained navigation and background return also start masked.
The native secure editor preserves password/accessibility semantics and normal
editing, selection and paste while hiding the displayed text.

## Checks and boundaries

Add Android regression coverage against the actual AiSettingsScreen/ViewModel using
a private Koin context, in-memory synthetic AiSettings and a fake validator. Cover
default masking/password semantics, reveal/hide, retained destination return, fresh
re-entry, Activity recreation, editing/selection, paste, validation and save. A private
Compose clipboard override must isolate paste from the owner's system clipboard.
Never read or write real AI settings, credentials, diary or photos; no live validator
or network, secret logging, UI tree dump or real-credential screenshots. DeveloperActivity
inherits the accepted general secureScreen boolean read. Restore test service flags
and keep the application Koin context intact. Use safe drawing insets and bounded waits.

Harness has no installed modules. Coordinator runs targeted/common checks, APK builds,
physical and compact Samsung checks and independent privacy review, then owns Story
status, milestone/Design closure, receipt and authorized commit. Worker does not build,
operate devices, commit, pull, push, release or alter other Stories.

## Evidence and next step

Read AGENTS, relevant laws/Design, milestone acceptance, run briefing, harness and
existing secure editor/test-host patterns. Local resolved Material 3 class metadata
confirms `OutlinedSecureTextField` and `textObfuscationMode` without new dependencies.
Production and regression sources frozen for coordinating acceptance. The only
production change is `AiSettingsScreen.kt`: native outlined secure key editor,
explicit localized eye action, screen-local non-saveable visibility and pause/stop
reset. ViewModel and generic fields are unchanged.

`AiSettingsKeyPrivacyTest` has three Android methods:

- `masksByDefaultRevealsDeliberatelyAndMasksRetainedAndFreshReentry`: native password
  semantics, deliberate eye actions, covering and returning to the same
  retained navigation entry/VM, fresh Back/re-entry and no persistence/probe effects.
- `recreationRestoresNavigationAndDraftButNeverRevealsTheKey`: mounts synthetic
  content automatically in `onActivityPostCreated` before the recreated Activity's
  first composition. A non-secret saveable sentinel and restored route demonstrate
  actual saved-state restoration; the same ViewModel preserves the edited draft.
- `backgroundReturnMasksAndEditingPrivateClipboardValidationAndSaveKeepRealDrafts`:
  real secure editing/selection and `ACTION_PASTE` using private Compose clipboard
  overrides, background return masking, fake validation using the actual draft and
  actual save to the isolated settings repository. Native clipboard access fails
  closed; private clipboard read-count proves the tested paste uses the override.

Local resolved native editor metadata confirms the default U+2022 bullet used by
the accessibility assertions. `git diff --check` passes. Worker has not run builds
or device checks; those runtime assertions remain pending coordinator execution.
Independent privacy review, physical/compact Samsung evidence, build acceptance,
tracker closure and completion commit remain with the coordinator. No source edits
will be made while the coordinating gates run without handing ownership back.

## Corrected visual evidence boundary (2026-10-25)

Coordinator's first full build/unit gate passed. All three initial phone methods
failed the fixture assumption that privileged `AccessibilityNodeInfo.text` contains
rendered bullets. Production is unchanged. Read-only disassembly of the exact local
resolved `foundation-android` and `ui-android` 1.11.0-alpha05 binaries establishes:

- `BasicSecureTextFieldKt` Hidden branch (bytecode 1684–1695) installs a codepoint
  transformation; its transform lambda returns the current obfuscation character.
- `TransformedTextFieldState.getVisualText()` uses codepoint-transformed text;
  `getOutputText()` instead returns output-transformed text or the original fallback.
- `TextFieldDecoratorModifierNode.applySemantics()` (0–52) sets EditableText from
  `getOutputText()`, independently setting InputText from the original text. Password
  semantics are set separately (75–79).
- Android UI `getInfoText()` preferentially returns EditableText, `setText()` assigns
  it directly to the native accessibility node, and the password flag is independent.

Extracted foundation classes SHA-256:
`27ee98a2279d4edff65c006c928fc550b64ce955da1bc121e58af5fda92a3e24`;
UI classes SHA-256:
`d4d67afd7a0a9d64c4e8ec84d581e659833e0c1ad1728bf3005a88cc4000c88b`.
This explains raw text visible to privileged test automation without proving any
visual display leak. It does not claim protection against a privileged accessibility
service. No new production semantics or dependency workaround is introduced.

Coordinator authorized a fixture-only correction. Every masked/revealed assertion
now preserves native password/action semantics and the real synthetic draft, and
requires PixelCopy of only the actual key-field region of the synthetic Activity
window. The source rectangle is checked wholly inside that window. PNGs go only to
the debug app's `externalFilesDir/foodus-010-synthetic/`, using unique test-case,
viewport, run and state-counter names. Five-second capture timeout and PixelCopy
success assertions prevent silent missing evidence; images contain synthetic keys
only. Coordinator must inspect every required masked/revealed state at both physical
and compact sizes. This is replacement evidence for the incorrect accessibility
assumption, not a masking waiver. Editing, selection, actual private-clipboard paste,
validation, save, recreation sentinel, retained VM and no-owner-data checks remain.

Fixture and plan refrozen; production remains at the original freeze. Coordinator
owns rebuild, both viewport runs/all-image inspection and renewed independent review.

## Coordinator final acceptance (2026-10-25)

The full offline unit/debug/test-APK command passed 145 unit tests in 31 suites,
zero failures/errors. The initial phone invocation failed the fixture's assumption
that AccessibilityNodeInfo.text contains rendered bullets. Exact installed Compose
bytecode provenance above, independently reproduced by the reviewer, disproved
that assumption. Production remained unchanged. The final fixture keeps native
password semantics, eye action and underlying-draft assertions, and requires
bounded PixelCopy capture of the actual synthetic-window key-field region for
rendered masking proof. The final test APK rebuild passed.

Samsung SM-A057F Android 15: all three tests passed at physical 1080x2400 in 19.795s
and compact 900x1600 in 50.955s. This covers default/reveal/hide, retained cover/pop,
fresh Back/re-entry, genuine Activity recreation with restored navigation/saveable
sentinel and retained draft VM, background return, actual private clipboard paste,
selection/editing, fake validation and real isolated preference save. No live
validation request, owner key/settings write or owner clipboard access occurred.
The inherited host secureScreen preference read is qualified above.

All 22 final field-region PNGs were opened and visually inspected by the coordinator:
14 Hidden states contain clear bullets and eight Visible states contain legible
expected dummy text. No blank, black, clipped or eye-only capture was accepted as
proof. Navigation/recreation/background and post-save captures exist at both sizes.
Generated manifest `app/build/foodus-010-image-manifest.json` aggregate:
`26d6ee2c539835d3c7361d073519c3752a2d24a37e4c70c8b503a4e6a2f51e5a`.
Final device readback: physical size 1080x2400, fixed rotation policy default,
user rotation free. No credential screenshots contain real secrets.

Independent reviewer `/root/review_foodus_010` returned static PASS WITH NOTES,
no findings. Its remaining notes were these required runtime/image gates and the
accepted inherited preference read; runtime/image gates now passed without waiver.
Two-source aggregate:
`4183936ba69efd75d4c110f1f1bd507efe6b6c6c6c113d946ffea1efb37a8178`.
Checked artifact identity:
`aa7bc77f9cb8fdea5d3e19824111c22fa69fe3824b095c37287d4fe7475010d8`.
Whitespace passes. Storage/provider precedence, VM and generic fields are unchanged.

This is the last B Story: milestone index/detail and Design index are closed in
this same completion bundle. Supplementary closures contribute under the version
policy; catalog 3.5.0/code 126 remains its explicit temporary legacy exception until
release preparation. No release, tag, publication, version, dependency or schema
change was authorized or performed. [Receipt](../../../_boss-runs/milestone-b-sequential/foodus-010-receipt.json)
prepared before terminal metadata; seal and authorized final commit follow.
