# Typed navigation

## Current contract

| Piece | Location | Role |
|-------|----------|------|
| `AppDestination` | `:core` | Sealed route IDs: unlock → careplan → decoder → appointments → contacts |
| `AppNavigator` | `:core` | Thin navigate / current API |
| `DefaultAppNavigator` | `:core` | `StateFlow`-backed implementation (Koin `coreModule`) |
| Shared UI | `:ui` `AppShell` + Navigation Compose | Maps destinations to tabs |

## Decompose / Circuit — deferred

**Decision:** do **not** adopt Decompose or Circuit yet.

**Why**

- Shared Compose `:ui` already owns the tab shell.
- Product IA is a flat unlock → tab shell; no shared multi-stack or deep-link graph yet.
- Prefs keep DI/state as **Koin + ViewModel/StateFlow**.

**Concrete next step (when needed)**

1. Keep `AppDestination.route` IDs stable.
2. Introduce a Decompose `RootComponent` (or Circuit `Presenter`) that owns navigator-equivalent state and child slots.
3. Host from `:ui` (and the future CMP iOS entry) without reintroducing a separate SwiftUI tree.
