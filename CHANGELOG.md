# Changelog

Notable changes to the Vera repository. Dates are UTC.

## Unreleased

### Merged

- [#26](https://github.com/ingmarstruijs/Vera/pull/26) — Add English product ideas backlog doc (2026-11-04)
- [#24](https://github.com/ingmarstruijs/Vera/pull/24) — Update README.md (2026-11-04)
- [#21](https://github.com/ingmarstruijs/Vera/pull/21) — docs: Actions must be allowed to create PRs for changelog bot (2026-11-04)
- [#19](https://github.com/ingmarstruijs/Vera/pull/19) — fix(ci): changelog bot opens PR instead of pushing main (2026-11-04)
- [#18](https://github.com/ingmarstruijs/Vera/pull/18) — chore(VER-20): CI triggers, main protection docs, changelog on merge (2026-11-04)
- [#17](https://github.com/ingmarstruijs/Vera/pull/17) — feat: richer care-plan mocks + inline decode (VER-19) (2026-11-04)

### Added

- CI/repo hygiene: KMP smoke only on `pull_request`/`push` → `main` (no `cursor/**` push double-runs); changelog-on-merge workflow; contributing guide with `main` branch-protection clicks
- Vera app icon: shield + stylized V on DesignTokens sage (`#2F5D50`); master `assets/icons/vera-app-icon-1024.png`, Android adaptive `@mipmap/ic_launcher`, iOS `AppIcon` asset catalog

### Changed (P11 / Phase 6 leftover hardening)

- Gradle `:core` extract (domain, failure, i18n, navigation, design tokens); `:shared` exports `:core` for iOS
- `DefaultAppNavigator` (StateFlow); Decompose/Circuit deferred — [docs/migration/navigation.md](docs/migration/navigation.md)
- CI: detekt + `:androidApp:lintDebug` (a11y-minded); signing kept out of CI — [docs/setup/signing.md](docs/setup/signing.md)
- Dual-UI polish: Android legend/headings use DesignTokens; iOS `ClinicalColors` parity

### Changed (P11 / Phase 6 first slice)

- Documented **CMP vs dual-native product gate** (stay dual-native until explicit decision)
- Koin split into feature modules; `CarePlanRepository` vs `FhirClient` boundary prep
- Typed shell destinations (`AppDestination`); IA unlock → care plan → decoder/appointments
- EN/NL/DE/FR `StringCatalog` (+ Android `values-*`); calmer clinical DesignTokens / theme
- Accessibility defaults (content descriptions, iOS Dynamic Type caps); removed unused placeholder screen
- Module map: [docs/migration/module-map.md](docs/migration/module-map.md)

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
