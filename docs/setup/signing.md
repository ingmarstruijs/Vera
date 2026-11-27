# Signing (Android / iOS)

## CI policy

| Workflow | What it builds | Signing |
| --- | --- | --- |
| **KMP smoke** | `:ui` + `:androidApp` `assembleDebug` + tests / detekt / lint | Debug (always) |
| **Build & Sign Android** | `assembleRelease` → GitHub Release APK | See precedence below |

### Release signing precedence

1. **Project key** via `ANDROID_KEYSTORE_*` secrets / local `keystore.properties` (preferred for production)
2. **Committed Obtainium preview key** [`ci/obtainium.keystore`](../../ci/README.md) — stable fingerprint so Obtainium can update `0.1.<N>` builds
3. Debug — **local only**; CI refuses to publish without (1) or (2)

Ephemeral debug signing on GitHub runners creates a **new certificate every run**, so Obtainium cannot update in place. That is why the preview keystore is committed.

Do **not** block smoke CI on missing project secrets. Do **not** use the Obtainium preview key for Play Store / patient-facing production.

## Android (CI / Obtainium)

### Preview (default)

Uses `ci/obtainium.keystore` when project secrets are absent. Details: [ci/README.md](../../ci/README.md).

### Project / production key (optional secrets)

Set these repository secrets once a project keystore exists:

| Secret | Purpose |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded `.jks` / `.keystore` |
| `ANDROID_KEYSTORE_PASSWORD` | Store password |
| `ANDROID_KEY_ALIAS` | Key alias |
| `ANDROID_KEY_PASSWORD` | Key password (CI falls back to store password if empty) |

The release workflow decodes the keystore to `keystore.jks` and writes `keystore.properties` on the runner. Those paths are gitignored.

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

## iOS (future CMP host)

When the Compose Multiplatform iOS host exists, configure Team ID + signing in Xcode for `net.moonbaseone.vera`. Shared framework steps: [ios.md](ios.md).

iOS is **not** part of the GitHub Releases / Obtainium channel yet.
