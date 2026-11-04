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
| UI | **Native per platform** — shared = Kotlin business logic; no Compose Multiplatform shared UI |
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

**Navigation tabs:** Timeline | Decoder | Appointments (| Settings later). **No** Share/Sync.

### Shared (`commonMain`)

- Domain models, Identity Shield (regex + ML actuals later), AES-GCM vault API  
- SQLDelight schema/queries, Mock FHIR, sealed `Failure`  
- i18n EN/NL/DE/FR, design tokens (not widgets)  
- Koin modules + shared ViewModels (`StateFlow`)

### Native UI

1. **Android:** Jetpack Compose + Material 3  
2. **iOS:** SwiftUI (+ UIKit bridges where needed)  
3. **Shared:** no screens in `commonMain`

## Migration phases (summary)

| Phase | Focus |
|-------|--------|
| 0 | Remove Webviewer/P2P + docs scrub A |
| 1 | KMP skeleton + CI (this PR / P1) |
| 2 | Domain & services (common + android/ios actuals) |
| 3 | Native UI port (shell → auth → timeline → decoder → appointments) |
| 4 | Platform hardening (Keystore/Keychain, a11y, fonts) |
| 5 | Flutter decommission + docs finalize |

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
| **P7** | ML Kit NER/translation | Android + iOS only |
| **P10** | Remove Flutter tree | After Android+iOS parity |
| **P10b** | English KMP cutover docs | Docs phase C |

**Dropped vs earlier drafts:** desktop (P8), web (P9) KMP targets.

## Acceptance (short)

- [x] P0: no WebRTC/QR-share/viewer/`p2pSessionActive` on Flutter `main`  
- [x] Shared tests for FHIR mock + jargon glossary (P2); BSN redaction / vault encrypt still later  
- [ ] Android + iOS: unlock → timeline → decoder → appointments  
- [x] Stack direction: Koin + StateFlow; SQLDelight stubs; IDs `net.moonbaseone.vera`  
- [x] No desktop/web KMP modules in v1  
- [ ] App i18n EN/NL/DE/FR  
- [ ] CI green: shared tests + Android build (+ iOS when runner available)

## How to run (skeleton)

```bash
./gradlew :shared:testDebugUnitTest
./gradlew :androidApp:assembleDebug
open iosApp/Vera.xcodeproj   # macOS + Xcode
```
