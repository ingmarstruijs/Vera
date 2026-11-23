# Contributing

## Branching

- Work on a feature branch (`cursor/…` for agent branches).
- Open a pull request targeting `main`.
- Do not push directly to `main` once branch protection is enabled.

## CI

**KMP smoke** runs on:

- `pull_request` → `main`
- `push` → `main`
- `workflow_dispatch` (manual)

It does **not** run on push to `cursor/**` (that used to double-fire alongside PR checks). Superseded runs on the same ref are cancelled (`concurrency`).

Required check name for branch protection:

`Shared tests + Android assembleDebug`

**Build & Sign Android** (Obtainium / GitHub Releases) runs on every `main` push and on `v*` tags — see [RELEASING.md](RELEASING.md). It is **not** a PR required check; merge only when KMP smoke is green.

## Changelog

Every meaningful PR should update [`CHANGELOG.md`](../CHANGELOG.md) under `## Unreleased` (Keep a Changelog–style).

There is **no** changelog GitHub Actions bot. Agents follow [`.cursor/rules/changelog.mdc`](../.cursor/rules/changelog.mdc):

- Prefer categorized bullets (`### Added` / `### Changed` / `### Fixed` / …) in the feature PR itself.
- Optionally add a `### Merged` line with the PR number once known.
- Do not open `chore/changelog-record-*` follow-up PRs.

## Protect `main` (GitHub Settings)

The repo token used by agents cannot write branch protection (HTTP 403). A repo admin should configure a **Ruleset** (or classic branch rule):

1. Open **https://github.com/ingmarstruijs/Vera/settings/rules** (Rulesets) or **…/settings/branches**
2. Create/edit a rule targeting **`main`**, enforcement **Active**
3. Enable:
   - **Require a pull request before merging** (approvals can stay at **0**)
   - **Require status checks to pass** → **`Shared tests + Android assembleDebug`**
     - Tip: the check must have run at least once before it appears in search
   - **Block force pushes** / **Restrict deletions**
4. **Bypass list:** leave empty unless you intentionally allow a bot or admin to skip checks.
5. **Save / Create**
