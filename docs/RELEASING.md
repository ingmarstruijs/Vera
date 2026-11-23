# Releasing Vera (for maintainers / agents)

`main` should stay protected: no direct pushes for releases.

**Day-to-day Android installs** use **GitHub Releases + [Obtainium](https://github.com/ImranR98/Obtainium)** (same habit as Solo). Play Store, App Store / TestFlight, and F-Droid are later channels — not required for early device testing.

| Channel | When | Signing | Consumer |
| --- | --- | --- | --- |
| **GitHub preview `0.1.<N>`** | Every `main` push that builds Android (and `workflow_dispatch`) | Project keystore if secrets exist; else debug | Obtainium (include pre-releases) |
| **GitHub `v*`** | Annotated tag on `main` | Project keystore (CI) | Obtainium / sideload |
| **Play / TestFlight / F-Droid** | Later product decision | Store / F-Droid keys | Later |

Agents: follow this file. Quality gate before merge: [contributing.md](contributing.md) (KMP smoke). Signing secrets: [setup/signing.md](setup/signing.md).

## Version numbering (SemVer + Android)

One phone APK (`net.moonbaseone.vera`). Inject via `-PveraVersionName` / `-PveraVersionCode` (see `versioning.gradle.kts`).

| Channel | GitHub tag | `versionName` | `versionCode` | GitHub Release |
| --- | --- | --- | --- | --- |
| **Preview** (early) | `0.1.<N>` (unique each build; **no** `v` prefix) | `0.1.<N>` | `N` = Actions `run_number` | Pre-release; APK `vera-0.1.<N>.apk` |
| **Milestone** | `v0.1.0-android.N` (or `vX.Y.Z-rc.N`) | tag without `v` | Actions `run_number` | Pre-release |
| **Production** | `vX.Y.Z` (no `-` suffix) | `X.Y.Z` | Actions `run_number` | Full release |

Rules:

- **`versionCode` always increases** (CI run number) so PackageManager treats the build as newer.
- **Preview `versionName` is `0.1.<N>`** (not `0.1.0-dev.<N>`). Obtainium’s default GitHub detector often strips SemVer pre-release suffixes after `-`, so `0.1.0-dev.9` and `0.1.0-dev.11` both looked like `0.1.0` and never offered an update.
- **Preview GitHub tag must equal `versionName` and change every build.** Obtainium’s GitHub source uses **`tag_name` as the version** by default. A floating `preview` tag therefore never looks like an update.
- Preview tags omit the `v` prefix so they do not re-trigger the workflow’s `v*` path.
- Milestone/production still use normal SemVer; a `-` in the tag → GitHub pre-release.
- Local builds without `-P` default to `versionName=0.1.0`, `versionCode=1`.
- iOS versioning stays in Xcode for now; this document covers the Android Obtainium channel only.

Manual dispatch may pass a custom `version` input as `versionName`; channel stays `preview` and `versionCode` is still the run number. Prefer `0.1.<N>`-shaped labels so Obtainium compares cleanly.

## Early Android installs (Obtainium) — preferred while iterating

Workflow: [`.github/workflows/build-release.yml`](../.github/workflows/build-release.yml).

1. Merge Android work to `main` (or run **Build & Sign Android** → `workflow_dispatch`).
2. CI publishes a **unique** pre-release tag **`0.1.<N>`** (= `versionName`) with:
   - `vera-0.1.<N>.apk` — phone (`net.moonbaseone.vera`)
   - `sha256.txt`
   - APK metadata: `versionName=0.1.<N>`, `versionCode=<N>` (`N` = Actions `run_number`)
   - Legacy floating tag `preview` is deleted if still present
3. On the phone, add the repo in Obtainium:

| Setting | Value |
| --- | --- |
| Source | `https://github.com/ingmarstruijs/Vera` |
| Include pre-releases | **On** (required for `0.1.<N>` preview tags) |
| APK filter | `^vera-[0-9].*\.apk$` |
| Release title as version | Off (not needed — tag == versionName) |

Without `ANDROID_KEYSTORE_*` secrets, CI signs with the debug key — fine for private testing; set the project keystore before sharing widely (especially before any patient-facing installs) so updates stay on one cert.

### Required GitHub secrets (project signing)

| Secret | Purpose |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded `.jks` / `.keystore` |
| `ANDROID_KEYSTORE_PASSWORD` | Store password |
| `ANDROID_KEY_ALIAS` | Key alias |
| `ANDROID_KEY_PASSWORD` | Key password (defaults to store password if omitted in CI) |

## Stable GitHub Releases (`v*`)

1. Bump / feature PRs → merge to `main` after **KMP smoke** is green
2. Tag the merge commit: `git tag -a vX.Y.Z -m "Vera X.Y.Z" && git push origin vX.Y.Z`
3. Tag must be an ancestor of `main` or the release job fails
4. CI creates a GitHub Release for that tag with the versioned APK

A `v*` tag **not** on `main` must not create GitHub Releases (gated in the workflow).

Versions with a SemVer pre-release suffix (`v0.2.0-android.3`) publish as GitHub **pre-releases**.

## Verify release APKs

```bash
sha256sum vera-0.1.*.apk
keytool -printcert -jarfile vera-0.1.*.apk
```

## Local builds

```bash
# Debug (same as CI smoke)
./gradlew :androidApp:assembleDebug

# Release (debug-signed unless keystore.properties is present)
./gradlew :androidApp:assembleRelease

# CI-shaped version injection
./gradlew :androidApp:assembleRelease -PveraVersionCode=42 -PveraVersionName=0.1.42
```

iOS: see [setup/ios.md](setup/ios.md) and [iosApp/README.md](../iosApp/README.md). Not part of the Obtainium channel.
