# Testing

Vera’s automated tests live in the KMP `shared` module (`shared/src/commonTest`). There is no Flutter / Dart test tree.

## Run locally

```bash
# Shared unit tests on the Android JVM host (same target as CI)
./gradlew :shared:testDebugUnitTest

# Smoke: Android debug assemble
./gradlew :androidApp:assembleDebug
```

CI runs both steps via [`.github/workflows/kmp-smoke.yml`](../.github/workflows/kmp-smoke.yml) on `main` and `cursor/**` branches.

## What is covered (shared)

| Area | Location (under `shared/src/commonTest`) |
|------|------------------------------------------|
| Domain models | `domain/` — jargon, medical record, timeline |
| Mock FHIR | `fhir/MockDataServiceTest.kt` |
| Identity Shield | `shield/IdentityShieldServiceTest.kt` |
| Vault crypto | `vault/VaultCryptoTest.kt`, `LocalVaultServiceTest.kt` |
| Translation | `translation/LocalTranslationServiceTest.kt` |
| ViewModels | `auth/`, `blueprint/`, `decoder/`, `appointments/` |

Platform actuals (Keystore, ML Kit, speech) need device / instrumented coverage later; CI today is shared unit tests + Android assemble.

## iOS

There is no GitHub Actions macOS job yet for `iosApp`. Compile locally with Xcode after linking `Shared.framework`.
