# Vera — product ideas backlog

Captured 2026-11-04 (Europe/Amsterdam). Raw product notes from Ingmar; prioritized below for a first slice.

## First step (in progress)

**Readable + editable pocket guide** on the existing Appointment Navigator path.

Why this first:
- Highest UX gap vs code that already exists (`generatePocketGuide`, STT/TTS, appointments UI)
- Local-only — no new permissions, calendar APIs, or cloud STT decisions
- Unlocks later work (action items → guide, appointment prep notes) without locking product model answers

Scope of the slice:
- Structured guide model (sections, not raw markdown in the UI)
- Editable extra notes for the appointment
- Full-page / full-screen view
- Plain-text export for TTS (no `#` / `**` / `##` in what the user sees or hears)

## Priority order (proposed)

1. **Pocket guide polish** — readable, editable, full-page *(this branch)*
2. **Spoken symptoms finish** — delete one note, re-read as text, re-record/replace (not only clear-all)
3. **Care-plan chrome** — subtler IKNL, morphing title into app header, less “go to Decoder” friction
4. **Contacts section** — practitioners / institutes / department phones (“who do I call?”)
5. **Calendar sync** — start one-way Vera → device calendar after privacy copy is clear
6. **Appointment session audio** — record → transcript → simplify; needs on-device vs cloud call first

## Appointments

- Record audio of appointments
- Recording → transcript (speech-to-text)
- Transcript into plain language (translate / simplify)
- Sync appointments to the system calendar

## Pocket guide

- [x] Full-page view *(first step)*
- [x] Editable / expandable (e.g. jot extra notes for an appointment) *(first step)*
- [x] Format: source may stay structured internally, but the user sees a readable/normalized format (no markdown syntax) *(first step)*

## Care plan UX / content

- Text without needing the decoder — remove (or revisit: less “go to decoder” friction)
- Source **IKNL** too prominent / takes too much space — shrink or make subtler
- Title **Your care plan**, subtitle, and source attribution merge with the app header that morphs on scroll (large title/subtitle/source collapse into a compact header)
- Care plan contains many “think about this / think about that” action items. Open question:
  - Do those also appear in the pocket guide?
  - Or should you separately book an appointment for them (e.g. by phone)?
  - What exactly are the action items for (reminder vs. action vs. checklist)?
- Names of practitioners, institutes, and department phone numbers — separate section (contacts / “who do I call?”)

## Spoken symptoms

- Can delete
- Can re-read as text
- Can re-record

## Open product questions

1. Relationship **care-plan action items ↔ pocket guide ↔ appointments** (one model or separate flows?)
2. Calendar sync: one-way Vera → device calendar, or bidirectional?
3. Appointment audio: local / on-device only, or cloud STT + simplify?
