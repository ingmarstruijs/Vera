package net.moonbaseone.vera.shared.fhir

import kotlinx.coroutines.delay
import net.moonbaseone.vera.shared.failure.FhirParseFailure

/**
 * Simulates a MedMij / HL7 FHIR R4 oncology data pull from a Dutch hospital EPD.
 *
 * Dataset: 51-year-old Dutch female, HER2-positive ER-negative invasief ductaal
 * carcinoom mammae links (stadiumgroep IIA, cT2N0M0).
 *
 * @param ioDelayMs Simulated I/O latency; 0 in unit tests.
 */
class MockDataService(
    private val ioDelayMs: Long = 0L,
) {
    private companion object {
        const val IKNL_BORSTKANKER_REF = "https://www.iknl.nl/richtlijnen/borstkanker"
    }

    suspend fun fetchOncologyBundle(): OncologyBundleResult {
        if (ioDelayMs > 0) delay(ioDelayMs)
        return try {
            val resources = listOf(
                buildPatient(),
                buildCondition(),
                buildDiagnosticReport(),
            ) + buildObservations() + buildProcedures() + buildMedicationRequests() +
                listOf(buildCarePlan()) + buildAppointments()

            OncologyBundleResult(
                bundle = FhirBundle(
                    resources = resources,
                    timestamp = "2025-03-15T08:00:00+01:00",
                ),
            )
        } catch (e: Exception) {
            OncologyBundleResult(failure = FhirParseFailure(e.message ?: e.toString()))
        }
    }

    suspend fun fetchAppointments(): List<FhirResource> {
        if (ioDelayMs > 0) delay(ioDelayMs)
        return buildAppointments()
    }

    suspend fun fetchCarePlan(): FhirResource {
        if (ioDelayMs > 0) delay(ioDelayMs)
        return buildCarePlan()
    }

    private fun buildPatient(): FhirResource = FhirResource(
        resourceType = "Patient",
        id = "patient-001",
        data = mapOf(
            "resourceType" to "Patient",
            "id" to "patient-001",
            "meta" to mapOf(
                "profile" to listOf(
                    "http://nictiz.nl/fhir/StructureDefinition/nl-core-Patient",
                ),
            ),
            "text" to mapOf(
                "status" to "generated",
                "div" to """<div xmlns="http://www.w3.org/1999/xhtml">Geanonimiseerde patiënt</div>""",
            ),
            "name" to listOf(
                mapOf(
                    "use" to "official",
                    "family" to "[GEANONIMISEERD]",
                    "given" to listOf("[GEANONIMISEERD]"),
                ),
            ),
            "gender" to "female",
            "birthDate" to "[GEANONIMISEERD]",
            "address" to listOf(
                mapOf(
                    "use" to "home",
                    "city" to "Utrecht",
                    "postalCode" to "[GEANONIMISEERD]",
                    "country" to "NL",
                ),
            ),
            "generalPractitioner" to listOf(
                mapOf(
                    "reference" to "Practitioner/huisarts-001",
                    "display" to "Huisartsenpraktijk Utrecht Noord",
                ),
            ),
        ),
    )

    private fun buildCondition(): FhirResource = FhirResource(
        resourceType = "Condition",
        id = "condition-001",
        data = mapOf(
            "resourceType" to "Condition",
            "id" to "condition-001",
            "meta" to mapOf(
                "profile" to listOf("http://nictiz.nl/fhir/StructureDefinition/zib-Problem"),
            ),
            "clinicalStatus" to mapOf(
                "coding" to listOf(
                    mapOf(
                        "system" to "http://terminology.hl7.org/CodeSystem/condition-clinical",
                        "code" to "active",
                        "display" to "Actief",
                    ),
                ),
            ),
            "verificationStatus" to mapOf(
                "coding" to listOf(
                    mapOf(
                        "system" to "http://terminology.hl7.org/CodeSystem/condition-ver-status",
                        "code" to "confirmed",
                        "display" to "Bevestigd",
                    ),
                ),
            ),
            "category" to listOf(
                mapOf(
                    "coding" to listOf(
                        mapOf(
                            "system" to "http://terminology.hl7.org/CodeSystem/condition-category",
                            "code" to "encounter-diagnosis",
                            "display" to "Diagnose bij contactmoment",
                        ),
                    ),
                ),
            ),
            "severity" to mapOf(
                "coding" to listOf(
                    mapOf(
                        "system" to "http://snomed.info/sct",
                        "code" to "6736007",
                        "display" to "Matig ernstig",
                    ),
                ),
            ),
            "code" to mapOf(
                "coding" to listOf(
                    mapOf(
                        "system" to "http://hl7.org/fhir/sid/icd-10",
                        "code" to "C50.4",
                        "display" to "Maligne neoplasma van bovenste buitenste kwadrant van mamma",
                    ),
                    mapOf(
                        "system" to "http://snomed.info/sct",
                        "code" to "783541009",
                        "display" to "Invasief ductaal carcinoom van mamma (bevinding)",
                    ),
                ),
                "text" to "HER2-positief invasief ductaal carcinoom mammae links",
            ),
            "bodySite" to listOf(
                mapOf(
                    "coding" to listOf(
                        mapOf(
                            "system" to "http://snomed.info/sct",
                            "code" to "368209003",
                            "display" to "Linker mamma (lichaamsstructuur)",
                        ),
                    ),
                ),
            ),
            "subject" to mapOf("reference" to "Patient/patient-001"),
            "onsetDateTime" to "2024-11-20",
            "recordedDate" to "2024-11-28",
            "stage" to listOf(
                mapOf(
                    "summary" to mapOf(
                        "coding" to listOf(
                            mapOf(
                                "system" to "http://snomed.info/sct",
                                "code" to "1228882005",
                                "display" to "Stadium IIA (bevinding)",
                            ),
                        ),
                        "text" to "cT2N0M0 – Stadium IIA",
                    ),
                    "type" to mapOf(
                        "coding" to listOf(
                            mapOf(
                                "system" to "http://snomed.info/sct",
                                "code" to "260998006",
                                "display" to "Klinische stadiëring",
                            ),
                        ),
                    ),
                ),
            ),
            "note" to listOf(
                mapOf(
                    "text" to
                        "IKNL Richtlijn Borstkanker 2023 van toepassing. " +
                        "Tumorgrootte: 2,8 cm. Schildwachtklierprocedure uitgevoerd. " +
                        "Geen lymfekliermetastasen vastgesteld.",
                ),
            ),
        ),
    )

    private fun buildDiagnosticReport(): FhirResource = FhirResource(
        resourceType = "DiagnosticReport",
        id = "report-pa-2024-01",
        data = mapOf(
            "resourceType" to "DiagnosticReport",
            "id" to "report-pa-2024-01",
            "status" to "final",
            "category" to listOf(
                mapOf(
                    "coding" to listOf(
                        mapOf(
                            "system" to "http://terminology.hl7.org/CodeSystem/v2-0074",
                            "code" to "PAT",
                            "display" to "Pathologie",
                        ),
                    ),
                ),
            ),
            "code" to mapOf(
                "coding" to listOf(
                    mapOf(
                        "system" to "http://loinc.org",
                        "code" to "60568-3",
                        "display" to "Pathologie-rapport",
                    ),
                ),
                "text" to "Pathologisch anatomisch rapport PA-2024-01",
            ),
            "subject" to mapOf("reference" to "Patient/patient-001"),
            "effectiveDateTime" to "2024-12-05",
            "issued" to "2024-12-05T14:30:00+01:00",
            "performer" to listOf(
                mapOf(
                    "reference" to "Organization/lumc-pathologie",
                    "display" to "LUMC Afdeling Pathologie",
                ),
            ),
            "result" to listOf(
                mapOf("reference" to "Observation/obs-er"),
                mapOf("reference" to "Observation/obs-pr"),
                mapOf("reference" to "Observation/obs-her2"),
                mapOf("reference" to "Observation/obs-ki67"),
            ),
            "conclusion" to
                "HER2-positief (IHC 3+), ER-negatief, PR-negatief, Ki-67 38%. " +
                "Invasief ductaal carcinoom graad III (Bloom-Richardson).",
            "conclusionCode" to listOf(
                mapOf(
                    "coding" to listOf(
                        mapOf(
                            "system" to "http://snomed.info/sct",
                            "code" to "783541009",
                            "display" to "Invasief ductaal carcinoom van mamma",
                        ),
                    ),
                ),
            ),
        ),
    )

    private fun buildObservations(): List<FhirResource> = listOf(
        FhirResource(
            resourceType = "Observation",
            id = "obs-er",
            data = mapOf(
                "resourceType" to "Observation",
                "id" to "obs-er",
                "status" to "final",
                "category" to listOf(
                    mapOf(
                        "coding" to listOf(
                            mapOf(
                                "system" to "http://terminology.hl7.org/CodeSystem/observation-category",
                                "code" to "laboratory",
                                "display" to "Laboratorium",
                            ),
                        ),
                    ),
                ),
                "code" to mapOf(
                    "coding" to listOf(
                        mapOf(
                            "system" to "http://loinc.org",
                            "code" to "85319-2",
                            "display" to "Oestrogeenreceptor [aanwezigheid] in borst kanker specimen",
                        ),
                    ),
                    "text" to "Oestrogeenreceptor (ER)",
                ),
                "subject" to mapOf("reference" to "Patient/patient-001"),
                "effectiveDateTime" to "2024-12-05",
                "valueCodeableConcept" to mapOf(
                    "coding" to listOf(
                        mapOf(
                            "system" to "http://snomed.info/sct",
                            "code" to "260385009",
                            "display" to "Negatief",
                        ),
                    ),
                    "text" to "Negatief (0%)",
                ),
                "interpretation" to listOf(
                    mapOf(
                        "coding" to listOf(
                            mapOf(
                                "system" to "http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation",
                                "code" to "NEG",
                                "display" to "Negatief",
                            ),
                        ),
                    ),
                ),
            ),
        ),
        FhirResource(
            resourceType = "Observation",
            id = "obs-pr",
            data = mapOf(
                "resourceType" to "Observation",
                "id" to "obs-pr",
                "status" to "final",
                "code" to mapOf(
                    "coding" to listOf(
                        mapOf(
                            "system" to "http://loinc.org",
                            "code" to "85325-9",
                            "display" to "Progesteronreceptor in borst kanker specimen",
                        ),
                    ),
                    "text" to "Progesteronreceptor (PR)",
                ),
                "subject" to mapOf("reference" to "Patient/patient-001"),
                "effectiveDateTime" to "2024-12-05",
                "valueCodeableConcept" to mapOf("text" to "Negatief (0%)"),
            ),
        ),
        FhirResource(
            resourceType = "Observation",
            id = "obs-her2",
            data = mapOf(
                "resourceType" to "Observation",
                "id" to "obs-her2",
                "status" to "final",
                "code" to mapOf(
                    "coding" to listOf(
                        mapOf(
                            "system" to "http://loinc.org",
                            "code" to "18474-7",
                            "display" to "HER2 [aanwezigheid] in borst kanker specimen door immunohistochemie",
                        ),
                    ),
                    "text" to "HER2/neu (immunohistochemie)",
                ),
                "subject" to mapOf("reference" to "Patient/patient-001"),
                "effectiveDateTime" to "2024-12-05",
                "valueCodeableConcept" to mapOf(
                    "coding" to listOf(
                        mapOf(
                            "system" to "http://snomed.info/sct",
                            "code" to "10828004",
                            "display" to "Positief",
                        ),
                    ),
                    "text" to "Positief (IHC 3+)",
                ),
                "interpretation" to listOf(
                    mapOf(
                        "coding" to listOf(
                            mapOf(
                                "system" to "http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation",
                                "code" to "POS",
                                "display" to "Positief",
                            ),
                        ),
                    ),
                ),
            ),
        ),
        FhirResource(
            resourceType = "Observation",
            id = "obs-ki67",
            data = mapOf(
                "resourceType" to "Observation",
                "id" to "obs-ki67",
                "status" to "final",
                "code" to mapOf(
                    "coding" to listOf(
                        mapOf(
                            "system" to "http://loinc.org",
                            "code" to "85326-7",
                            "display" to "Ki-67 [groei-index] in borst kanker specimen",
                        ),
                    ),
                    "text" to "Ki-67 proliferatieindex",
                ),
                "subject" to mapOf("reference" to "Patient/patient-001"),
                "effectiveDateTime" to "2024-12-05",
                "valueQuantity" to mapOf(
                    "value" to 38,
                    "unit" to "%",
                    "system" to "http://unitsofmeasure.org",
                    "code" to "%",
                ),
                "interpretation" to listOf(
                    mapOf(
                        "coding" to listOf(
                            mapOf(
                                "system" to "http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation",
                                "code" to "H",
                                "display" to "Hoog",
                            ),
                        ),
                        "text" to "Hoog (≥25% = hoge proliferatie per IKNL)",
                    ),
                ),
            ),
        ),
    )

    private fun buildProcedures(): List<FhirResource> = listOf(
        FhirResource(
            resourceType = "Procedure",
            id = "proc-sentinel",
            data = mapOf(
                "resourceType" to "Procedure",
                "id" to "proc-sentinel",
                "status" to "completed",
                "code" to mapOf(
                    "coding" to listOf(
                        mapOf(
                            "system" to "http://snomed.info/sct",
                            "code" to "396487001",
                            "display" to "Schildwachtklierprocedure",
                        ),
                    ),
                    "text" to "Schildwachtklierprocedure (SLNB) linker axilla",
                ),
                "subject" to mapOf("reference" to "Patient/patient-001"),
                "performedDateTime" to "2025-01-14",
                "outcome" to mapOf(
                    "coding" to listOf(
                        mapOf(
                            "system" to "http://snomed.info/sct",
                            "code" to "385669000",
                            "display" to "Geslaagd",
                        ),
                    ),
                    "text" to "Geen lymfekliermetastasen (pN0 SN)",
                ),
                "note" to listOf(
                    mapOf("text" to "IKNL aanbeveling: SLNB bij cN0 borstkanker."),
                ),
            ),
        ),
        FhirResource(
            resourceType = "Procedure",
            id = "proc-bcs",
            data = mapOf(
                "resourceType" to "Procedure",
                "id" to "proc-bcs",
                "status" to "completed",
                "code" to mapOf(
                    "coding" to listOf(
                        mapOf(
                            "system" to "http://snomed.info/sct",
                            "code" to "392021009",
                            "display" to "Borstsparende chirurgie",
                        ),
                    ),
                    "text" to "Borstsparende chirurgie (BCS) – lumpectomie",
                ),
                "subject" to mapOf("reference" to "Patient/patient-001"),
                "performedDateTime" to "2025-01-14",
                "performer" to listOf(
                    mapOf(
                        "actor" to mapOf(
                            "reference" to "Practitioner/onco-chirurg-001",
                            "display" to "Onco-chirurg, UMC Utrecht",
                        ),
                    ),
                ),
                "note" to listOf(
                    mapOf(
                        "text" to
                            "Vrije resectiemarges geconfirmeerd. R0-resectie. " +
                            "IKNL richtlijn: adjuvante radiotherapie geïndiceerd na BCS.",
                    ),
                ),
            ),
        ),
        FhirResource(
            resourceType = "Procedure",
            id = "proc-radio",
            data = mapOf(
                "resourceType" to "Procedure",
                "id" to "proc-radio",
                "status" to "in-progress",
                "code" to mapOf(
                    "coding" to listOf(
                        mapOf(
                            "system" to "http://snomed.info/sct",
                            "code" to "108290001",
                            "display" to "Radiotherapie",
                        ),
                    ),
                    "text" to "Adjuvante radiotherapie linker mamma (15 fracties)",
                ),
                "subject" to mapOf("reference" to "Patient/patient-001"),
                "performedPeriod" to mapOf(
                    "start" to "2025-03-01",
                    "end" to "2025-03-21",
                ),
                "reasonReference" to listOf(
                    mapOf("reference" to "Condition/condition-001"),
                ),
                "note" to listOf(
                    mapOf(
                        "text" to
                            "Hypofractionering 40 Gy / 15 fracties per FAST-Forward protocol. " +
                            "IKNL Richtlijn Borstkanker 2023 §6.3.",
                    ),
                ),
            ),
        ),
    )

    private fun buildMedicationRequests(): List<FhirResource> = listOf(
        FhirResource(
            resourceType = "MedicationRequest",
            id = "medrq-tras",
            data = mapOf(
                "resourceType" to "MedicationRequest",
                "id" to "medrq-tras",
                "status" to "active",
                "intent" to "order",
                "medicationCodeableConcept" to mapOf(
                    "coding" to listOf(
                        mapOf(
                            "system" to "http://www.whocc.no/atc",
                            "code" to "L01XC03",
                            "display" to "Trastuzumab",
                        ),
                    ),
                    "text" to "Trastuzumab (Herceptin) 6 mg/kg IV q3w",
                ),
                "subject" to mapOf("reference" to "Patient/patient-001"),
                "authoredOn" to "2024-12-20",
                "reasonReference" to listOf(
                    mapOf("reference" to "Condition/condition-001"),
                ),
                "dosageInstruction" to listOf(
                    mapOf(
                        "text" to "6 mg/kg intraveneus elke 3 weken, 18 cycli totaal",
                        "route" to mapOf(
                            "coding" to listOf(
                                mapOf(
                                    "system" to "http://snomed.info/sct",
                                    "code" to "47625008",
                                    "display" to "Intraveneuze route",
                                ),
                            ),
                        ),
                    ),
                ),
                "note" to listOf(
                    mapOf(
                        "text" to
                            "IKNL: Trastuzumab + pertuzumab gedurende 1 jaar bij HER2-positief " +
                            "stadium I-III. Echocardiografie q3m ter controle LVEF.",
                    ),
                ),
            ),
        ),
        FhirResource(
            resourceType = "MedicationRequest",
            id = "medrq-pertu",
            data = mapOf(
                "resourceType" to "MedicationRequest",
                "id" to "medrq-pertu",
                "status" to "active",
                "intent" to "order",
                "medicationCodeableConcept" to mapOf(
                    "coding" to listOf(
                        mapOf(
                            "system" to "http://www.whocc.no/atc",
                            "code" to "L01XC13",
                            "display" to "Pertuzumab",
                        ),
                    ),
                    "text" to "Pertuzumab (Perjeta) 420 mg IV q3w",
                ),
                "subject" to mapOf("reference" to "Patient/patient-001"),
                "authoredOn" to "2024-12-20",
            ),
        ),
        FhirResource(
            resourceType = "MedicationRequest",
            id = "medrq-pacli",
            data = mapOf(
                "resourceType" to "MedicationRequest",
                "id" to "medrq-pacli",
                "status" to "completed",
                "intent" to "order",
                "medicationCodeableConcept" to mapOf(
                    "coding" to listOf(
                        mapOf(
                            "system" to "http://www.whocc.no/atc",
                            "code" to "L01CD01",
                            "display" to "Paclitaxel",
                        ),
                    ),
                    "text" to "Paclitaxel 80 mg/m² IV wekelijks (neoadjuvant)",
                ),
                "subject" to mapOf("reference" to "Patient/patient-001"),
                "authoredOn" to "2024-12-20",
                "note" to listOf(
                    mapOf(
                        "text" to
                            "Neoadjuvant schema TCHP (taxaan + carboplatine + HER2-blokkade). " +
                            "Afgerond na 6 cycli. pCR bereikt in axilla.",
                    ),
                ),
            ),
        ),
    )

    private fun buildCarePlan(): FhirResource = FhirResource(
        resourceType = "CarePlan",
        id = "careplan-001",
        data = mapOf(
            "resourceType" to "CarePlan",
            "id" to "careplan-001",
            "status" to "active",
            "intent" to "plan",
            "title" to "Oncologisch zorgplan – IKNL Richtlijn Borstkanker 2023",
            "description" to
                "Persoonlijk zorgplan voor HER2-positief invasief ductaal carcinoom " +
                "mammae links, Stadium IIA (cT2N0M0), ER-negatief, Ki-67 38%. " +
                "Gebaseerd op IKNL Richtlijn Borstkanker 2023 en MDO UMC Utrecht 2024-12-12.",
            "subject" to mapOf("reference" to "Patient/patient-001"),
            "period" to mapOf(
                "start" to "2024-12-01",
                "end" to "2026-12-01",
            ),
            "activity" to listOf(
                carePlanNeoadjuvantChemo(),
                carePlanSurgery(),
                carePlanRadiotherapy(),
                carePlanHer2Therapy(),
                carePlanSurveillance(),
            ),
            "note" to listOf(
                mapOf(
                    "text" to
                        "Richtlijn: $IKNL_BORSTKANKER_REF. " +
                        "MDO-besluit: 2024-12-12 Multidisciplinair Overleg UMC Utrecht. " +
                        "Diagnosecontext: HER2-positief, ER-negatief, Ki-67 38%, " +
                        "invasief ductaal carcinoom Stadium IIA.",
                ),
            ),
        ),
    )

    private fun carePlanActivity(
        kind: String,
        codeText: String,
        status: String,
        start: String,
        end: String,
        description: String,
        patientInstruction: String,
        nextStep: String,
        actionItems: List<String>,
    ): Map<String, Any?> = mapOf(
        "detail" to mapOf(
            "kind" to kind,
            "code" to mapOf("text" to codeText),
            "status" to status,
            "scheduledPeriod" to mapOf("start" to start, "end" to end),
            "description" to description,
            "patientInstruction" to patientInstruction,
            "nextStep" to nextStep,
            "actionItem" to actionItems,
            "iknlGuidelineRef" to IKNL_BORSTKANKER_REF,
        ),
    )

    private fun carePlanNeoadjuvantChemo(): Map<String, Any?> = carePlanActivity(
        kind = "ServiceRequest",
        codeText = "Neoadjuvante chemotherapie (TCHP schema) – VOLTOOID",
        status = "completed",
        start = "2024-12-20",
        end = "2025-01-10",
        description =
            "Zes cycli TCHP (taxaan + carboplatine + trastuzumab + pertuzumab) " +
                "als neoadjuvante HER2-gerichte therapie bij cT2N0M0 Stadium IIA. " +
                "Pathologie toonde pCR in de axilla; resttumor in de mamma " +
                "was beperkt. IKNL beveelt neoadjuvante HER2-blokkade aan bij " +
                "HER2-positief invasief ductaal carcinoom ≥cT2.",
        patientInstruction =
            "Noteer koorts >38,5°C, aanhoudende misselijkheid of " +
                "neurologische tintelingen in het bijwerkingendagboek. " +
                "Neem contact op bij kortademigheid tijdens trastuzumab.",
        nextStep =
            "Door naar borstsparende chirurgie (BCS) met schildwachtklierprocedure " +
                "(SLNB) zodra bloedbeeld hersteld is.",
        actionItems = listOf(
            "Bewaar het TCHP-infuusschema en de bijsluiter trastuzumab.",
            "Vraag op het MDO-verslag wat pCR in de axilla betekent voor uw prognose.",
        ),
    )

    private fun carePlanSurgery(): Map<String, Any?> = carePlanActivity(
        kind = "ServiceRequest",
        codeText = "Borstsparende chirurgie + schildwachtklierprocedure – VOLTOOID",
        status = "completed",
        start = "2025-01-14",
        end = "2025-01-14",
        description =
            "Borstsparende chirurgie (BCS / lumpectomie) linker mamma met " +
                "gelijktijdige schildwachtklierprocedure (SLNB). " +
                "Resectiemarges R0; schildwachtklier pN0 SN – geen " +
                "okselkliertoilet nodig. Operatie bevestigt Stadium IIA-beeld " +
                "zonder lymfekliermetastasen.",
        patientInstruction =
            "Wondzorg: droog houden tot hechtingen zijn verwijderd. " +
                "Meld roodheid, zwelling of koorts. Vermijd zwaar tillen " +
                "met de linkerarm de eerste twee weken.",
        nextStep =
            "Start adjuvante radiotherapie (hypofractionering) en hervat " +
                "trastuzumab + pertuzumab volgens schema.",
        actionItems = listOf(
            "Vraag of de snijvlakken ruim genoeg vrij waren.",
            "Bespreek wanneer u mag starten met oefeningen voor de schouder.",
        ),
    )

    private fun carePlanRadiotherapy(): Map<String, Any?> = carePlanActivity(
        kind = "ServiceRequest",
        codeText = "Adjuvante radiotherapie – ACTIEF",
        status = "in-progress",
        start = "2025-03-01",
        end = "2025-03-21",
        description =
            "Adjuvante radiotherapie linker mamma na BCS volgens " +
                "hypofractionering / FAST-Forward: 40 Gy in 15 fracties over " +
                "ongeveer drie weken. IKNL §6.3: hypofractionering is standaard " +
                "na borstsparende chirurgie bij Stadium IIA zonder " +
                "lymfeklieruitzaaiing.",
        patientInstruction =
            "Smeer alleen de door de radiotherapeut goedgekeurde crème. " +
                "Vermijd zonlicht en strakke BH’s op de bestraalde huid. " +
                "Noteer huidreacties (roodheid, jeuk, schilfering) dagelijks.",
        nextStep =
            "Nacontrole radiotherapie na de laatste fractie; daarna " +
                "verder met trastuzumab + pertuzumab en LVEF-controles.",
        actionItems = listOf(
            "Noteer huidreacties in uw bijwerkingendagboek.",
            "Vraag hoeveel fracties u nog heeft en wanneer vermoeidheid piekt.",
        ),
    )

    private fun carePlanHer2Therapy(): Map<String, Any?> = carePlanActivity(
        kind = "MedicationRequest",
        codeText = "Adjuvant trastuzumab + pertuzumab (1 jaar) – ACTIEF",
        status = "in-progress",
        start = "2025-01-20",
        end = "2026-01-20",
        description =
            "Achttien cycli trastuzumab (Herceptin) + pertuzumab (Perjeta) " +
                "als adjuvante HER2-blokkade gedurende één jaar. " +
                "Cardiotoxiciteitsbewaking via echocardiografie: LVEF elke " +
                "drie maanden. Bij daling van de ejectiefractie kan de kuur " +
                "tijdelijk worden onderbroken.",
        patientInstruction =
            "Meld kortademigheid, enkeloedeem of snelle hartslag meteen. " +
                "Neem echocardiografie-uitslagen mee naar elk oncologieconsult. " +
                "Vaccinaties en tandheelkunde: stem af met de oncoloog.",
        nextStep =
            "Volgende LVEF-meting plannen; bij stabiele LVEF schema " +
                "voortzetten tot 18 cycli zijn voltooid.",
        actionItems = listOf(
            "Plan echocardiografie elke 3 maanden tijdens trastuzumab.",
            "Vraag wat uw laatste LVEF-waarde was en wat de ondergrens is.",
        ),
    )

    private fun carePlanSurveillance(): Map<String, Any?> = carePlanActivity(
        kind = "ServiceRequest",
        codeText = "Jaarlijkse mammografie controle-plan – GEPLAND",
        status = "scheduled",
        start = "2026-06-01",
        end = "2031-06-01",
        description =
            "Nacontroleschema na afronding van systemische therapie: " +
                "jaarlijkse mammografie gedurende vijf jaar, plus klinische " +
                "nacontrole. Zolang trastuzumab loopt blijft echocardiografie " +
                "voor LVEF onderdeel van de follow-up; daarna op indicatie. " +
                "IKNL: survivorship-zorg na Stadium IIA borstkanker.",
        patientInstruction =
            "Zet mammografie en huisartscontroles in uw agenda. " +
                "Blijf borstzelfonderzoek doen en meld nieuwe knobbels of " +
                "huidveranderingen zonder te wachten op de jaarcontrole.",
        nextStep =
            "Eerste surveillance-mammografie plannen ongeveer één jaar " +
                "na afronding van radiotherapie / HER2-therapie.",
        actionItems = listOf(
            "Vraag wie uw vaste aanspreekpunt is in de nacontrole.",
            "Noteer de datum van de eerste controle-mammografie.",
        ),
    )

    private fun buildAppointments(): List<FhirResource> = listOf(
        FhirResource(
            resourceType = "Appointment",
            id = "appt-001",
            data = mapOf(
                "resourceType" to "Appointment",
                "id" to "appt-001",
                "status" to "booked",
                "serviceCategory" to listOf(
                    mapOf(
                        "coding" to listOf(
                            mapOf(
                                "system" to "http://snomed.info/sct",
                                "code" to "363346000",
                                "display" to "Oncologie",
                            ),
                        ),
                        "text" to "Oncologie",
                    ),
                ),
                "serviceType" to listOf(
                    mapOf("text" to "Poliklinisch consult medisch oncoloog"),
                ),
                "reasonCode" to listOf(
                    mapOf(
                        "text" to
                            "Tussentijdse evaluatie radiotherapie + trastuzumab bijwerkingen",
                    ),
                ),
                "start" to "2025-04-08T10:30:00+02:00",
                "end" to "2025-04-08T11:00:00+02:00",
                "participant" to listOf(
                    mapOf(
                        "actor" to mapOf(
                            "reference" to "Practitioner/oncoloog-001",
                            "display" to "Dr. A. de Vries – Medisch oncoloog",
                        ),
                        "status" to "accepted",
                    ),
                    mapOf(
                        "actor" to mapOf("reference" to "Patient/patient-001"),
                        "status" to "accepted",
                    ),
                ),
                "location" to mapOf(
                    "reference" to "Location/umcu-oncologie",
                    "display" to "UMC Utrecht – Afdeling Oncologie, Polikliniek 3B",
                ),
                "comment" to
                    "Meenemen: bijwerkingendagboek, lijst met huidige medicatie, " +
                    "vragen over LVEF-uitslag echocardiografie.",
            ),
        ),
        FhirResource(
            resourceType = "Appointment",
            id = "appt-002",
            data = mapOf(
                "resourceType" to "Appointment",
                "id" to "appt-002",
                "status" to "booked",
                "serviceType" to listOf(
                    mapOf("text" to "Echocardiografie – LVEF-meting"),
                ),
                "reasonCode" to listOf(
                    mapOf(
                        "text" to
                            "LVEF-monitoring trastuzumab: cardiotoxiciteitscontrole q3m",
                    ),
                ),
                "start" to "2025-04-22T09:00:00+02:00",
                "end" to "2025-04-22T09:30:00+02:00",
                "participant" to listOf(
                    mapOf(
                        "actor" to mapOf(
                            "reference" to "Practitioner/cardioloog-001",
                            "display" to "Afdeling Cardiologie",
                        ),
                        "status" to "accepted",
                    ),
                ),
                "location" to mapOf(
                    "display" to "UMC Utrecht – Afdeling Cardiologie",
                ),
            ),
        ),
        FhirResource(
            resourceType = "Appointment",
            id = "appt-003",
            data = mapOf(
                "resourceType" to "Appointment",
                "id" to "appt-003",
                "status" to "booked",
                "serviceType" to listOf(
                    mapOf("text" to "Nacontrole radiotherapie"),
                ),
                "reasonCode" to listOf(
                    mapOf("text" to "Afsluiting radiotherapie – beoordeling huidreactie"),
                ),
                "start" to "2025-04-25T14:00:00+02:00",
                "end" to "2025-04-25T14:30:00+02:00",
                "participant" to listOf(
                    mapOf(
                        "actor" to mapOf(
                            "reference" to "Practitioner/radiotherapeut-001",
                            "display" to "Dr. M. Peters – Radiotherapeut-oncoloog",
                        ),
                        "status" to "accepted",
                    ),
                ),
                "location" to mapOf(
                    "display" to "UMC Utrecht – Afdeling Radiotherapie",
                ),
            ),
        ),
    )
}
