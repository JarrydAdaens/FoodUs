---
name: 2026-07-26-addendum-ai-settings-branding-foodus
description: Dictation addendum - three-layer AI prompt architecture, AI settings screen (key/endpoint/model/system prompt), privacy policy URL, provider info links, and the FoodUs rename.
metadata:
  version: "1.0"
  owner: "Jarryd Adaens"
  repo: "FoodYou (fork of maksimowiczm/FoodYou)"
  dictated: "2026-07-26"
  tier: "Dictation (tier 0)"
---

# Dictation Addendum — 2026-07-26

> Raw dictation synthesized into structured items. Source: owner voice/chat session,
> 2026-07-26. Messy by design; promote durable decisions into Design / Milestone /
> Story tiers per the normal context lifecycle.

## Project state corrections (supersede stale context)

- **Milestone 1 is Complete.**
- **Milestone 2 is Complete.** The AI scanning screen exists on-device (Stories 6/7
  shipped: take-a-photo, pick-from-gallery). Milestone doc statuses are out of date.
- **Milestone 3 is being written** (separate session — not part of this addendum).
  Its spine is **multi-user support**, which motivates item 5's rename.
- Item numbering note: the owner's original "item 1" was defining Milestone 3 itself,
  handled elsewhere. The items below are re-enumerated 1–5.

---

## Item 1: Three-layer AI prompt architecture (revises the Story 6 design)

Replace the single embedded prompt ("we're Australians in Victoria, this is my food")
with three additive layers:

| Layer | Where | Contains | Applied |
| --- | --- | --- | --- |
| 1. Developer prompt | Baked into the app | **Stripped of ALL user data.** Pure machinery only: task framing, JSON output shape, whatever makes the system work. | Always |
| 2. User system prompt | AI settings screen (item 2), optional | All personal context, user-authored: e.g. "I'm Australian, vegetarian, never eat nuts, take photos with an S22 Ultra". Steering for every diet-related AI use. | Always, when set |
| 3. Per-scan hint | Text field on the AI scanning screen | One-off context: "I'm at McDonald's", "this is red lentils and bread". | That call only |

Layers are additive; layer 2 does not replace layer 1, layer 3 rides on top of both.

**AI scanning screen additions:**

- Free-text hint field (layer 3), optional.
- A big **Submit** button at the bottom. **Image required; text field optional.**

Rationale: moving user data out of the developer prompt makes the baked prompt
shippable and merge-safe, with nothing personal in the codebase.

## Item 2: AI settings screen (new story)

A settings surface (near/with the existing key entry) holding four fields:

1. **OpenAI-compatible API key** (user-entered).
2. **Endpoint** — defaults to OpenRouter; any OpenAI-compatible endpoint can be set.
3. **Model** — free-text model name, with a **Validate** button.
4. **User system prompt** — layer 2 above, optional.

**Validate behavior (decided, not open):** simplest possible — make a live call to the
configured endpoint with the entered model, key, and a trivial one-token prompt. Green
tick on success; surface the returned error string on failure. One code path, no
provider-specific handling; confirms key, endpoint, and model in a single shot.
Deliberately advanced-user; do not over-engineer.

**Design shift to note when promoting:** the original Milestone 2 design had the
owner's key *baked into private builds*. This item moves to **user-entered key +
user-set endpoint + user-set model** — more flexible, and it removes the baked-secret
problem (the Story 6 "secret-injection mechanism" open question dissolves).

## Item 3: Privacy policy link target (completes existing Story 3)

The in-app privacy policy link is wrong (an earlier session guessed the URL). Correct
target, owner-supplied:

```
https://jarrydadaens.github.io/privacy.html
```

This is the missing piece of the existing "replace the privacy policy link" story —
same story, now with its real destination.

## Item 4: Provider website "more info" links (new story)

Each remote/composition food-data provider has an import/update screen in-app. On
every such screen, add a button linking out to that provider's website for more info.
Apply to **all providers**, not just one.

**Open observation:** the owner described the *Australian* food compositional
database's screen; existing docs list only Swiss FCD, Open Food Facts, and USDA.
Either upstream added an Australian source, the screen was the Swiss one, or docs are
stale — confirm on-device during planning.

## Item 5: Rename to FoodUs (new story — likely Milestone 3)

The app becomes **FoodUs**, superseding both "Food You" and "ACME Food App".

- **Rationale:** Milestone 3 adds multi-user support — "Us", not "You".
- **Icon:** new icon, made by the owner personally; human-taste-driven. (Absorbs the
  earlier "app icon sucks, make a good one" note.)
- **About screen:** credits carry the owner's name.
- Upstream attribution requirements from the original identity story still apply.
- **Placement:** because the name only makes sense once multi-user lands, this story
  likely belongs to Milestone 3 rather than as a late Milestone 2 addition — decide
  during Milestone 3 writing.

---

## Promotion checklist (for the next context-maintenance pass)

- [ ] Correct Milestone 1/2 statuses (both Complete) in `design.md` and milestone docs.
- [ ] Revise Story 6's prompt design to the three-layer architecture (item 1).
- [ ] Add the AI settings story (item 2); retire the "baked key injection" open question.
- [ ] Update Story 3 with the real privacy policy URL (item 3).
- [ ] Add the provider-links story (item 4); verify the Australian-database question.
- [ ] Stage the FoodUs rename (item 5) into Milestone 3 or backlog as appropriate.
- [ ] Update the identity references (ACME Food App → FoodUs) in Design once decided.
