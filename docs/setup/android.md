# Android setup

## Prerequisites

- JDK **21** (Temurin recommended; matches CI)
- Android SDK with:
  - `platforms;android-35`
  - `build-tools;35.0.0`
  - `platform-tools`
- Optional: Android Studio Ladybug+ for emulator / Logcat

Point Gradle at the SDK via `local.properties` (gitignored):

```properties
sdk.dir=/path/to/Android/Sdk
```

## Configuration

| Item | Value |
|------|--------|
| App shell | `androidApp/` |
| Shared UI | `ui/` (Compose Material 3) |
| applicationId | `net.moonbaseone.vera` |
| minSdk | 26 |
| compileSdk / targetSdk | 35 |

Permissions (see `androidApp/src/main/AndroidManifest.xml`): biometric unlock, mic/speech for appointments.

Logic: `:shared` / `:core`. Screens: `:ui`.

## Build

```bash
./gradlew :shared:testDebugUnitTest
./gradlew :ui:assembleDebug :androidApp:assembleDebug
./gradlew :androidApp:installDebug
```

Release / Obtainium channel: [RELEASING.md](../RELEASING.md). Local or CI project signing: [signing.md](signing.md).

## Fonts

Atkinson Hyperlegible OTFs are under `assets/fonts/`. Wire them into Compose typography when productizing UI (Phase 4 leftover / Phase 6 polish).
