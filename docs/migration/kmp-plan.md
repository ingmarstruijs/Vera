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
| Cleanup | Webviewer/P2P removed from Flutter `main` (P0) |
| Signalling | Decommission `signal.vera-app.nl` (DNS; infra outside repo) |

## Target architecture

```
Vera/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/libs.versions.toml
├── shared/          # KMP: domain, SQLDelight, Koin, ViewModels, expect/actual
├── androidApp/      # Jetpack Compose (net.moonbaseone.vera)
├── iosApp/          # SwiftUI + Shared framework
└── docs/
```

**Not in v1:** `desktopApp`, `webApp`, Compose Multiplatform shared UI, `desktopMain`, `wasmJsMain`.

**Navigation (migration v1):** Timeline | Decoder | Appointments (| Settings later). **No** Share/Sync.  
**Phase 6 IA goal:** unlock → care plan → decoder/appointments (thinner shell; no legacy placeholders).

### Shared (`commonMain`)

- Domain models, Identity Shield (regex + ML actuals later), AES-GCM vault API  
- SQLDelight schema/queries, Mock FHIR, sealed `Failure`  
- i18n EN/NL/DE/FR, design tokens (not widgets)  
- Koin modules + shared ViewModels (`StateFlow`)

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
| 5 | Flutter decommission + docs finalize (P10 / P10b) |
| **6** | **Post-parity hardening / greenfield-inspired refactor** (optional/phased, **after P10**) |

### Phase 6 — post-parity / greenfield-inspired (**optional**, after P10)

Not part of migration parity. Start only after Android+iOS feature parity **and** Flutter removal (Phase 5 / P10 + P10b).

#### Keep

- Android + iOS only; no P2P/Webviewer/signalling  
- Privacy-first vault + Identity Shield  
- Kotlin domain + ViewModels/StateFlow + Koin  
- Capability interfaces for ML / speech / biometrics  

#### Change (checkable workstreams)

**Architecture**

- [ ] **Product decision gate (before UI unification):** Compose Multiplatform shared UI **vs** keep dual Compose + SwiftUI. Unify only if native-iOS feel is **not** a hard product requirement (reduces design drift). Migration-v1 dual-native stands until this gate is explicit.
- [ ] Split fat `shared` into feature modules (`vault`, `shield`, `decoder`, `appointments`) + thin app shells
- [ ] Typed/shared navigation (Decompose or Circuit) — do not defer until complexity forces it
- [ ] Stricter repository boundaries: local care-plan store vs future FHIR/MedMij client (boundary cleanup only; **no** MedMij production here)
- [ ] Scrub Flutter leftovers (debug auth-skip, Isar mental model in docs/comments)

**UI / product**

- [ ] Thinner IA: unlock → care plan → decoder/appointments (no legacy tabs/placeholders)
- [ ] Calmer clinical visual language (not 1:1 Flutter Material teal port); stronger hierarchy for oncology context
- [ ] Accessibility as default (Dynamic Type, contrast, voice UX) — not late hardening
- [ ] Proper i18n resource packs (NL/EN/…) instead of `UiCopy` stubs

**Release hardening (may ship first within P11)**

- [ ] detekt / ktlint, signing, a11y baseline checks

**MedMij/FHIR:** later compliance track. Phase 6 may only prep repository boundaries.

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
| **P10** | Remove Flutter tree | After Android+iOS parity |
| **P10b** | English KMP cutover docs | Docs phase C |
| **P11** | Post-parity hardening / greenfield-inspired refactor | **Optional/phased after P10:** detekt/signing/a11y; CMP-vs-dual gate; feature modules; typed nav; thinner IA; clinical UI; i18n packs; repo boundaries (Phase 6) |

**Dropped vs earlier drafts:** desktop (P8), web (P9) KMP targets.

**Parallel note:** P11 only after P10 (may phase internally: release hardening first, then module/UI refactor after CMP gate).

## Open decisions (post-parity; not blocking migration)

| Topic | Status |
|-------|--------|
| Apple Team / Play signing | Open |
| Reopen desktop/web? | Open; out of v1 |
| **CMP vs dual-native (Phase 6 gate)** | **Open product decision** — required before UI unification in P11 |
| Feature-module split + typed nav | Planned P11; optional/phased |
| MedMij/FHIR production | Later compliance track (boundary prep OK in P11) |

## Acceptance (short)

- [x] P0: no WebRTC/QR-share/viewer/`p2pSessionActive` on Flutter `main`  
- [x] Shared tests for FHIR mock + jargon glossary (P2)
- [x] Shared tests for BSN redaction + vault AES-GCM round-trip (P3)
- [x] Android EncryptedSharedPreferences + BiometricPrompt; iOS Keychain + LocalAuthentication (P4); iOS AES-GCM CryptoKit bridge still follow-up  
- [x] Android + iOS: unlock → timeline shell (decoder/appointments placeholders) (P5)  
- [x] Decoder + appointments feature port (P6); speech STT/TTS actuals on mobile; ML Kit translation still P7  
- [x] On-device translation + NER (P7): `LocalTranslationService` + ML Kit Translate/Entity Extraction (Android); NLTagger NER + Translate stub (iOS); DecoderViewModel download/translate flow  

- [x] Stack direction: Koin + StateFlow; SQLDelight stubs; IDs `net.moonbaseone.vera`  
- [x] No desktop/web KMP modules in v1  
- [ ] App i18n EN/NL/DE/FR (stubs OK through parity; real resource packs in **P11 / Phase 6**)  
- [ ] CI green: shared tests + Android build (+ iOS when runner available)
- [ ] **P11 / Phase 6 (optional, after P10):** CMP-vs-dual product gate taken; feature-module split and/or typed nav where chosen; thinner IA + a11y-default; no Flutter leftovers in KMP tree

## How to run (skeleton)

```bash
./gradlew :shared:testDebugUnitTest
./gradlew :androidApp:assembleDebug
open iosApp/Vera.xcodeproj   # macOS + Xcode
```
