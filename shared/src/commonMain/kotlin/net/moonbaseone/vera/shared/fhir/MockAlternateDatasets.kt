package net.moonbaseone.vera.shared.fhir

import net.moonbaseone.vera.shared.domain.IknlGuidelines

/**
 * Alternate mock care plans / appointments for [MockDatasetId.ColonStageIii]
 * and [MockDatasetId.LungEarlyStart].
 */
internal object MockAlternateDatasets {
    private const val IKNL_COLON = IknlGuidelines.COLON
    private const val IKNL_LUNG = IknlGuidelines.LUNG_NSCLC

    fun colonCarePlan(): FhirResource = FhirResource(
        resourceType = "CarePlan",
        id = "careplan-colon-001",
        data = mapOf(
            "resourceType" to "CarePlan",
            "id" to "careplan-colon-001",
            "status" to "active",
            "intent" to "plan",
            "title" to "Zorgplan colorectaal carcinoom stadium III",
            "subject" to mapOf("reference" to "Patient/patient-colon-001"),
            "period" to mapOf("start" to "2025-01-10"),
            "activity" to listOf(
                activity(
                    kind = "Procedure",
                    codeText = "Hemicolectomie rechts – VOLTOOID",
                    status = "completed",
                    start = "2025-01-10",
                    end = "2025-01-10",
                    description =
                        "Rechtszijdige hemicolectomie bij adenocarcinoom van het " +
                            "colon ascendens, pT3N1M0 (stadium III). " +
                            "R0-resectie; 2/18 lymfeklieren positief.",
                    patientInstruction =
                        "Wondzorg en voedingsadvies van de stomaverpleegkundige " +
                            "opvolgen. Meld koorts, buikpijn of niet-functionerende stoma.",
                    nextStep = "Start adjuvante chemotherapie (CAPOX) binnen 6–8 weken.",
                    actionItems = listOf(
                        "Vraag of u in aanmerking komt voor CAPOX of FOLFOX.",
                        "Noteer wanneer de pathologiebespreking was en wat pN1 betekent.",
                    ),
                    iknl = IKNL_COLON,
                ),
                activity(
                    kind = "MedicationRequest",
                    codeText = "Adjuvante chemotherapie CAPOX – ACTIEF",
                    status = "in-progress",
                    start = "2025-03-01",
                    end = "2025-08-01",
                    description =
                        "Acht cycli CAPOX (capecitabine + oxaliplatine) als " +
                            "adjuvante therapie bij stadium III coloncarcinoom. " +
                            "IKNL: adjuvante chemotherapie verlaagt het risico op recidief.",
                    patientInstruction =
                        "Noteer tintelingen in handen/voeten (neuropathie) en " +
                            "hand-voetsyndroom. Drink voldoende; vermijd extreme kou.",
                    nextStep = "Tussenevaluatie na 4 cycli; zo nodig dosisaanpassing.",
                    actionItems = listOf(
                        "Vraag wanneer neuropathie reden is om oxaliplatine te stoppen.",
                        "Plan bloedcontroles vóór elke kuur.",
                    ),
                    iknl = IKNL_COLON,
                ),
                activity(
                    kind = "ServiceRequest",
                    codeText = "Follow-up CEA + CT – GEPLAND",
                    status = "scheduled",
                    start = "2025-09-01",
                    end = "2030-09-01",
                    description =
                        "Nacontroleschema: CEA elke 3–6 maanden en CT-thorax/abdomen " +
                            "volgens IKNL-survivorship na stadium III.",
                    patientInstruction =
                        "Zet CEA-prikken in de agenda. Meld onverklaarde buikklachten " +
                            "of gewichtsverlies niet pas bij de jaarafspraak.",
                    nextStep = "Eerste post-adjuvante CEA en beeldvorming plannen.",
                    actionItems = listOf(
                        "Vraag wie uw vaste contact is op de polikliniek chirurgie/oncologie.",
                    ),
                    iknl = IKNL_COLON,
                ),
            ),
            "note" to listOf(
                mapOf(
                    "text" to
                        "Richtlijn: $IKNL_COLON. " +
                        "Diagnose: adenocarcinoom colon ascendens, stadium III (pT3N1M0).",
                ),
            ),
        ),
    )

    fun colonAppointments(): List<FhirResource> = listOf(
        appointment(
            id = "appt-colon-001",
            serviceType = "Poliklinisch consult medisch oncoloog",
            reason = "Evaluatie CAPOX-cyclus 3 – toxiciteit en dosis",
            start = "2025-04-10T11:00:00+02:00",
            end = "2025-04-10T11:30:00+02:00",
            practitioner = "Dr. S. Bakker – Medisch oncoloog",
            location = "Amsterdam UMC – Oncologie, polikliniek C2",
            comment = "Meenemen: bijwerkingendagboek en recente bloeduitslagen.",
        ),
        appointment(
            id = "appt-colon-002",
            serviceType = "Infusion – CAPOX cyclus 4",
            reason = "Toediening oxaliplatine + start capecitabine-kuur",
            start = "2025-04-17T09:00:00+02:00",
            end = "2025-04-17T12:00:00+02:00",
            practitioner = "Dagbehandeling Oncologie",
            location = "Amsterdam UMC – Dagbehandeling Oncologie",
        ),
        appointment(
            id = "appt-colon-003",
            serviceType = "Consult stomaverpleegkundige",
            reason = "Controle stoma en huid na chemotherapie",
            start = "2025-04-24T15:00:00+02:00",
            end = "2025-04-24T15:30:00+02:00",
            practitioner = "Stomazorg-team",
            location = "Amsterdam UMC – Stomacare",
        ),
    )

    fun lungCarePlan(): FhirResource = FhirResource(
        resourceType = "CarePlan",
        id = "careplan-lung-001",
        data = mapOf(
            "resourceType" to "CarePlan",
            "id" to "careplan-lung-001",
            "status" to "active",
            "intent" to "plan",
            "title" to "Zorgplan NSCLC – starttraject",
            "subject" to mapOf("reference" to "Patient/patient-lung-001"),
            "period" to mapOf("start" to "2025-04-01"),
            "activity" to listOf(
                activity(
                    kind = "DiagnosticReport",
                    codeText = "Diagnostiek NSCLC – VOLTOOID",
                    status = "completed",
                    start = "2025-03-20",
                    end = "2025-03-28",
                    description =
                        "PET-CT en biopsie bevestigen niet-kleincellig longcarcinoom " +
                            "linkerbovenkwab, cT2N1M0. Moleculaire diagnostiek " +
                            "(EGFR/ALK/ROS1/PD-L1) aangevraagd.",
                    patientInstruction =
                        "Rookstop ondersteuning starten indien van toepassing. " +
                            "Neem alle medicatielijsten mee naar het MDO-gesprek.",
                    nextStep = "Multidisciplinair overleg (MDO) voor behandelvoorstel.",
                    actionItems = listOf(
                        "Vraag wanneer de moleculaire uitslagen bekend zijn.",
                        "Noteer welke behandelopties (chirurgie, chemo, immuno) op tafel liggen.",
                    ),
                    iknl = IKNL_LUNG,
                ),
                activity(
                    kind = "ServiceRequest",
                    codeText = "MDO longoncologie – ACTIEF",
                    status = "in-progress",
                    start = "2025-04-05",
                    end = "2025-04-05",
                    description =
                        "Multidisciplinair overleg longoncologie (chirurg, " +
                            "longarts, oncoloog, radiotherapeut) om stadium en " +
                            "behandelintentie vast te stellen volgens IKNL NSCLC.",
                    patientInstruction =
                        "Schrijf vragen op voor het uitslaggesprek na het MDO. " +
                            "Neem een naaste mee indien mogelijk.",
                    nextStep = "Uitslaggesprek en shared decision making met longarts.",
                    actionItems = listOf(
                        "Vraag of curatieve intentie (chirurgie ± adjuvans) mogelijk is.",
                        "Vraag naar timing van operatie versus systemische therapie.",
                    ),
                    iknl = IKNL_LUNG,
                ),
                activity(
                    kind = "ServiceRequest",
                    codeText = "Behandeling na MDO – GEPLAND",
                    status = "scheduled",
                    start = "2025-04-20",
                    end = "2025-12-01",
                    description =
                        "Behandeltraject volgt na MDO: waarschijnlijk resectie " +
                            "met of zonder neoadjuvante immuno-/chemotherapie, " +
                            "afhankelijk van moleculair profiel en fitheid.",
                    patientInstruction =
                        "Houd agenda flexibel voor snelle start van behandeling. " +
                            "Meld toenemende dyspneu of hemoptoë meteen.",
                    nextStep = "Behandelplan bevestigen in uitslaggesprek.",
                    actionItems = listOf(
                        "Vraag een second opinion-mogelijkheid indien u twijfelt.",
                    ),
                    iknl = IKNL_LUNG,
                ),
            ),
            "note" to listOf(
                mapOf(
                    "text" to
                        "Richtlijn: $IKNL_LUNG. " +
                        "Diagnose: NSCLC linker bovenkwab, cT2N1M0 — behandelplan in wording.",
                ),
            ),
        ),
    )

    fun lungAppointments(): List<FhirResource> = listOf(
        appointment(
            id = "appt-lung-001",
            serviceType = "Uitslaggesprek longarts",
            reason = "MDO-uitslag en shared decision making behandelplan",
            start = "2025-04-08T13:30:00+02:00",
            end = "2025-04-08T14:15:00+02:00",
            practitioner = "Dr. L. Jansen – Longarts-oncoloog",
            location = "Antoni van Leeuwenhoek – Longoncologie",
            comment = "Neem vragenlijst en naaste mee.",
        ),
        appointment(
            id = "appt-lung-002",
            serviceType = "Preoperatieve screening",
            reason = "Fitheid en longfunctie voor eventuele resectie",
            start = "2025-04-15T10:00:00+02:00",
            end = "2025-04-15T11:00:00+02:00",
            practitioner = "Anesthesiologie / Longfunctie",
            location = "AVL – Preoperatieve poli",
        ),
        appointment(
            id = "appt-lung-003",
            serviceType = "Consult thoraxchirurgie",
            reason = "Bespreken lobectomie vs. segmentresectie",
            start = "2025-04-18T09:30:00+02:00",
            end = "2025-04-18T10:00:00+02:00",
            practitioner = "Dr. R. Vermeer – Thoraxchirurg",
            location = "AVL – Thoraxchirurgie",
        ),
    )

    private fun activity(
        kind: String,
        codeText: String,
        status: String,
        start: String,
        end: String,
        description: String,
        patientInstruction: String,
        nextStep: String,
        actionItems: List<String>,
        iknl: String,
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
            "iknlGuidelineRef" to iknl,
        ),
    )

    private fun appointment(
        id: String,
        serviceType: String,
        reason: String,
        start: String,
        end: String,
        practitioner: String,
        location: String,
        comment: String? = null,
    ): FhirResource = FhirResource(
        resourceType = "Appointment",
        id = id,
        data = buildMap {
            put("resourceType", "Appointment")
            put("id", id)
            put("status", "booked")
            put("serviceType", listOf(mapOf("text" to serviceType)))
            put("reasonCode", listOf(mapOf("text" to reason)))
            put("start", start)
            put("end", end)
            put(
                "participant",
                listOf(
                    mapOf(
                        "actor" to mapOf("display" to practitioner),
                        "status" to "accepted",
                    ),
                ),
            )
            put("location", mapOf("display" to location))
            if (comment != null) put("comment", comment)
        },
    )
}
