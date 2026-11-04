# Vera

**Your personal oncology compass.** Clear, private, trusted medical guidance — on your device.

Vera is a **Kotlin Multiplatform** app: shared Kotlin logic with **native UI** (Android Jetpack Compose, iOS SwiftUI). Product IDs use `net.moonbaseone.vera` (`vera.moonbase-one.net`).

> **v1 platforms:** Android + iOS only. Web and desktop are deferred.  
> **Not shipped:** Webviewer / P2P sharing (removed); signalling host `signal.vera-app.nl` is decommissioned outside this repo.

---

## Product

### Problem & solution

Patients sit between complex clinical data and generic internet advice. Vera turns diagnosis and care-plan data into a clear, personalized roadmap — without sending identifiable records to a central server.

### Core features

- **Blueprint** — Interactive care-plan timeline (mock FHIR CarePlan today)
- **Plain-Language Decoder** — On-device jargon glossary + translation (ML Kit on Android; NLTagger / Translate stub on iOS)
- **Appointment Navigator** — Pocket guide and voice symptom logging (STT/TTS on device)
- **Identity Shield** — Local PII redaction (regex + NER) before analysis
- **Local Vault** — AES-256-GCM encrypted records + biometric gate

### Privacy

- **Local-first** — Analysis stays on device; no cloud ML for v1
- **Encrypted vault** — AES-256-GCM + Android Keystore / iOS Keychain
- **Privacy sentinel** — Local-only status (no remote share path)

MedMij / production FHIR sync is a later compliance track; the app uses a mock FHIR R4 oncology bundle today.

---

## Architecture

```
Vera/
├── core/            # KMP: domain, failure, i18n, navigation, design tokens
├── shared/          # KMP: vault/shield/features, SQLDelight, Koin, ViewModels
├── androidApp/      # Jetpack Compose (applicationId net.moonbaseone.vera)
├── iosApp/          # SwiftUI + Shared framework (exports :core)
├── docs/            # English documentation
├── assets/fonts/    # Atkinson Hyperlegible (wire into app resources as needed)
├── settings.gradle.kts
└── gradle/
```

| Layer | Stack |
|-------|--------|
| Shared logic | Kotlin Multiplatform (`:core` + `:shared`), Koin, ViewModel / StateFlow, SQLDelight |
| Android UI | Jetpack Compose, Material 3 |
| iOS UI | SwiftUI |
| Crypto / keys | AES-GCM + EncryptedSharedPreferences / Keychain |
| ML | Android ML Kit Translate + Entity Extraction; iOS NLTagger (+ Translate stub) |
| CI | Shared/core tests, detekt, Android lint, assembleDebug (signing out of CI) |

**Not in v1:** `desktopApp`, `webApp`.

### CMP vs dual-native (Phase 6 / P11 product gate)

Compose Multiplatform **shared UI is not adopted** until an explicit product decision. Until then Vera stays **dual-native** (Jetpack Compose + SwiftUI) with shared Kotlin logic only. Unifying UI under CMP is optional post-parity work and must not be forced for design-drift reasons alone. See [docs/migration/kmp-plan.md](docs/migration/kmp-plan.md) and [docs/migration/module-map.md](docs/migration/module-map.md).

Migration history and Phase 6 / P11: [docs/migration/kmp-plan.md](docs/migration/kmp-plan.md).

---

## Prerequisites

- **JDK 21** (CI uses Temurin 21)
- **Android SDK** with platform 35 / build-tools 35 (for `androidApp`)
- **Xcode 15+** on macOS (for `iosApp`)

See [docs/setup/android.md](docs/setup/android.md) and [docs/setup/ios.md](docs/setup/ios.md).

---

## Build & run

```bash
# Shared unit tests (JVM / Android host)
./gradlew :shared:testDebugUnitTest

# Android debug APK
./gradlew :androidApp:assembleDebug

# Install / run on a connected device or emulator (Android Studio or):
./gradlew :androidApp:installDebug
```

**iOS** (macOS + Xcode):

```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
open iosApp/Vera.xcodeproj
```

Details: [iosApp/README.md](iosApp/README.md).

CI smoke workflow: [`.github/workflows/kmp-smoke.yml`](.github/workflows/kmp-smoke.yml) — shared tests + `assembleDebug`.

---

## Documentation

| Doc | Description |
|-----|-------------|
| [docs/README.md](docs/README.md) | Doc index |
| [docs/setup/android.md](docs/setup/android.md) | Android setup & build |
| [docs/setup/ios.md](docs/setup/ios.md) | iOS setup & build |
| [docs/testing.md](docs/testing.md) | How to run tests |
| [docs/migration/kmp-plan.md](docs/migration/kmp-plan.md) | Migration plan (in-repo mirror) |
| [docs/migration/module-map.md](docs/migration/module-map.md) | Feature package → module map (P11) |
| [CHANGELOG.md](CHANGELOG.md) | Notable changes |

App UI copy: EN / NL / DE / FR via shared `StringCatalog` (+ Android `values-*` packs).

---

## License / contributing

Private product repo. Prefer English in repository docs; keep platform UI multilingual via shared / platform strings.
