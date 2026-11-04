# Vera documentation

English documentation for the Kotlin Multiplatform app (Android + iOS).

| Path | Description |
|------|-------------|
| [setup/android.md](setup/android.md) | Android SDK, Gradle, assemble / install |
| [setup/ios.md](setup/ios.md) | Xcode project, Shared framework link |
| [setup/signing.md](setup/signing.md) | Release signing (out of CI; no secrets in repo) |
| [testing.md](testing.md) | Unit tests, detekt, lint, CI smoke |
| [contributing.md](contributing.md) | Branching, CI triggers, changelog bot, protect `main` |
| [product-ideas-backlog.md](product-ideas-backlog.md) | Raw product ideas backlog (unprioritized) |
| [migration/kmp-plan.md](migration/kmp-plan.md) | Migration plan mirror (P0–P11 / Phase 6) |
| [migration/module-map.md](migration/module-map.md) | `:core` / `:shared` boundaries + CMP gate note |
| [migration/navigation.md](migration/navigation.md) | Typed nav + Decompose/Circuit deferral |

## Modules

- `core/` — Stable KMP types: domain, failure, i18n, navigation, design tokens
- `shared/` — Feature KMP logic (vault, shield, FHIR mock, ViewModels); depends on `:core`
- `androidApp/` — Jetpack Compose UI (`net.moonbaseone.vera`)
- `iosApp/` — SwiftUI UI

Webviewer / P2P sharing and Flutter hosts are **removed**. No desktop or web KMP modules in v1.  
**UI gate:** dual-native continues until an explicit CMP product decision (see module-map).
