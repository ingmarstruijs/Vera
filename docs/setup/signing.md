# Signing (Android / iOS)

Release signing material is **not** stored in this repository. GitHub Actions may use **optional** secrets for Android Obtainium builds; see [RELEASING.md](../RELEASING.md).

## CI policy

| Workflow | What it builds | Signing |
| --- | --- | --- |
| **KMP smoke** | `assembleDebug` + tests / detekt / lint | Debug (always) |
| **Build & Sign Android** | `assembleRelease` → GitHub Release APK | Project keystore if `ANDROID_KEYSTORE_*` secrets exist; otherwise **debug** signing |

Do **not** block smoke CI on missing signing secrets. Do **not** share debug-signed APKs as patient-facing builds.

## Android (CI / Obtainium)

Set these repository secrets once a project keystore exists:

| Secret | Purpose |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded `.jks` / `.keystore` |
| `ANDROID_KEYSTORE_PASSWORD` | Store password |
| `ANDROID_KEY_ALIAS` | Key alias |
| `ANDROID_KEY_PASSWORD` | Key password (CI falls back to store password if empty) |

The release workflow decodes the keystore to `keystore.jks` and writes `keystore.properties` on the runner. Both paths are gitignored.

## Android (local release)

1. Create a release keystore outside the repo (or reuse the project key).
2. At the repo root, add gitignored files:

```properties
# keystore.properties (repo root — gitignored)
storeFile=keystore.jks
storePassword=…
keyAlias=…
keyPassword=…
```

3. Place `keystore.jks` next to that file (or set `storeFile` to a path under the repo root).
4. `androidApp` wires `buildTypes.release.signingConfig` when those values are present; otherwise release falls back to the debug keystore.

```bash
./gradlew :androidApp:assembleRelease
```

## iOS (local release)

1. Configure Team ID + signing in Xcode for `net.moonbaseone.vera`.
2. Use automatic signing for development; App Store profiles for distribution.
3. Shared XCFramework link steps: see [ios.md](ios.md).

iOS is **not** part of the GitHub Releases / Obtainium channel yet.
