# Vera iOS app (SwiftUI)

SwiftUI host for the Kotlin Multiplatform app (Android counterpart: `androidApp/`).

## Open in Xcode

1. On a Mac with Xcode 15+:
   ```bash
   open iosApp/Vera.xcodeproj
   ```
2. Select the **Vera** scheme and an iOS Simulator.
3. Build & run.

## Shared Kotlin framework

The `:shared` KMP module produces a static `Shared` XCFramework/framework for iOS targets.

Until SPM/XCFramework packaging is wired in a later phase, you can build the framework from the repo root on a Mac:

```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

Then add the generated framework under `shared/build/bin/iosSimulatorArm64/debugFramework/Shared.framework` to the Xcode project (Embed & Sign / link as needed).

## Tabs (v1)

Timeline · Decoder · Appointments — no sync / share tab (P2P / Webviewer permanently out of scope).

## P5 shell / auth / blueprint

- `AuthGateView` + `AuthCoordinator` — biometric unlock gate (LocalAuthentication; mirrors shared `AuthViewModel`).
- `RootTabView` — shell with privacy sentinel + three tabs.
- `BlueprintView` — horizontal CarePlan timeline (mock aligned with shared `MockDataService`).

When `Shared.framework` is linked, replace `AuthCoordinator` with the Kotlin `AuthViewModel` StateFlow bridge.

## P6 decoder / appointments

- `DecoderView` — glossary search + expand (NL; non-NL notice until CocoaPods ML Kit Translate is linked).
- `AppointmentsView` + `SpeechCoordinator` — CarePlan appointments, pocket guide, SFSpeech / AVSpeech.
