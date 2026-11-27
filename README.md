# Vera

**Your personal oncology compass** — clear, private care-plan guidance on device.

Kotlin Multiplatform (`:core` / `:shared`) + shared Compose UI (`:ui`). Android ships today; iOS hosts the same UI via CMP (pending).

`net.moonbaseone.vera` · local-first · EN / NL / DE / FR · no cloud ML for v1.

## Product

| Screen | What it does |
|--------|----------------|
| **Zorgplan** | Care-plan timeline (mock FHIR) + IKNL links; plain / clinical toggle |
| **Woordenboek** | On-device jargon → plain language |
| **Afspraken** | Navigator + **afspraak notities** (questions, recording, prep) |
| **Hoe het gaat** | Type or speak how you’re doing; shown in notes |
| **Contact** | Care-team phones + app language |

Debug builds: long-press the care-plan title to switch demo datasets.

## Layout

```
core/        domain, i18n, navigation, design tokens
shared/      features, SQLDelight, Koin, ViewModels
ui/          shared Compose UI
androidApp/  thin Android shell
iosApp/      CMP host placeholder
ci/          stable Obtainium preview keystore
```

## Build

```bash
./gradlew :shared:testDebugUnitTest :ui:assembleDebug :androidApp:assembleDebug
```

JDK 21 · Android SDK 35 — [docs/setup/android.md](docs/setup/android.md).

## Release (Obtainium)

Every `main` push → preview `0.1.<N>` · `v*` tags → GitHub Releases. Details: **[docs/RELEASING.md](docs/RELEASING.md)**.

| | |
|--|--|
| Obtainium | Include pre-releases · APK filter `^vera-[0-9].*\.apk$` |
| Signing | `ANDROID_KEYSTORE_*` secrets, else committed `ci/obtainium.keystore` |
| CI | [kmp-smoke](.github/workflows/kmp-smoke.yml) (PRs) · [build-release](.github/workflows/build-release.yml) |

## Docs

[Index](docs/README.md) · [Contributing](docs/contributing.md) · [Changelog](CHANGELOG.md) · [Signing](docs/setup/signing.md) · [MedMij](docs/integrations/medmij.md)

Repo docs stay English; product UI is localized.
