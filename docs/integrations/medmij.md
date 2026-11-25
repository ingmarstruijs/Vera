# MedMij — research notes

Vera does **not** ship a live MedMij connection. Clinical data today comes from local mock FHIR R4 fixtures (`MockDataService`) behind `CarePlanRepository` / `FhirClient`.

## What MedMij is

MedMij is the Dutch agreements framework for exchanging health data between care providers (via a **Dienstverlener aanbieder**, DVA) and personal health environments / PGOs (**Dienstverlener persoon**, DVP). Data services use **HL7 FHIR** (Nictiz profiles). Patient authorization typically involves DigiD / OAuth 2.0 at the DVA.

## If Vera were to connect later

Likely roles to evaluate:

1. **PGO / DVP-style client** — pull care-plan, appointments, and related resources into the local vault after patient consent.
2. **Aanbiedermodule** — launch from a PGO with SMART-on-FHIR launch codes (see MedMij change-management docs).

Both paths require:

- MedMij participant agreement and ongoing audits
- **NEN 7510** (and related logging / NEN 7513 expectations)
- Choosing which gegevensdienst to implement first (e.g. BGZ, medication, oncology-related resources)
- Hardening the existing `FhirClient` boundary (`UnsupportedFhirClient` fails closed today)

## Code hooks already in-repo

| Piece | Role |
|-------|------|
| `FhirClient` / `UnsupportedFhirClient` | Future transport contract |
| `CarePlanRepository` | ViewModel-facing read API |
| `MockDataService` | Local demo datasets (switchable) |

## Sources

- [MedMij — voorbereiden implementatie](https://changemanagement.medmij.nl/alpha-of-beta/actueel/voorbereiden-implementatie)
- Richtlijnendatabase / IKNL-supported guidelines (clinical content source of truth for Vera UX)
