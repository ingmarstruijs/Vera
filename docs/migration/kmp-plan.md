# Vera — Kotlin Multiplatform migration plan (in-repo mirror)

**Repo:** [github.com/ingmarstruijs/Vera](https://github.com/ingmarstruijs/Vera)  
**Goal:** Flutter/Dart → **Kotlin Multiplatform (shared logic)** + **native UI** (Android Jetpack Compose, iOS SwiftUI)  
**Product domain / IDs:** `vera.moonbase-one.net` → `net.moonbaseone.vera`  
**Out of scope for migration:** Webviewer / WhatsApp Web-style P2P sharing (removed in P0; signalling host `signal.vera-app.nl` to be decommissioned outside this repo)

> This file mirrors the Project Store plan for repo locality. Update both when decisions change. Repo docs are **English only**.

## Decided scope (v1)

| Topic | Decision |
|-------|----------|
| Platforms v1 | **Android + iOS only** — web deferred; desktop out of scope |
| UI | **Migration v1: native per platform** — shared = Kotlin logic; no CMP shared UI until **Phase 6 product gate** |
| DI / state | **Koin + ViewModel/StateFlow** |
| Storage | **SQLDelight** + platform secure storage for keys |
| ML | **Android + iOS only** |
| Docs (repo) | **English only**; app UI i18n EN, NL, DE, FR (+ extensible) |
| Cleanup | Webviewer/P2P removed (P0); Flutter tree removed (P10) |
| Signalling | Decommission `signal.vera-app.nl` (DNS; infra outside repo) |

## Target architecture

```
Vera/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/libs.versions.toml
├── core/            # KMP: domain, failure, i18n, navigation, design tokens
├── shared/          # KMP: vault/shield/features, SQLDelight, Koin, ViewModels, expect/actual
├── androidApp/      # Jetpack Compose (net.moonbaseone.vera)
├── iosApp/          # SwiftUI + Shared framework (exports :core)
└── docs/
```

**Not in v1:** `desktopApp`, `webApp`, Compose Multiplatform shared UI, `desktopMain`, `wasmJsMain`.

**Navigation:** unlock → **care plan** → decoder → appointments (| Settings later). **No** Share/Sync.  
**Phase 6 IA:** thinner shell (`AppDestination` + `DefaultAppNavigator`); Decompose/Circuit deferred — [navigation.md](navigation.md).

### Shared layers (`commonMain`)

- **`:core`:** domain models, sealed `Failure`, i18n EN/NL/DE/FR, design tokens, typed nav  
- **`:shared`:** Identity Shield, AES-GCM vault API, SQLDelight, Mock FHIR, Koin, ViewModels (`StateFlow`)  

### Native UI

1. **Android:** Jetpack Compose + Material 3  
2. **iOS:** SwiftUI (+ UIKit bridges where needed)  
3. **Shared:** no screens in `commonMain` (revisit CMP in Phase 6 gate)

## Migration phases (summary)

| Phase | Focus |
|-------|--------|
| 0 | Remove Webviewer/P2P + docs scrub A |
| 1 | KMP skeleton + CI (P1) |
| 2 | Domain & services (common + android/ios actuals) |
| 3 | Native UI port (shell → auth → timeline → decoder → appointments) |
| 4 | Platform hardening (Keystore/Keychain, a11y, fonts) |
| 5 | Flutter decommission + docs finalize (**P10 / P10b — done**) |
| **6** | **Post-parity hardening / greenfield-inspired refactor** (optional/phased, **after P10**) |

### Phase 5 — done (P10 + P10b)

- Removed Flutter `lib/`, `test/`, `pubspec.yaml`, Flutter `android/` / `ios/` / `web/`, `analysis_options.yaml`
- English docs cutover: README, `docs/setup/*`, `docs/testing.md`, `CHANGELOG.md`
- Sole truth: `shared` + `androidApp` + `iosApp`

### Phase 6 — post-parity / greenfield-inspired (**optional**, after P10)

Not part of migration parity. Start only after Android+iOS feature parity **and** Flutter removal (Phase 5 / P10 + P10b — complete).

#### Keep

- Android + iOS only; no P2P/Webviewer/signalling  
- Privacy-first vault + Identity Shield  
- Kotlin domain + ViewModels/StateFlow + Koin  
- Capability interfaces for ML / speech / biometrics  

#### Change (checkable workstreams)

**Architecture**

- [x] **Product decision gate documented (P11 slice):** CMP shared UI **vs** dual Compose + SwiftUI remains an **open product decision**. Until decided explicitly, **stay dual-native** — do not force CMP. Gate recorded in README + this plan + [module-map.md](module-map.md).
- [~] Split fat `shared` into feature modules — **partial:** `:core` Gradle extract + Koin feature modules ([module-map.md](module-map.md)); `:vault` / `:shield` / … still follow-up
- [~] Typed/shared navigation — **partial:** `AppDestination` + `DefaultAppNavigator` (`StateFlow`); Decompose/Circuit **deferred** with next step in [navigation.md](navigation.md)
- [x] Stricter repository boundaries: `CarePlanRepository` (local) vs `FhirClient` stub (future MedMij) — **no** MedMij production
- [x] Scrub Flutter leftovers (removed unused `PlaceholderScreen`; no debug auth-skip / Isar comments in Kotlin)

**UI / product**

- [x] Thinner IA: unlock → care plan → decoder/appointments (route `careplan`; no placeholder tabs)
- [~] Calmer clinical visual language — **partial:** DesignTokens + Android theme + iOS `ClinicalColors` parity; further polish optional
- [~] Accessibility as default — **partial:** headings/content descriptions, Dynamic Type, lint a11y enables in CI; deeper audits optional
- [x] Proper i18n resource packs EN/NL/DE/FR (`StringCatalog`) — `UiCopy` is a thin facade

**Release hardening (may ship first within P11)**

- [x] detekt + Android lint (a11y-minded) in CI; signing documented out-of-CI ([setup/signing.md](../setup/signing.md)) — ktlint optional later

**MedMij/FHIR:** later compliance track. Phase 6 may only prep repository boundaries.

**P11 progress:** first slice + leftover hardening (`:core`, detekt/lint CI, nav deferral, dual-UI token polish). Still open: further Gradle feature extracts, Decompose/Circuit adoption when needed, explicit CMP product decision, optional ktlint.

## PR / workstream order

| # | Workstream | Content |
|---|------------|---------|
| **P0** | Remove webviewer/P2P | Done on `main` |
| **P1** | KMP skeleton Android+iOS | `shared` + `androidApp` + `iosApp`; Koin/SQLDelight stubs; CI |
| **P1b** | Docs stub + plan mirror | Docs phase B (this file + README WIP) |
| **P2** | Domain + mock FHIR + jargon | Shared tests |
| **P3** | SQLDelight + vault crypto | Secure-storage interfaces |
| **P4** | Keystore/Keychain + biometrics | Platform actuals |
| **P5–P6** | UI shell + features | Compose + SwiftUI; no sync tab |
| **P7** | ML Kit NER/translation | Android ML Kit + iOS NLTagger / Translate stub |
| **P10** | Remove Flutter tree | **Done** — no Flutter hosts remaining |
| **P10b** | English KMP cutover docs | **Done** — docs phase C |
| **P11** | Post-parity hardening / greenfield-inspired refactor | **Optional/phased after P10:** detekt/signing/a11y; CMP-vs-dual gate; feature modules; typed nav; thinner IA; clinical UI; i18n packs; repo boundaries (Phase 6) |

**Dropped vs earlier drafts:** desktop (P8), web (P9) KMP targets.

**Parallel note:** P11 only after P10 (may phase internally: release hardening first, then module/UI refactor after CMP gate).

## Open decisions (post-parity; not blocking migration)

| Topic | Status |
|-------|--------|
| Apple Team / Play signing | Open |
| Reopen desktop/web? | Open; out of v1 |
| **CMP vs dual-native (Phase 6 gate)** | **Open** — dual-native continues; CMP not forced without explicit decision |
| Further feature-module Gradle extract (`:vault`/…) | Follow-up (`:core` landed) |
| Decompose/Circuit | Deferred — concrete next step in [navigation.md](navigation.md) |
| MedMij/FHIR production | Later compliance track (boundary prep landed in P11) |

## Acceptance (short)

- [x] P0: no WebRTC/QR-share/viewer/`p2pSessionActive`  
- [x] Shared tests for FHIR mock + jargon glossary (P2)
- [x] Shared tests for BSN redaction + vault AES-GCM round-trip (P3)
- [x] Android EncryptedSharedPreferences + BiometricPrompt; iOS Keychain + LocalAuthentication (P4); iOS AES-GCM CryptoKit bridge still follow-up  
- [x] Android + iOS: unlock → timeline shell (decoder/appointments placeholders) (P5)  
- [x] Decoder + appointments feature port (P6); speech STT/TTS actuals on mobile  
- [x] On-device translation + NER (P7): `LocalTranslationService` + ML Kit Translate/Entity Extraction (Android); NLTagger NER + Translate stub (iOS); DecoderViewModel download/translate flow  
- [x] **P10:** Flutter tree removed (`lib/`, Flutter hosts, `pubspec`, web/)  
- [x] **P10b:** English README + `docs/setup/*` + `docs/testing.md` + CHANGELOG; this plan current  

- [x] Stack direction: Koin + StateFlow; SQLDelight; IDs `net.moonbaseone.vera`  
- [x] No desktop/web KMP modules in v1  
- [x] App i18n EN/NL/DE/FR resource packs (`StringCatalog`) — P11 first slice  
- [x] CI green: shared tests + Android build (+ iOS when runner available)
- [~] **P11 / Phase 6 (optional, after P10):** first + leftover slices landed (`:core` extract; detekt + lint CI; signing docs; `DefaultAppNavigator`; dual-UI token/a11y polish; Decompose deferred). Still open: CMP decision, further feature Gradle modules, Decompose/Circuit when product needs it

## How to run

```bash
./gradlew :shared:testDebugUnitTest
./gradlew :androidApp:assembleDebug
open iosApp/Vera.xcodeproj   # macOS + Xcode
```

See also [docs/setup/android.md](../setup/android.md), [docs/setup/ios.md](../setup/ios.md), [docs/testing.md](../testing.md).
