# Changelog

Notable changes to the Vera repository. Dates are UTC.

## Unreleased

### Removed (P10 / VER-14)

- Flutter application tree: `lib/`, Flutter `test/`, `pubspec.yaml`, `analysis_options.yaml`, Flutter hosts `android/`, `ios/`, `web/`, and `.metadata`
- Legacy Flutter docs: root `PLATFORM_SETUP.md`, `TEST_GUIDE.md` (replaced by `docs/setup/*` and `docs/testing.md`)

### Documentation (P10b)

- Root README rewritten for Kotlin Multiplatform (Android + iOS)
- English setup and testing guides under `docs/`
- Migration plan acceptance updated: Flutter decommission complete

### Previously removed (P0)

- Webviewer / WhatsApp Web-style P2P sharing (WebRTC, QR share/viewer, PrivacySentinel P2P status)
- Signalling host `signal.vera-app.nl` — decommission outside this repo (DNS / infra)

## KMP migration highlights

- **P1** — Gradle monorepo: `shared`, `androidApp`, `iosApp`, smoke CI
- **P2–P4** — Domain, SQLDelight vault crypto, Keystore/Keychain + biometrics
- **P5–P6** — Shell, auth gate, blueprint, decoder, appointments (+ speech)
- **P7** — On-device translation + NER (ML Kit Android; NLTagger / Translate stub iOS)
