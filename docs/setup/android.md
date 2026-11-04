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
| Module | `androidApp/` |
| applicationId / namespace | `net.moonbaseone.vera` |
| minSdk | 26 |
| compileSdk / targetSdk | 35 |
| UI | Jetpack Compose + Material 3 |

Permissions (see `androidApp/src/main/AndroidManifest.xml`): biometric unlock, and mic/speech where the appointments navigator needs them.

Shared KMP code lives in `:shared` (SQLDelight, vault, Identity Shield, ML Kit Translate / Entity Extraction on Android).

## Build

From the repo root:

```bash
./gradlew :shared:testDebugUnitTest
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:installDebug
```

Release signing (Play Store) is not wired yet — see open decisions in [migration/kmp-plan.md](../migration/kmp-plan.md).

## Fonts

Atkinson Hyperlegible OTFs are under `assets/fonts/`. Wire them into Compose typography when productizing UI (Phase 4 leftover / Phase 6 polish).
