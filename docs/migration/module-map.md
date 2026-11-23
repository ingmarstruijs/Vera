# Shared module map

Vera ships Gradle modules `:core` + `:shared` + **`:ui`** (shared Compose) plus a thin `androidApp` shell. SwiftUI was removed; iOS will host the same Compose UI via CMP.

## Current packages → modules

| Package / concern | Gradle module | Status |
|-------------------|---------------|--------|
| `domain`, `failure`, `i18n`, `navigation`, `ui` tokens, `AppConstants` | **`:core`** | Extracted |
| `vault`, `auth`, `shield`, `decoder`, `appointments`, `speech`, `careplan`, `blueprint`, `fhir` mock | **`:shared`** | In-tree; Koin feature modules |
| Compose screens + theme (`net.moonbaseone.vera.ui.*`) | **`:ui`** | Shared Compose UI (Android library; CMP next) |
| Application / Activity | **`:androidApp`** | Thin host |
| iOS app | `iosApp/` placeholder | CMP host TBD — no SwiftUI |

`:shared` depends on `:core` via `api` and **exports** `:core` into the iOS `Shared` umbrella framework.

## Navigation

Typed destinations live in `navigation.AppDestination` (`:core`). `DefaultAppNavigator` provides StateFlow observation. Decompose/Circuit deferred — see [navigation.md](navigation.md).

## Repository boundaries

- **Local:** `CarePlanRepository` → `LocalCarePlanRepository(MockDataService)`
- **Future production:** `FhirClient` (MedMij / hospital EPD) — **not** implemented; `UnsupportedFhirClient` fails closed

## UI decision (shared Compose)

**Adopted:** one Compose UI in `:ui`. No separate iOS SwiftUI. Next step: Compose Multiplatform `iosMain` + thin host (`iosApp/README.md`).
