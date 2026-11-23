# iOS host (Compose Multiplatform — pending)

Vera no longer ships a **separate SwiftUI UI**. Product UI lives in the shared Compose module `:ui`.

## Current state

| Piece | Status |
|-------|--------|
| SwiftUI screens / Xcode app | **Removed** |
| Shared Kotlin (`:core` / `:shared`) | Builds iOS frameworks |
| Shared Compose UI (`:ui`) | Android library today; CMP iOS target next |
| iOS app shell | Not wired yet (`ComposeUIViewController` host) |

## Next (CMP iOS)

1. Convert `:ui` to Compose Multiplatform (`commonMain` + `iosMain`).
2. Add `MainViewController()` exporting `VeraApp()`.
3. Thin Xcode (or Amper/KMP) host that embeds that controller.
4. Extend CI with a macOS job when the host exists.

Until then, day-to-day installs are **Android** via Obtainium — see [docs/RELEASING.md](../docs/RELEASING.md).

Framework link for shared logic (unchanged):

```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```
