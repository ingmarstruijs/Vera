# Testing

Vera’s automated tests live in the KMP `shared` module (`shared/src/commonTest`). There is no Flutter / Dart test tree.

## Run locally

```bash
# Core + shared unit tests on the Android JVM host (same target as CI)
./gradlew :core:testDebugUnitTest :shared:testDebugUnitTest

# Static analysis (KMP source sets + Android UI)
./gradlew :core:detekt :shared:detekt :androidApp:detekt
./gradlew :androidApp:lintDebug

# Smoke: Android debug assemble
./gradlew :androidApp:assembleDebug
```

CI runs these via [`.github/workflows/kmp-smoke.yml`](../.github/workflows/kmp-smoke.yml) on `pull_request` and `push` to `main` only (not on `cursor/**` pushes). Signing secrets are not required — see [setup/signing.md](setup/signing.md). Branching and changelog automation: [contributing.md](contributing.md).

## What is covered

| Area | Location |
|------|----------|
| Domain / i18n / nav | `core/src/commonTest` |
| Mock FHIR | `shared/.../fhir/MockDataServiceTest.kt` |
| Identity Shield | `shared/.../shield/IdentityShieldServiceTest.kt` |
| Vault crypto | `shared/.../vault/` |
| Translation | `shared/.../translation/LocalTranslationServiceTest.kt` |
| ViewModels | `shared/.../{auth,blueprint,decoder,appointments}/` |
| Detekt | `config/detekt/` (+ baseline) |
| Android a11y-minded lint | `:androidApp:lintDebug` |

Platform actuals (Keystore, ML Kit, speech) need device / instrumented coverage later.

## iOS

There is no GitHub Actions macOS job yet for `iosApp`. Compile locally with Xcode after linking `Shared.framework`.
