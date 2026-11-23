# Vera documentation

| Path | Description |
|------|-------------|
| [RELEASING.md](RELEASING.md) | Obtainium preview + `v*` GitHub Releases |
| [setup/android.md](setup/android.md) | Android SDK, Gradle, assemble / install |
| [setup/ios.md](setup/ios.md) | iOS via future CMP host (SwiftUI removed) |
| [setup/signing.md](setup/signing.md) | Keystore / signing secrets |
| [testing.md](testing.md) | Unit tests, detekt, lint, CI smoke |
| [contributing.md](contributing.md) | Branching, CI, changelog agent rule, protect `main` |
| [product-ideas-backlog.md](product-ideas-backlog.md) | Raw product ideas backlog |
| [migration/kmp-plan.md](migration/kmp-plan.md) | Migration plan mirror |
| [migration/module-map.md](migration/module-map.md) | `:core` / `:shared` / `:ui` map |
| [migration/navigation.md](migration/navigation.md) | Typed nav notes |

## Modules

- `core/` — KMP: domain, failure, i18n, navigation, design tokens  
- `shared/` — KMP features (vault, shield, FHIR mock, ViewModels)  
- `ui/` — **Shared Compose UI** (Android library now; CMP iOS next)  
- `androidApp/` — Thin Android application shell  
- `iosApp/` — Placeholder for CMP iOS host (no SwiftUI)
