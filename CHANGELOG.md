# Changelog

Notable changes to the Vera repository. Dates are UTC.

## Unreleased

### Added

- Shared Compose UI module `:ui` (single UI codebase); thin `androidApp` host
- Solo-style Android release channel: every `main` push publishes Obtainium preview `0.1.<N>` via **Build & Sign Android**; `v*` tags on `main` publish stable/milestone Releases (`docs/RELEASING.md`)
- `versioning.gradle.kts` + optional `ANDROID_KEYSTORE_*` signing for `:androidApp:assembleRelease` (debug fallback when secrets absent)
- KMP smoke concurrency (cancel superseded runs on the same ref); smoke builds `:ui` + `:androidApp`
- Cursor agent rule `.cursor/rules/changelog.mdc` — agents update `CHANGELOG.md` in the same PR
- Demo dataset switcher for debug builds only (long-press care-plan title): HER2+ mamma, colon stadium III, lung early start; appointments reload with the same selection
- Clear IKNL source note on the care plan: Vera does not invent clinical content; citations open Richtlijnendatabase
- Header mic + symptoms list; spoken symptoms store with TTS replay
- Appointments list redesign (Volgende / Straks) and full-screen pocket guide per appointment (keep screen on, sections, local recording)
- Link care-plan / decoder action points into a pocket guide with persistent “In gids” state
- Inline glossary highlights with tap-to-explain sheet
- MedMij research notes: `docs/integrations/medmij.md`

### Changed

- App icon: clinical compass (proposal A) replaces shield+V; master + adaptive/mipmaps match the approved artwork (not a simplified vector)
- Product UI decision: **shared Compose** — no separate iOS SwiftUI; CMP iOS host is next
- Root README compacted (release table inline; screenshots removed)
- Care-plan timeline nodes reduced to 108×72.dp
- Demo dataset chips removed from the care-plan screen (debug long-press only; not in release)
- Care-plan jargon uitleg shows explanation only; jargon actiepunten live under Actiepunten with pocket-guide link
- “Toevoegen aan pocket gids” is a compact text link instead of a large button

### Fixed

- App icon mark optically centered in the circular launcher mask (was sitting high)
- IKNL guideline buttons now open the official Richtlijnendatabase URLs (care plan, decoder, jargon)

### Removed

- SwiftUI `iosApp` screens / Xcode project (placeholder README for CMP host)
- `docs/screenshots/` promo images
- GitHub Actions changelog-on-merge bot (follow-up `chore/changelog-record-*` PRs); agents own the changelog instead
- Migration docs (`docs/migration/`), product-ideas backlog, and CMP/P11 gate text from README
### Merged

- [#32](https://github.com/ingmarstruijs/Vera/pull/32) — chore: replace changelog bot with agent rule (2026-11-23)
- [#29](https://github.com/ingmarstruijs/Vera/pull/29) — feat: Solo-style Obtainium Android release channel (2026-11-23)
- [#28](https://github.com/ingmarstruijs/Vera/pull/28) — Readable editable appointment pocket guide (2026-11-23)
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
- `DefaultAppNavigator` (StateFlow); Decompose/Circuit deferred
- CI: detekt + `:androidApp:lintDebug` (a11y-minded); signing kept out of CI — [docs/setup/signing.md](docs/setup/signing.md)
- Dual-UI polish: Android legend/headings use DesignTokens; iOS `ClinicalColors` parity

### Changed (P11 / Phase 6 first slice)

- Documented **CMP vs dual-native product gate** (stay dual-native until explicit decision)
- Koin split into feature modules; `CarePlanRepository` vs `FhirClient` boundary prep
- Typed shell destinations (`AppDestination`); IA unlock → care plan → decoder/appointments
- EN/NL/DE/FR `StringCatalog` (+ Android `values-*`); calmer clinical DesignTokens / theme
- Accessibility defaults (content descriptions, iOS Dynamic Type caps); removed unused placeholder screen
- Module map documented for `:core` / `:shared` boundaries

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
