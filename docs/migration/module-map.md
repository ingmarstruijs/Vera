# Shared module map (P11)

Vera ships Gradle KMP modules `:core` + `:shared` plus thin app shells. P11 leftover hardening extracted stable types into `:core`; feature packages remain in `:shared` until further extracts.

## Current packages → intended modules

| Package / concern | Gradle module | Status |
|-------------------|---------------|--------|
| `domain`, `failure`, `i18n`, `navigation`, `ui` tokens, `AppConstants` | **`:core`** | Extracted (P11 leftover) |
| `vault`, `auth` | `:vault` (future) / `:shared` today | In-tree; Koin `vaultModule()` |
| `shield`, `translation` (engine wiring) | `:shield` (future) / `:shared` | In-tree; Koin `shieldModule()` |
| `decoder` | `:decoder` (future) / `:shared` | In-tree; Koin `decoderModule()` |
| `appointments`, `speech` | `:appointments` (future) / `:shared` | In-tree; Koin `appointmentsModule()` |
| `careplan`, `blueprint`, `fhir` mock | `:careplan` (future) / `:shared` | In-tree; Koin `carePlanModule()`; `FhirClient` stub only |
| `androidApp` / `iosApp` | thin shells | Native UI only |

`:shared` depends on `:core` via `api` and **exports** `:core` into the iOS `Shared` umbrella framework so Xcode keeps a single framework link.

## Navigation

Typed destinations live in `navigation.AppDestination` (`:core`). `DefaultAppNavigator` provides StateFlow observation. Decompose/Circuit deferred — see [navigation.md](navigation.md).

## Repository boundaries

- **Local:** `CarePlanRepository` → `LocalCarePlanRepository(MockDataService)`
- **Future production:** `FhirClient` (MedMij / hospital EPD) — **not** implemented; `UnsupportedFhirClient` fails closed

## CMP product gate

Compose Multiplatform shared UI is **not** adopted. Dual Jetpack Compose + SwiftUI remains until an explicit product decision. See [kmp-plan.md](kmp-plan.md) Phase 6 / open decisions.
