---
name: foodyou-docs-site-zensical
description: Scouting doc — the upstream Zensical static documentation site under docs/ (config, content, build/deploy workflow, why it's dead weight for the fork, and the removal footprint). Prep for eventual removal.
metadata:
  version: "1.0"
  owner: "Jarryd Adaens"
  repo: "FoodYou (fork of maksimowiczm/FoodYou)"
---

# Food You — Zensical Documentation Site (scouting doc)

> Reconnaissance of the static documentation-site generator inherited from upstream, living under
> `docs/`. Purpose: understand it before removing it. **This documents current state; it is not a
> removal instruction.** [Back to wiki home](home.md)
>
> Related: Milestone 1 Story 13 (own project infrastructure) already lists `docs/zensical.toml`
> among the upstream-pointing surfaces to sweep; this doc scopes the *remove-it-entirely* path
> instead. Also touches Milestone 2 Story 3 (replace the in-app privacy-policy link).

## TL;DR

- The site is built by **Zensical**, a static site generator from the Material-for-MkDocs team
  (Squidfunk). It compiles the Markdown under `docs/` into a static HTML site.
- It is **fully upstream's**: name, author, domain, copyright, repo link, Discord, Crowdin, and a
  "code contributions not accepted — this app generates income" notice. None of it describes the
  fork.
- It builds and **auto-deploys to GitHub Pages on every push to `main`** via
  `.github/workflows/docs.yml`. On the fork that workflow is dead weight (and would publish
  upstream-branded pages under the fork's Pages if Pages were enabled).
- **Not coupled to the app build** — no Gradle module, no compile-time dependency. Removing it does
  not affect the APK build.
- **One related coupling to reconcile:** the in-app privacy-policy link points at
  `https://foodyou.maksimowiczm.com/privacy-policy` (in `FoodYouConfig.kt`) — the page *this site's
  source produces*, but hosted on **upstream's own live domain**. Deleting the fork's `docs/` copy
  does **not** take that URL down, so the in-app link won't break; the real problem is that it sends
  the fork's users to *upstream's* privacy policy. Milestone 2 Story 3 already plans to swap it.

## What Zensical is

Zensical is a Markdown → static-HTML documentation site generator from Squidfunk (the makers of
Material for MkDocs), configured via a `zensical.toml` file rather than MkDocs' `mkdocs.yml`. It
produces a themed, searchable docs website. Here it is invoked purely in CI (`pip install zensical`
then `zensical build`); there is no local tooling checked into the repo beyond the config and
content.

## File map (everything that belongs to the site)

| Path | Role |
| --- | --- |
| `docs/zensical.toml` | Site config: project metadata, theme, nav, features, cookie consent, social links. |
| `docs/docs/` | **Content root** (the built site's `docs_dir`). Published pages live here. |
| `docs/docs/index.md` | Landing page ("Welcome" / feature overview). |
| `docs/docs/privacy-policy.md` | Privacy policy page — the target of the in-app privacy link. |
| `docs/docs/contribute.md` | Contribution page (issues, feature requests, Crowdin translations). |
| `docs/docs/images/` | `favicon.png`, `featureGraphic.png` used by the pages/theme. |
| `docs/overrides/` | Theme override dir (`custom_dir`); holds `.icons/sushi.svg`, the custom logo. |
| `docs/development/` | Dev-facing notes: a 5-entry `decision-log/` (ADRs) + `release.md`. **Not in the site nav** — see below. |
| `docs/.gitignore` | Ignores build output `site/` and `.cache/`. |
| `.github/workflows/docs.yml` | CI that builds and deploys the site to GitHub Pages. |

### Content root vs. dev notes

The config sits at `docs/zensical.toml`, so the effective `docs_dir` is `docs/docs/`. The `nav` only
lists three pages — `index.md`, `privacy-policy.md`, `contribute.md` — all of which live in
`docs/docs/`. The `docs/development/` folder (decision log + release process) sits **outside** the
content root and is **not** part of the generated website; it is just Markdown hosted in the repo.
Worth knowing when deciding what to keep: the ADRs are upstream's engineering history, not published
site pages.

## What `zensical.toml` declares (all upstream identity)

- `site_name = "Food You"`, `site_author = "Mateusz Maksimowicz"`,
  `site_url = "https://foodyou.maksimowiczm.com/"`, `repo_url = ".../maksimowiczm/FoodYou"`.
- `copyright` crediting Mateusz Maksimowicz (logo by Icons8).
- Material-style theme: light (`default`) + dark (`slate`) palettes, `sushi` logo, GitHub repo icon.
- A Discord social link (`discord.gg/MuF6VZjufn`) and a cookie-consent banner.
- A large, mostly-commented `features` list (navigation, code blocks, search, TOC behaviors).

`contribute.md` further carries upstream-only context: a "code contributions not accepted (the app
generates income)" danger notice and a Crowdin translation link — neither applies to a private fork.

## How it builds and deploys

`.github/workflows/docs.yml`:

1. Trigger: `push` to `main`.
2. Permissions: `pages: write`, `id-token: write`; environment `github-pages`.
3. Steps: configure Pages → checkout → set up Python 3.x → `pip install zensical` →
   `zensical build --clean -f docs/zensical.toml` → upload `docs/site` as the Pages artifact →
   `deploy-pages`.

Net effect on the fork: every push to `main` attempts to build and publish this upstream-branded
site to the fork's GitHub Pages. It is redundant (there is no audience for a personal fork's public
docs) and misattributing if it ever succeeds.

## Why it's dead weight for this fork

- **No fork value:** the fork is a private/household build; a public documentation website has no
  audience.
- **Entirely upstream-branded:** author, domain, copyright, repo, Discord, Crowdin, income notice —
  removing the site is cleaner than re-owning pages nobody will read.
- **CI cost with no payoff:** the deploy workflow runs on every `main` push.

## Removal footprint (for when we pull the trigger later)

Delete-set (self-contained; no app-build impact):

- `docs/` (the whole tree: `zensical.toml`, `docs/`, `development/`, `overrides/`, `.gitignore`).
- `.github/workflows/docs.yml`.

**Reconcile — the one related dependency (not a hard blocker):**

- `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/infrastructure/FoodYouConfig.kt`
  sets `privacyPolicyUri = "https://foodyou.maksimowiczm.com/privacy-policy"`. That URL is on
  **upstream's live domain**, so deleting the fork's `docs/` copy does not break it — the page keeps
  resolving from upstream. The issue is purely that the fork should not ship users to upstream's
  privacy policy. Milestone 2 Story 3 already plans to swap this link to the owner's own policy;
  deleting the docs source and swapping the link are independent tasks that happen to concern the
  same page.

No other coupling found:

- No Gradle reference (`settings.gradle.kts` / `build.gradle.kts` don't know about `docs/`).
- Repo-wide search for `zensical` / the docs domain turns up only `docs.yml` and the one
  `FoodYouConfig.kt` link above. (The `docs.google.com` poll URL in `StaticPollRepository.kt` is
  unrelated — a Google Forms link, not the docs site.)

## Decision-log note (upstream, informational)

`docs/development/decision-log/0004-close-source-development.md` and `0005-deprecate-version-3.md`
record upstream's move to restrict development-branch access and deprecate the 3.x line. Not
actionable for the fork, but useful background on upstream's posture if merge decisions come up.
