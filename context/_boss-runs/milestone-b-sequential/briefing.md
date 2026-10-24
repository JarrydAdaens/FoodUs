# Milestone B sequential delivery

Source and sole Story status owner: [Milestone B](../../milestones/milestone-b.md).
Authorized queue: FOODUS-001, 002, 003, 005, 004, 006, 007, 008, 009, 010.
Dependency order places 005 before 004. One implementation worker at a time.

User authority (2026-10-24): implement all B Stories sequentially, test each completed
Story on the attached Samsung, then invoke commit-log --commit before the next.
No pull, push, tag, publication, or release. Preserve private device data; use the
side-by-side debug application and synthetic inputs. Release legacy version remains
until the policy's release transition is performed; closures do not publish builds.

Baseline HEAD: 4151d6a4bf9f90077987cb8309d858e248a7133f.
Pre-existing untracked `.codex-remote-attachments/` is excluded from all commits.
No staged or modified tracked files at setup.

Checks: targeted regression tests, debug assemble, matching Samsung runtime evidence
for every Story. Harness lists no installed modules. Independent scoped review for
consequential data, navigation, and privacy changes. Missing checks block closure.
Completion receipts live here as `<foodus-id>-receipt.json`; plans/evidence live in
`context/implementation-plans/milestone-b/<foodus-id>/plan.md`.

## Dispatch FOODUS-001

Intent: reuse existing AFCD management/import UI inside onboarding; preserve existing
source and skip paths. Worker owns onboarding files and onboarding regression tests,
and its plan only; coordinator owns milestone, Design index, receipts, commits, and
Samsung runtime verification. No other Stories in this dispatch.
Requested model/effort: inherit. Observed identity: unknown (host does not expose it).
Worker identity: canonical task `/root/foodus_001`; actual runtime ID follows dispatch.
Stop conditions: missing required acceptance evidence, architectural conflict, or
need to access private device data. Worker returns changes uncommitted.

FOODUS-001 acceptance: 120 unit tests, debug assemble, 2/2 Samsung Android15
instrumentation tests, and whitespace check passed. Initial test-container lifecycle
failure was corrected with private Koin context and cleanup identity assertion.
Receipt: `foodus-001-receipt.json`; completion commit pending at seal time.
No optional independent review selected for this bounded screen reuse.
