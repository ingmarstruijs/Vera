# Signing (Android / iOS)

Release signing secrets are **not** required for CI and are **not** stored in this repository.

## CI policy

- GitHub Actions builds **unsigned** Android debug (`assembleDebug`) plus shared/core unit tests, detekt, and lint.
- Play Store / App Store signing keys, keystores, and provisioning profiles stay with the product owner (local or secret store).
- Do **not** block migration CI on missing signing secrets.

## Android (local release)

1. Create a release keystore outside the repo.
2. Point `androidApp` `signingConfigs` at local properties / env vars (`VERA_STORE_FILE`, `VERA_STORE_PASSWORD`, `VERA_KEY_ALIAS`, `VERA_KEY_PASSWORD`).
3. Wire `buildTypes.release.signingConfig` only when those values are present.

## iOS (local release)

1. Configure Team ID + signing in Xcode for `net.moonbaseone.vera`.
2. Use automatic signing for development; App Store profiles for distribution.
3. Shared XCFramework link steps: see [ios.md](ios.md).

## Next CI step (optional)

A future job may assemble `assembleRelease` with GitHub Actions secrets when keys exist — keep it optional and skip cleanly when secrets are absent.
