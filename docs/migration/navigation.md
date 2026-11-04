# Typed navigation (P11 leftover)

## Current contract

| Piece | Location | Role |
|-------|----------|------|
| `AppDestination` | `:core` | Sealed route IDs: unlock → careplan → decoder → appointments |
| `AppNavigator` | `:core` | Thin navigate / current API |
| `DefaultAppNavigator` | `:core` | `StateFlow`-backed implementation (Koin `coreModule`) |
| Android | `AppShell` + Navigation Compose | Maps destinations to tabs |
| iOS | `RootTabView` + SwiftUI `TabView` | Maps same IA natively |

## Decompose / Circuit — deferred

**Decision for this slice:** do **not** adopt Decompose or Circuit yet.

**Why**

- Dual-native UI (Compose + SwiftUI) already owns the back stack / tabs.
- Product IA is a flat unlock → three-tab shell; no shared multi-stack or deep-link graph yet.
- Prefs keep DI/state as **Koin + ViewModel/StateFlow**; Decompose Components would overlap until we need cross-platform stack orchestration.

**Concrete next step (when needed)**

1. Keep `AppDestination.route` IDs stable.
2. Introduce a Decompose `RootComponent` (or Circuit `Presenter`) that owns `DefaultAppNavigator`-equivalent state and child slots for CarePlan / Decoder / Appointments.
3. Android: host via `Children` / Circuit `NavigableCircuitContent`; iOS: observe child configs in SwiftUI (or keep native tabs and only share destination state).
4. Do **not** force Compose Multiplatform UI as part of nav adoption — CMP remains a separate product gate.
