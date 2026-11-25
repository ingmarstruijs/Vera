# Vera

**Your personal oncology compass** — clear, private care-plan guidance on device.

Kotlin Multiplatform **logic** (`:core` / `:shared`) + **one shared Compose UI** (`:ui`). Android ships today (`androidApp`); iOS uses the same UI via Compose Multiplatform (host pending — SwiftUI removed).

IDs: `net.moonbaseone.vera` · local-first · no cloud ML for v1.

## Features

- **Zorgplan** — care-plan timeline (mock FHIR) with IKNL source links; debug builds can switch demo datasets (long-press title)
- **Woordenboek** — on-device jargon → plain language
- **Afspraken** — pocket guide per visit + symptom logging (header mic)
- **Contacts** — care-team list
- **Vault** — AES-256-GCM + biometrics

## Layout

```
core/        KMP domain, i18n, navigation, design tokens
shared/      KMP features, SQLDelight, Koin, ViewModels
ui/          Shared Jetpack Compose UI (CMP iOS next)
androidApp/  Thin Android application shell
iosApp/      Placeholder for CMP iOS host (no SwiftUI)
```

## Build

```bash
./gradlew :shared:testDebugUnitTest :ui:assembleDebug :androidApp:assembleDebug
```

JDK 21 · Android SDK 35. Setup: [docs/setup/android.md](docs/setup/android.md).

## Release (Obtainium)

Same habit as Solo — details: **[docs/RELEASING.md](docs/RELEASING.md)**.

| Channel | Trigger | Tag / version |
|---------|---------|----------------|
| Preview | Every push to `main` (+ manual dispatch) | `0.1.<N>` pre-release · `vera-0.1.<N>.apk` |
| Milestone | Annotated `vX.Y.Z-…` on `main` | GitHub pre-release |
| Stable | Annotated `vX.Y.Z` on `main` | Full GitHub Release |

- `versionCode` = Actions `run_number`; preview tag **equals** `versionName` (Obtainium-friendly).
- Optional secrets: `ANDROID_KEYSTORE_*` (else debug-signed).
- Obtainium: include pre-releases · APK filter `^vera-[0-9].*\.apk$`.

CI: [kmp-smoke](.github/workflows/kmp-smoke.yml) (required on PRs) · [build-release](.github/workflows/build-release.yml) (APKs on `main` / `v*`).

## Docs

[Index](docs/README.md) · [Contributing](docs/contributing.md) · [Changelog](CHANGELOG.md) · [Testing](docs/testing.md) · [MedMij notes](docs/integrations/medmij.md) · [Signing](docs/setup/signing.md)

EN / NL / DE / FR via shared `StringCatalog`. Prefer English in repo docs.
