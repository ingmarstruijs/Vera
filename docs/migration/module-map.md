# Shared module map (P11)

Vera still ships as one Gradle KMP module (`:shared`) plus thin app shells. P11 introduces **feature boundaries** inside `shared` so a later Gradle extract stays mechanical.

## Current packages → intended modules

| Package / concern | Intended Gradle module | Status |
|-------------------|------------------------|--------|
| `vault`, `auth` | `:vault` | In-tree; Koin `vaultModule()` |
| `shield`, `translation` (engine wiring) | `:shield` | In-tree; Koin `shieldModule()` |
| `decoder` | `:decoder` | In-tree; Koin `decoderModule()` |
| `appointments`, `speech` | `:appointments` | In-tree; Koin `appointmentsModule()` |
| `careplan`, `blueprint`, `fhir` mock | `:careplan` (+ future `:fhir-client`) | In-tree; Koin `carePlanModule()`; `FhirClient` stub only |
| `domain`, `failure`, `i18n`, `navigation`, `ui` tokens | `:core` / stay in `:shared` | In-tree |
| `androidApp` / `iosApp` | thin shells | Native UI only |

## Navigation

Typed destinations live in `navigation.AppDestination`. Platforms wire Compose Navigation / SwiftUI tabs. Decompose or Circuit can replace the thin `AppNavigator` interface without changing destination IDs.

## Repository boundaries

- **Local:** `CarePlanRepository` → `LocalCarePlanRepository(MockDataService)`
- **Future production:** `FhirClient` (MedMij / hospital EPD) — **not** implemented; `UnsupportedFhirClient` fails closed

## CMP product gate

Compose Multiplatform shared UI is **not** adopted in this slice. Dual Jetpack Compose + SwiftUI remains until an explicit product decision. See [kmp-plan.md](kmp-plan.md) Phase 6 / open decisions.
