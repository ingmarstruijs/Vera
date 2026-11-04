# Vera documentation

English documentation for the Kotlin Multiplatform app (Android + iOS).

| Path | Description |
|------|-------------|
| [setup/android.md](setup/android.md) | Android SDK, Gradle, assemble / install |
| [setup/ios.md](setup/ios.md) | Xcode project, Shared framework link |
| [testing.md](testing.md) | Shared unit tests and CI smoke |
| [migration/kmp-plan.md](migration/kmp-plan.md) | Migration plan mirror (P0–P11 / Phase 6) |

## Modules

- `shared/` — Kotlin Multiplatform business logic (domain, vault, shield, FHIR mock, ViewModels)
- `androidApp/` — Jetpack Compose UI (`net.moonbaseone.vera`)
- `iosApp/` — SwiftUI UI

Webviewer / P2P sharing and Flutter hosts are **removed**. No desktop or web KMP modules in v1.
