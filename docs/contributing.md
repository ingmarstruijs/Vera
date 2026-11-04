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

Automation: [`.github/workflows/changelog-on-merge.yml`](../.github/workflows/changelog-on-merge.yml) prepends a bullet under `### Merged` from the PR title and number when the PR is merged.

- Put a clear, user-facing summary in the **PR title** (that string is what lands in the log).
- Add `[skip changelog]` to the PR title to opt out (also used by the bot’s own follow-up commit message).
- Prefer editing `### Added` / `### Changed` / etc. in the PR when you want richer categorization; the bot only appends the merge bullet and skips if `#N` is already mentioned.

After branch protection is on, allow **github-actions[bot]** to bypass the required-PR rule so the changelog workflow can push its commit (see below).

## Protect `main` (GitHub Settings)

The repo token used by agents cannot write branch protection (HTTP 403). A repo admin should click:

1. Open **https://github.com/ingmarstruijs/Vera/settings/branches**
2. Under **Branch protection rules**, click **Add rule** (or edit the rule for `main`)
3. **Branch name pattern:** `main`
4. Enable:
   - **Require a pull request before merging**
     - Approving reviews can stay at **0** if you only want the PR gate (no review requirement yet)
   - **Require status checks to pass before merging**
     - Enable **Require branches to be up to date before merging** (recommended)
     - Search and select status check: **`Shared tests + Android assembleDebug`**
       - Tip: the check must have run at least once on the repo before it appears in the search box; merge any green PR first if the list is empty
   - **Do not allow bypassing the above settings** — leave unchecked for admins if you want admins forced through PRs too; or leave admin bypass on if preferred
5. Ensure **Allow force pushes** and **Allow deletions** stay **off**
6. **Save changes**

### Changelog bot bypass

Still on the same rule (or under **Rulesets** if you use those):

7. Allow **github-actions[bot]** to bypass the “require pull request” restriction so `Changelog on merge` can push `chore(changelog): record #N` to `main`.
   - Classic rules: **Restrict who can push to matching branches** is usually left off; if pushes from Actions fail with 403 after protection is on, switch to a **Ruleset** with a bypass actor `github-actions[bot]` / “Repository role: GitHub Actions”, or add a fine-grained PAT secret for that workflow.
   - Rulesets path: **Settings → Rules → Rulesets** → rule covering `main` → **Bypass list** → add **github-actions[bot]** (or “GitHub Actions”).

Until that bypass exists, merges still succeed; only the automatic CHANGELOG commit may fail — update `CHANGELOG.md` in the PR as a fallback.
