# Vera iOS app (SwiftUI)

Empty SwiftUI shell for the Kotlin Multiplatform migration (P1).

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

Timeline · Decoder · Appointments — no Sync/Webviewer tab.
