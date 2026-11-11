# Vera — product ideas backlog

Captured 2026-11-04 (Europe/Amsterdam). Raw product notes from Ingmar; prioritized below.

## Shipped on this branch (PR)

1. **Pocket guide polish** — structured readable guide, editable extra notes, full-screen
2. **Spoken symptoms finish** — discrete notes with delete, re-read (TTS), re-record/replace
3. **Care-plan chrome** — tab header morphs to care-plan title + subtle IKNL; bulky source chip removed; IKNL link opens URL; less duplicate page chrome
4. **Contacts / who do I call?** — care-team phones from mock FHIR + GP / desk
5. **Readable dates** — ISO → `22 apr 2025 · 09:00` in appointments, pocket guide, blueprint

## Still open (later)

5. **Calendar sync** — start one-way Vera → device calendar after privacy copy is clear
6. **Appointment session audio** — record → transcript → simplify; needs on-device vs cloud call first
- True scroll-linked collapsing header (large → compact) beyond tab-title morph
- Care-plan action items ↔ pocket guide model decision

## Appointments

- Record audio of appointments
- Recording → transcript (speech-to-text)
- Transcript into plain language (translate / simplify)
- Sync appointments to the system calendar

## Pocket guide

- [x] Full-page view
- [x] Editable / expandable (e.g. jot extra notes for an appointment)
- [x] Format: readable/normalized (no markdown syntax in UI)

## Care plan UX / content

- [x] Text without needing the decoder — keep inline jargon chips + stronger header hint path
- [x] Source **IKNL** too prominent — shrunk into subtle app-bar subtitle
- [~] Title / subtitle / source merge into morphing header — tab-level morph done; scroll collapse later
- Care plan action items open questions remain:
  - Do those also appear in the pocket guide?
  - Or should you separately book an appointment for them (e.g. by phone)?
  - What exactly are the action items for (reminder vs. action vs. checklist)?
- [x] Names of practitioners, institutes, and department phone numbers — **Wie bel ik?** section

## Spoken symptoms

- [x] Can delete
- [x] Can re-read as text
- [x] Can re-record

## Open product questions

1. Relationship **care-plan action items ↔ pocket guide ↔ appointments** (one model or separate flows?)
2. Calendar sync: one-way Vera → device calendar, or bidirectional?
3. Appointment audio: local / on-device only, or cloud STT + simplify?
