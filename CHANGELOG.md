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
- Demo symptom samples seeded for emulator (STT often unavailable); visible in symptoms list + appointment notes
- Tap a symptom (list or appointment notes) to open a dialog with the full text
- Symptoms store and show local date/time in the list, appointment notes, and detail dialog
- Appointments list redesign (Volgende / Straks) and full-screen pocket guide per appointment (keep screen on, sections, local recording)
- Link care-plan / decoder action points into a pocket guide with persistent “In gids” state
- Inline glossary highlights with tap-to-explain sheet
- MedMij research notes: `docs/integrations/medmij.md`

### Changed

- Soft teal + lavender visual system (mock-inspired): gradient wash, rounded elevated cards, circular icon badges; primary teal `#006054`, secondary purple for nav/chips
- Graceful mint→aqua→lavender hero wash behind app header + timeline (soft orbs, fades to surface under detail cards)
- Unified app header (actions + title + subtitle) on the same soft wash as page content; timeline chips show short care-step names (e.g. Chirurgie / Radiotherapie), status pill (Afgerond/Huidig/Gepland) on the phase detail card, purple mode pills, appointment hero without overlapping badge
- Care-plan timeline pills are taller with a phase-status icon (done / current / planned) and a compact date under the step name — no per-treatment-type icons
- Timeline pills: balanced capsule padding; current step uses a radio/“here” icon instead of play
- Care-plan chip short titles are derived from activity titles when a dataset omits `shortTitle` (e.g. Chemotherapie / Chirurgie / HER2-therapie)
- App icon: soft mint→lavender compass mark aligned with the new UI wash (organic soft ring, not a hard geometric circle)
- Care-plan steps use a swipeable carousel synced with the timeline navigator; vertical scroll stays inside the detail card
- Compact next-appointment hint on the current care-plan step (mint strip under body copy; opens Afspraken)
- Woordenboek + Contact restyled to the same soft card / icon-circle language
- Contact “Bellen” is an outlined button instead of a solid teal CTA
- Care-plan jargon: tap underlined terms for uitleg (chip strip + helper copy removed); actiepunten only from the care step
- Trimmed shell/screen helper subtitles, care-plan legend / IKNL chip row, and compact top app bar
- Symptoms list is a closable overlay (✕ / system back); bottom tabs stay on the main IA
- Product UI decision: **shared Compose** — no separate iOS SwiftUI; CMP iOS host is next
- Demo dataset chips removed from the care-plan screen (debug long-press only; not in release)
- “Pocket gids” renamed to **afspraak notities** (EN: appointment notes)
- “Toevoegen aan afspraak notities” is a compact text link instead of a large button
- Symptom recording offers **Stop en sla op** / **Stop en gooi weg** while listening
- Appointments list opens notes on card tap (no notes CTA / status chip); play icon only when a recording exists
- Appointment recording uses an on-page text button (Start opname / Stop en sla op); playback scrubber stays below when a recording exists
- Appointment recording stop defaults to **Stop en sla op**; playback has scrubber, progress, ±10s, stop, and delete-with-confirm
- Manual questions on appointment notes (type + add; suggested questions can be added; delete per question); hospital memo labeled as care-team prep
- Appointment notes: “Vraag toevoegen” sits under suggested questions, same TextButton style as “+ …” suggestions
- Appointment notes: symptoms, my questions, and suggested questions sections are collapsible
- Patient-facing “symptomen” renamed to **Hoe het gaat** (EN: How I’m doing) — everyday wording for spoken wellbeing notes
- How I’m doing: type an entry as well as speak it (text field + add on the screen and record sheet)
- App language picker (NL/EN/DE/FR) on Contact; choice persists and refreshes UI copy
- How I’m doing header action uses a heart outline (FavoriteBorder) instead of a list/smiley icon
- Woordenboek: determinate progress bar while translating glossary entries; partial results appear as they complete
- Care-plan / appointments / contacts / notes content localizes with app language (Dutch source → on-device translation)
- Woordenboek follows app language (separate doeltaal picker removed; compact privacy note kept)
- Care-plan subtitle uses the compass tagline (“Jouw persoonlijke oncologie kompas”)
- Appointment notes: empty state for My questions when none are added yet
- Appointments navigator: “Straks” renamed to “Later”
- Stable Obtainium signing via committed `ci/obtainium.keystore` (no more per-run debug fingerprints that block updates)
- Care-plan Begrijpelijk / Dokterstekst is a single segmented toggle
- Care-plan mode toggle labels: **Eenvoudig** / **Origineel** (was Begrijpelijk / Dokterstekst)
- Language switch shows a global progress dialog while on-device content translation runs (woordenboek inline progress removed)
- README compacted; Obtainium signing and current product IA reflected
- On-device translation: download NL→target model pair (fixes FR hang), serialize downloads, timeout + progress pulse

### Fixed

- App icon compass optically recentered in master + launcher assets
- Appointments (and related) double header: title only in the top app bar
- Next-appointment calendar badge no longer overflows the card
- Symptomen inspreken no longer crashes when the speech service fails or the sheet is dismissed (main-thread recognizer + soft-fail)
- IKNL guideline buttons now open the official Richtlijnendatabase URLs (care plan, decoder, jargon)

### Removed

- IKNL Richtlijn link and “Gebaseerd op de Richtlijnendatabase…” footer from appointment notes
- Green privacy sentinel dot from the top app bar
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
