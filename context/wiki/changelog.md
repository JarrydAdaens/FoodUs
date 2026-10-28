# FoodUs What's New

## Owner's writing rules

Write for people who use the app. Include only the main new features and important
improvements, usually three to five short bullets per release. Explain what people
can now do. Leave out commit lists, Story IDs, test counts, code changes and minor
button or icon edits.

Use plain English inspired by ASD-STE100, simplified further for everyday readers.
Use common words, active verbs and short sentences. Give each bullet one main idea.
Use familiar app terms such as Log, meal, recipe and food. Avoid technical words and
formal specification language. This is a writing approach, not a claim of formal
ASD-STE100 compliance. Do not use the engineering commit-log template for app notes.

## When asked to update the changelog

1. Read the catalog version in `gradle/libs.versions.toml` and inspect the release
   ledger, accepted Story evidence and changes since the previous release.
2. Edit `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/changelog/infrastructure/StaticChangelog.kt`.
   This is the source used by the app's What's New sheet. The inherited
   `metadata/en-US/changelog/` files do not populate that sheet.
3. Add or update the current release's highlights, newest first. Check for missing
   earlier published releases. Match version strings exactly; never truncate them
   or add build-type suffixes. Keep old entries and their history distinctions.
4. Use the actual UTC publication date for a published release. For an explicitly
   authorized unpublished candidate, use its planned date and record that status
   in the release plan. Recheck the date before publication if preparation spans days.
   A changelog entry does not establish publication or increase ReleaseCount.
5. Update the common history tests and run the release-changelog gate. When adding
   entries, run the existing native history checks for scrolling, the Installed
   badge and dismissal; use a disposable emulator and synthetic settings.

This instruction also applies automatically whenever an agent prepares, creates
or moves a release tag. Update and verify the in-app highlights **before tagging**,
and include them in the tagged commit. Do not wait for a separate changelog request.
The release workflow checks the candidate entry before building a tagged APK.
The gate checks presence and structure; the agent must still review the facts and
writing quality. Keep GitHub's technical release ledger separate from these app notes.

Only move a local unpublished tag with owner authorization and after confirming
that it is absent remotely. Preserve published tags and release history. Follow
the version policy and update runbook for commits, branches, signing and publication.

## October 2026 backfill evidence

- `3.2.39.4`: GitHub publication `2026-10-26T20:52:22Z`, source
  `ec4b612450b76a3b8d56b873ccfc6297a7d7a366`. Compared with `v2.23.0`:
  selected AI foods and preserved nutrition (Milestone B), reusable recipes/meals
  and library import (Stories 24-28), diary-slot bulk actions (Story 39).
- `3.2.41.5`: owner-authorized candidate for 2026-10-28, still unpublished at this
  edit. Includes Story 38's unified library, the first phone-review batch and
  Story 40's accepted food-entry and Log refinements. Its existing release count
  and Android code remain unchanged by this content correction.
- Older entries retain the evidence and founding-announcement distinction in
  `context/implementation-plans/milestone-b/foodus-009/plan.md`.
