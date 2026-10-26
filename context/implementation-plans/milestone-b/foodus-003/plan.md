---
name: foodus-003-plan
description: Scanner hint keyboard visibility correction and device evidence.
---

# FOODUS-003: Scanner hint above the keyboard

Authoritative Story status and acceptance: [Milestone B](../../../milestones/milestone-b.md#foodus-003).
Coordinator owns status, device operation, builds, completion receipts, and commits.

## Scope and approach

Keep the production scanner's captured-photo hint usable with the Android keyboard,
including focus, short/multiline input, scrolling to Submit, and dismissal. Preserve
photo acquisition, review/mapping, and AI contracts. No dependencies or settings
storage changes are needed. Scanner UI, its keyboard regression, and this plan are
the worker's ownership; overlapping Stories execute sequentially.

The initial source inspection found a vertically scrolling Column with Scaffold
padding and 16 dp content padding, but no IME inset handling or explicit inset
consumption. Android preview is square at full width, so its height pushes the hint
down. MainActivity declares `adjustResize`; the existing DeveloperActivity test
host is edge-to-edge and must explicitly mirror that window mode. Scrolling alone
does not prove the hint is visible after focus. These initial source observations
are supplemented by the reproduced baseline below.

## Regression and privacy boundary

`AiScanKeyboardTest` hosts the production `AiScanScreen` with isolated
Koin and a generated green JPEG supplied through `onPhotoCaptured`. Its in-memory
AI settings contain a synthetic key; all fallback AI configuration fields are
blank. The synthetic scanner fails if called. The app's FoodSearchUseCase is only
an unused constructor dependency; no Align/search path executes. No camera,
gallery, real AI call, app data clearing, persistent settings, or diary operation
is required. Global Koin identity is checked after fixture cleanup.

Two tests cover short and five-line synthetic input through actual keyboard
events, including Enter between lines. They
require the actual Android IME, compare accessibility input bounds to the IME top,
reject clipping relative to the pre-keyboard field height, scroll to the enabled
Submit button with the keyboard still open, dismiss the keyboard, reach the photo
and Submit again, preserve text, and exercise Back. After dismissal they scroll
to the settled bottom and require the gap below Submit to be within 64 dp of
the scroll viewport, rejecting stale keyboard-sized empty space. Device runs
must cover normal Samsung dimensions and a compact viewport with the display
override restored.

## Evidence and continuation

- Source baseline: `a3782461`; production scanner untouched for the initial red run.
- Test source prepared; no builds or phone operations performed by this worker.
- First coordinator baseline compiled and ran on Samsung; both methods failed
  before keyboard focus because no visible editable node was found. Reported
  window was landscape `2400 x 1080` (physical display `1080 x 2400`, density 450).
  This is not keyboard-bug reproduction. The test now selects the target app's
  application window explicitly, recognizes editable/action/class semantics,
  scrolls only nodes exposing the requested action, and requires a minimum 56 dp
  pre-keyboard input height. Bounded diagnostic output contains class/geometry/
  numeric actions only, never UI text or settings. Android 30+ guard covers IME
  inset APIs. Production was unchanged at that pre-focus investigation stage.
- Required baseline: coordinator builds/runs `AiScanKeyboardTest` on Samsung and
  records exact failures/geometry. Failure due to fixture compilation, host mode,
  or keyboard availability is infrastructure evidence, not bug reproduction.
- Next: inspect baseline geometry, make the smallest production inset/focus fix,
  then rerun both cases on normal and compact Samsung viewports and relevant
  deterministic checks. Independent review follows the verified production diff.
- Harness has no installed matching module. Direct instrumented device evidence
  is required for the runtime behavior claim; Story remains incomplete until it
  is recorded.

## Reproduced baseline and correction

After two landscape attempts failed before focus (scrolling did not expose the
hint in Android accessibility), the coordinator changed approach and temporarily
locked portrait. Both portrait tests reached focus and reproduced the reported
keyboard overlap: hint bounds `Rect(45, 1401 - 1035, 1683)` versus IME top `1392`,
window `1080 x 2400`. The hint was invisible while the scroll viewport still
extended below the keyboard. Ignored raw evidence:
`app/build/foodus-003-baseline-portrait.txt`. Original free rotation was restored.

The correction consumes Scaffold padding before applying `imePadding` outside
the scrolling viewport. This shrinks the viewport to the usable area above the
keyboard while avoiding duplicate system-bar padding, allowing normal focus
bring-into-view and scrolling to work. Photo acquisition and result mapping are
untouched. Coordinator runs of the fix passed focus and typed-field/IME geometry
on normal portrait Samsung, then exposed two unsupported accessibility operations:
forced selection returned false and the provider did not advertise character
location extra data. Those operations are removed. They cannot establish whether
the final multiline line is visible on this provider.

Subsequent coordinator evidence found an accessibility-action scrolling limit:
the multiline case completed, while short input left Submit below the keyboard
after one successful scroll action. The test now uses injected touchscreen swipes
inside the scanner viewport, with target-app `TYPE_VIEW_SCROLLED` events and
`scrollY`/`maxScrollY` to observe progress. A missing event requires independent
end-content geometry; it is never accepted solely because an accessibility scroll
action returned false. Swipes and settling are bounded.

The first real-gesture run still could not scroll short input from the outer
right padding, while multiline completed. Gesture coordinates now use the
viewport center horizontally and its upper 65% vertically, normally starting
within the actual photo content, with a required movement above 100 physical
pixels. This avoids the system edge/padding hit region and the focused input at
the bottom. This is a fixture correction, pending a fresh device run; no further
production behavior change is inferred from an unrecognized gesture.

Visual inspection of the first normal-size screenshots confirmed `rice` visible,
but only three of five multiline lines visible after accessibility `ACTION_SET_TEXT`.
That is outstanding final-line visibility, not a passing Story. Input is now typed
through keyboard events with Enter between lines to exercise actual cursor motion
and autoscroll before deciding whether further production focus handling is needed.

A fresh screenshot after actual multiline keyboard input still showed only the
first three lines. IME padding alone therefore does not satisfy final-line
visibility. A measured trailing-edge relocation candidate subsequently made all
five lines visible, but independent review identified a consequential risk: always
following the tail can hide an earlier line when the user edits it. That candidate
also still failed the real-touch Submit check. It is not the accepted solution.

The current candidate removes the entire custom requester/focus/size/effect and
uses the repository's established state-based `OutlinedTextField` pattern from
AI Settings. `AiScanViewModel` owns an in-memory `TextFieldState`; Submit reads
and trims `hint.text` with the same optional-hint semantics. Standard field state
now owns selection/caret handling. Scaffold inset consumption and IME padding
remain around the scrolling viewport. Mapping, retake, scanner result state,
external contracts, and persistence remain unchanged. This candidate requires
fresh physical/compact device checks; its caret behavior is not assumed proven.

The first state-based device run reached focus but found the merged field semantics
partly below the IME: bottom `1460` versus keyboard top `1392`; the inner editable
text region `1324..1392` was visible. That conservative gate prompted additional
visual investigation rather than treating a partially visible semantic node as proof.
The candidate now requests the **whole field** with a standard
`BringIntoViewRequester` only on focus or IME inset changes. No hint-text, selection,
or size changes trigger this request; normal state-based caret handling remains
responsible for typing and earlier-line editing. No custom trailing rectangle is
used. Physical/compact focus, final-line, earlier-edit, and Submit checks remain
outstanding for this combined candidate.

Subsequent focus screenshots showed a complete purple input border above the
keyboard for short input; the merged EditText includes label/supporting prose and
extends beyond the actual outlined input container. The automated gate is now
explicitly tied to the first non-text View child representing the visual input
border, plus the narrower inner editable-text View. It requires the full input
border above the IME, at least its measured pre-focus height (minimum 56 dp), and
inner text geometry inside that border. This is a corrected measurement target,
not acceptance of hidden input. Synthetic screenshots still establish actual
active-line visibility. An immediate multiline capture showed a mid-render green
photo; captures now wait one second and idle for settled drawing, and timeout
also captures a heading-guarded synthetic diagnostic.

The current candidate bounds the state-based editor to three visible text lines,
using standard `TextFieldLineLimits.MultiLine`. Long hints scroll inside the
editor so actual caret movement, including first-line editing, remains under
standard TextFieldState handling instead of growing an unbounded outer field
under the keyboard. This is a fixed editor layout, not a new setting. The
focus/IME-only whole-field request waits for the next frame before reading the
updated inset layout. No text-to-tail effects or pixel offsets are introduced.
The conservative actual-input geometry, first-line edit, touch scrolling,
enabled Submit, dismissal gap, and visual proof remain required.

The multiline regression uses the state-based editor's advertised accessibility
`ACTION_SET_SELECTION` at position zero, requires the field still focused and
keyboard visible, then types `Edited ` through actual keyboard events into the first line,
checks the exact retained text, and saves a separate earlier-edit screenshot.
After explicit finger MotionEvents were accepted but still produced no scroll
events, the test changed driver rather than assuming injection was effective.
It now runs Android's native `input touchscreen -d <display> swipe` using measured
fixture coordinates and closes/consumes the returned descriptor. Observed scroll
events/end geometry and reachable Submit remain required; command acceptance
alone does not establish scrolling.

The latest run passed focus/typing geometry but fresh screenshots disagreed with
cached accessibility bounds (one-line node geometry after five typed lines).
Before each root read the test now clears UiAutomation's cache on Android 33+,
so current input/viewport geometry is requested from the provider. The short
touch driver and earlier-line key mapping still failed in that run, so neither
is reported as accepted. Production remains unchanged during this test-driver
repair. New device evidence must establish accurate current geometry, active
line visibility, real scrolling, and exact retained text.

After clearing the accessibility cache and switching to native swipes, the short
hint test completed successfully, including real scrolling, Submit access,
dismissal, measured trailing space, retention, and Back. Fresh multiline geometry
exposed real growth under the IME: the three-line border was `1133..1427` versus
IME top `1392`, while inner text `1178..1382` remained visible. The editor grew
from one to three lines after its focus-only relocation. Its minimum and maximum
height are now both three lines so the complete input has stable dimensions at
initial focus. Standard internal caret scrolling and focus/IME-only whole-field
relocation remain; no per-edit relocation or new controls are introduced. The
stable-height candidate still needs full normal/compact regression and visual
active-line proof before acceptance.

The stable-height normal run again passed short input completely, and multiline
typing plus full input-border geometry passed. Earlier-line navigation exposed a
test-driver issue: repeated Up events continued beyond the first line, moved
focus to Back, and dismissed the keyboard before prefix typing. The fresh editor
now advertises supported selection action `131072`; the test requires that action
to succeed and verifies selection, editor focus, and real IME visibility before
typing the prefix. Unsupported selection is an explicit failure; no model state
is edited directly. A compact attempt unexpectedly launched landscape despite
the display-size request. The test now checks a nonzero portrait window before
any input lookup or gesture; rotation/display setup failures are explicit device
prerequisite errors. The coordinator must reapply/verify portrait lock after size
changes and restore the original configuration.

Automated coverage retains real keyboard focus/input, minimum field geometry,
retained model text, enabled Submit reachability, dismissal, measured trailing
space, and Back. To establish final-line visibility, each case saves a screenshot
of only the isolated synthetic scanner after typing and passing geometry checks.
Files are stored under the target app's external-files `foodus-003-keyboard/`
subfolder as `short-<width>x<height>.png`, `multiline-<width>x<height>.png`, and
`multiline-earlier-edit-<width>x<height>.png`.
Capture/encoding failure fails the test. No additional dependencies or production
test seams are introduced. The coordinator must pull only this folder to ignored
`app/build/` and visually inspect all six physical/compact images, including the
active last multiline line while appending and active first-line edit, before
accepting the device claim. The bounded editor need not display all five lines
simultaneously; all text must remain retained and internally reachable.
Device/local images are
not committed, and no private app-data folder is copied. This combines automated
field layout checks with direct synthetic visual evidence where character-level
accessibility geometry is unsupported; no acceptance condition is waived.

Pending coordinator checks: build and both instrumented tests on Samsung portrait
at physical `1080 x 2400` and compact `900 x 1600` (density 450, 320 x 569 dp),
restore size/rotation, review the scoped diff, and record the verified result.
Landscape accessibility reachability remains a documented limitation requiring
follow-up evidence; initial landscape failures did not exercise the IME behavior.

## Final acceptance and delivery

2026-10-24: JDK 21 debug and instrumentation APK builds passed. Final Samsung
Galaxy A05s (SM-A057F), Android 15, density 450, instrumented results:

- Physical portrait 1080x2400: both tests passed, 36.262 seconds.
- Compact portrait 900x1600 (320x569 dp): both tests passed, 38.519 seconds.
- Both tests include real keyboard input, complete input-border geometry, retained
  text, real scrolling, enabled Submit reachability, keyboard dismissal, measured
  trailing gap and Back. Multiline additionally selects the first character via
  the supported editor action, verifies focus/IME/selection, and types a prefix.
- All six fresh short/multiline/earlier-edit screenshots were inspected. Active
  last-line and first-line glyphs/carets are above the real IME. Some UiAutomation
  captures have incomplete photo/header redraw; active text remains clear, and
  current input geometry plus the complete first-edit/compact frames corroborate
  the required hint visibility. These captures do not claim broader rendering QA.
- Independent final static review `/root/review_foodus_003`: PASS, no findings.
  Final reviewed source/test SHA-256 identities match the checked artifacts.
- `git diff --check` passed. Existing SDK XML/compiler warnings remain.

Compact setup needed temporary `fixed-to-user-rotation enabled` in addition to
portrait lock because ordinary rotation lock still allowed landscape. The prior
policy was read as `default`; final cleanup restored physical size 1080x2400,
fixed policy `default`, and free rotation. No stored settings, diary data, real
photos, AI service calls or credentials were used or changed by the fixtures.
Raw instrumentation and synthetic images remain ignored under `app/build/`.
Landscape is unverified; this Story's two different portrait sizes are verified.

Acceptance complete. No remaining implementation step. Shared catalog remains
under the version policy's temporary legacy exception; no release is prepared.
Completion is provisional until the sealed [receipt](../../../_boss-runs/milestone-b-sequential/foodus-003-receipt.json)
reconciles with the authorized per-Story commit.
