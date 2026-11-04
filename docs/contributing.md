# Contributing

## Branching

- Work on a feature branch (`cursor/…` for agent branches).
- Open a pull request targeting `main`.
- Do not push directly to `main` once branch protection is enabled.

## CI

**KMP smoke** runs on:

- `pull_request` → `main`
- `push` → `main`

It does **not** run on push to `cursor/**` (that used to double-fire alongside PR checks).

Required check name for branch protection:

`Shared tests + Android assembleDebug`

## Changelog

Every merge to `main` should appear in [`CHANGELOG.md`](../CHANGELOG.md) (Keep a Changelog–style under `## Unreleased`).

Automation: [`.github/workflows/changelog-on-merge.yml`](../.github/workflows/changelog-on-merge.yml) opens a follow-up PR (`chore/changelog-record-N`) that prepends a bullet under `### Merged` from the merged PR’s title and number. It does **not** push to `main` (so no Ruleset bypass for `github-actions[bot]` is required).

- Put a clear, user-facing summary in the **PR title** (that string is what lands in the log).
- Add `[skip changelog]` to the PR title to opt out (also used by the bot’s follow-up PR title so it does not recurse).
- Prefer editing `### Added` / `### Changed` / etc. in the feature PR when you want richer categorization; the bot only appends the merge bullet and skips if `#N` is already mentioned.
- Merge the bot’s changelog PR when its CI is green (or enable auto-merge).

## Protect `main` (GitHub Settings)

The repo token used by agents cannot write branch protection (HTTP 403). A repo admin should configure a **Ruleset** (or classic branch rule):

1. Open **https://github.com/ingmarstruijs/Vera/settings/rules** (Rulesets) or **…/settings/branches**
2. Create/edit a rule targeting **`main`**, enforcement **Active**
3. Enable:
   - **Require a pull request before merging** (approvals can stay at **0**)
   - **Require status checks to pass** → **`Shared tests + Android assembleDebug`**
     - Tip: the check must have run at least once before it appears in search
   - **Block force pushes** / **Restrict deletions**
4. **Bypass list:** leave empty if `github-actions[bot]` is not offered (normal). Changelog automation uses a PR instead.
5. **Save / Create**

No Actions bypass is required for the changelog workflow.
