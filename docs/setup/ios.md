# iOS setup

Vera **does not** ship a SwiftUI UI anymore. Product screens live in shared Compose (`:ui`). An iOS **Compose Multiplatform** host is the planned shell (not wired yet).

## Today

- Build shared Kotlin for iOS frameworks from a Mac:

```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

- There is no runnable Xcode app in-repo until the CMP host lands. See [iosApp/README.md](../../iosApp/README.md).

## Next

1. Turn `:ui` into Compose Multiplatform (`commonMain` + `iosMain`).
2. Export `MainViewController { VeraApp() }`.
3. Thin Xcode (or KMP) host embeds that controller.
4. Add a macOS CI job when the host exists.

Signing / Team ID notes for that future host: [signing.md](signing.md).

Day-to-day device testing is **Android Obtainium** — [RELEASING.md](../RELEASING.md).
