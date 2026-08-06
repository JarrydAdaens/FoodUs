# Run 2026-07-28 — blocked stories record

Eight of the thirteen in-scope Milestone 3 stories are gate-blocked, not failed. None were
dispatched; none should be retried until the named gate releases.

| Story | Blocking gate | Unblock condition |
| --- | --- | --- |
| 3.6 Friend codes | foodus-relay friend-code endpoints, contract v1, deployed | Owner releases the Story 5 gate |
| 3.7 Friends list | foodus-relay resolve + block endpoints, contract v1, deployed | Owner releases the Story 5 gate |
| 3.8 Envelope & E2E pipeline | No plan exists; unknown-version envelope disposition unsettled in the wire contract | Contract settles the question → plan generated → Story 5 gate |
| 3.9 Groups | Story 8 pipeline absent; two staged owner decisions OPEN (block-notice wording, meal-plan editing scope) | Story 8 lands + owner answers |
| 3.10 Save to Group | Story 8 pipeline absent; None-trust visibility decision OPEN | Story 8 lands + owner answers |
| 3.11 Receive into diary | Story 8 pipeline absent; carry-over/matching assumptions OPEN | Story 8 lands + owner confirms |
| 3.12 Suggestion queue | Stories 8/9/11 absent | Those stories land |
| 3.14 Household proof | Manual two-phone research protocol against the live relay | Story 5 gate + Stories 6-13 + both phones configured via 3.15 |

Context: the relay is being implemented in foodus-relay concurrently with this run (2026-07-28
overnight). Per the user's instruction this run did not read that repo. Re-queue candidates for
tomorrow's iteration once the owner confirms contract v1 / deployment state.
