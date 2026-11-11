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

## Roadmap — medische data & FHIR-toegang

FHIR is een **open formaat**, geen vrijbrief voor EPD-toegang. Vera blijft local-first tot onderstaande stappen klaar zijn. Niet elke app mag zomaar FHIR-koppelen met zorgsystemen.

### Fase 0 — nu (local-first)
- [x] Mock / lokale FHIR-achtige payloads (geen live EPD)
- [x] Privacy-first copy: data blijft op het apparaat waar mogelijk
- [ ] Productcopy die uitlegt: geen live dossierkoppeling totdat MedMij/autorisatie rond is

### Fase 1 — toestemming & lokale bronnen
- [ ] Expliciete patiënttoestemming-UX vóór elke import/koppeling (per bron, per gegevenstype)
- [ ] Dataminimalisatie: alleen bouwstenen die Vera echt nodig heeft (bijv. CarePlan, Medication, lab)
- [ ] Optioneel: **HealthKit** / **Health Connect** als lokale FHIR-achtige bron (OS-toestemming per type)
- [ ] Duidelijke revoke/uitleg: wat Vera wel/niet leest van lokale health stores

### Fase 2 — autorisatie-techniek (SMART on FHIR)
- [ ] App-registratie bij IdP / authorization server
- [ ] **OAuth 2.0** + **SMART on FHIR** launch/scopes (geen brede EPD-toegang)
- [ ] Token-opslag encrypted on-device; refresh/revoke flows
- [ ] Scope-matrix per feature (care plan vs medicatie vs afspraken)

### Fase 3 — Nederland: MedMij & DigiD
- [ ] Aansluiting **MedMij**-afsprakenstelsel (juridisch + technisch)
- [ ] MedMij-label / keuringstraject
- [ ] Inloggen met **DigiD** (of opvolger) en per-sessie toestemming welke gegevens Vera mag inzien
- [ ] Koppeling met huisarts/ziekenhuis alleen via goedgekeurde MedMij-routes

### Fase 4 — compliance & beveiliging
- [ ] AVG / bijzondere persoonsgegevens: DPIA, verwerkersafspraken waar nodig
- [ ] Zorg-IB: **NEN 7510** (en waar relevant ISO 27001) als ontwerpnorm
- [ ] Logging/audit van toestemming en datatoegang (lokaal + server waar van toepassing)
- [ ] Pen-test / security review vóór productie-koppeling

### Guardrails (niet doen)
- Geen willekeurige of “brede” EPD-API-calls zonder registratie, keuring en patiënttoestemming
- Geen cloud-sync van medische dossiers als default; local-first blijft uitgangspunt
- Geen feature die MedMij/SMART vereist shippen alsof die al live is (mock duidelijk labelen)

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
4. Eerste echte datakoppeling: **HealthKit/Health Connect** (lokaal) of direct **MedMij**?
5. Welke FHIR/MedMij-bouwstenen zijn MVP (CarePlan alleen vs + medicatie/lab/afspraken)?
