# iOS setup

## Prerequisites

- macOS with **Xcode 15+**
- Apple development team for device / TestFlight (simulator works without distribution signing)

## Project

| Item | Value |
|------|--------|
| Xcode project | `iosApp/Vera.xcodeproj` |
| Bundle id (target) | `net.moonbaseone.vera` (align with product domain) |
| UI | SwiftUI |
| Shared logic | Kotlin `:shared` framework |

Info.plist includes Face ID / biometry usage strings. Speech (SFSpeech / AVSpeech) is used by the appointments navigator.

## Shared framework

Until SPM / XCFramework packaging is fully automated, build the framework on a Mac:

```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

Framework output (typical):

`shared/build/bin/iosSimulatorArm64/debugFramework/Shared.framework`

Add / embed it in the Xcode target as needed. See [iosApp/README.md](../../iosApp/README.md).

## Open & run

```bash
open iosApp/Vera.xcodeproj
```

Select the **Vera** scheme and an iOS Simulator, then Build & Run.

## Fonts

Atkinson Hyperlegible OTFs are under `assets/fonts/`. Add them to the Xcode target when productizing typography.
