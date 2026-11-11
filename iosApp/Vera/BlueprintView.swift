import SwiftUI

struct TimelineEventModel: Identifiable, Equatable {
    let id: String
    let title: String
    let description: String
    let dateIso: String
    let phase: Phase
    let fhirStatus: String?
    let patientInstruction: String?
    let nextSteps: String?
    let actionItems: [String]
    let jargon: [JargonMatch]

    enum Phase: String {
        case completed, current, future
    }
}

struct JargonMatch: Identifiable, Equatable {
    var id: String { term }
    let term: String
    let explanation: String
    let actionItem: String
}

/// Blueprint timeline UI (CarePlan mock aligned with shared MockDataService).
struct BlueprintView: View {
    @State private var events: [TimelineEventModel] = BlueprintMockData.events
    @State private var expandedId: String?
    @State private var expandedJargonTerm: String?
    @State private var showClinicalText = false
    @State private var didCenterOnLaunch = false

    private var focusId: String? {
        expandedId ?? events.first(where: { $0.phase == .current })?.id
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 8) {
                HStack(spacing: 14) {
                    LegendDot(color: ClinicalColors.statusCompleted, label: "Afgerond")
                    LegendDot(color: ClinicalColors.statusInProgress, label: "Huidig")
                    LegendDot(color: ClinicalColors.statusScheduled, label: "Gepland")
                }
                .accessibilityElement(children: .combine)
                .accessibilityLabel("Legenda: Afgerond, Huidig, Gepland")

                ScrollViewReader { proxy in
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 0) {
                            ForEach(Array(events.enumerated()), id: \.element.id) { index, event in
                                TimelineNodeView(
                                    event: event,
                                    expanded: expandedId == event.id
                                ) {
                                    expandedId = expandedId == event.id ? nil : event.id
                                    expandedJargonTerm = nil
                                    if let id = expandedId {
                                        withAnimation(.easeInOut(duration: 0.25)) {
                                            proxy.scrollTo(id, anchor: .center)
                                        }
                                    }
                                }
                                .id(event.id)
                                if index < events.count - 1 {
                                    Rectangle()
                                        .fill(Color.secondary.opacity(0.3))
                                        .frame(width: 24, height: 2)
                                }
                            }
                        }
                        .padding(.vertical, 4)
                    }
                    .onAppear {
                        centerCurrentItem(proxy: proxy)
                    }
                    .onChange(of: expandedId) { newValue in
                        guard let id = newValue else { return }
                        withAnimation(.easeInOut(duration: 0.25)) {
                            proxy.scrollTo(id, anchor: .center)
                        }
                    }
                }

                if let expanded = events.first(where: { $0.id == expandedId }) {
                    EventDetailCard(
                        event: expanded,
                        expandedJargonTerm: expandedJargonTerm,
                        showClinicalText: showClinicalText,
                        onToggleJargon: { term in
                            expandedJargonTerm = expandedJargonTerm == term ? nil : term
                        },
                        onShowClinicalText: { clinical in
                            showClinicalText = clinical
                            if clinical { expandedJargonTerm = nil }
                        }
                    )
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 12)
            .padding(.bottom, 8)
        }
        .onAppear {
            if expandedId == nil {
                expandedId = events.first(where: { $0.phase == .current })?.id
            }
        }
    }

    private func centerCurrentItem(proxy: ScrollViewProxy) {
        guard !didCenterOnLaunch, let id = focusId else { return }
        didCenterOnLaunch = true
        // Defer one run-loop so layout has measured carousel widths.
        DispatchQueue.main.async {
            withAnimation(.easeInOut(duration: 0.3)) {
                proxy.scrollTo(id, anchor: .center)
            }
        }
    }
}

private struct LegendDot: View {
    let color: Color
    let label: String

    var body: some View {
        HStack(spacing: 6) {
            Circle().fill(color).frame(width: 12, height: 12)
            Text(label).font(.caption)
        }
    }
}

private struct TimelineNodeView: View {
    let event: TimelineEventModel
    let expanded: Bool
    let onTap: () -> Void

    private var phaseColor: Color {
        switch event.phase {
        case .completed: return ClinicalColors.statusCompleted
        case .current: return ClinicalColors.statusInProgress
        case .future: return ClinicalColors.statusScheduled
        }
    }

    private var phaseLabel: String {
        switch event.phase {
        case .completed: return "Afgerond"
        case .current: return "Huidig"
        case .future: return "Gepland"
        }
    }

    var body: some View {
        Button(action: onTap) {
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 6) {
                    Circle().fill(phaseColor).frame(width: 10, height: 10)
                    Text(phaseLabel)
                        .font(.caption.weight(.bold))
                        .foregroundStyle(expanded ? Color.white : phaseColor)
                }
                Text(event.title)
                    .font(.caption)
                    .foregroundStyle(expanded ? Color.white : Color.primary)
                    .lineLimit(3)
                    .multilineTextAlignment(.leading)
            }
            .padding(.horizontal, 10)
            .padding(.vertical, 8)
            .frame(width: 140, height: 92, alignment: .topLeading)
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(expanded ? phaseColor : phaseColor.opacity(0.12))
            )
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(phaseColor, lineWidth: expanded ? 2 : 1.5)
            )
        }
        .buttonStyle(.plain)
        .accessibilityLabel("\(event.title). Status: \(phaseLabel).")
    }
}

private struct EventDetailCard: View {
    let event: TimelineEventModel
    let expandedJargonTerm: String?
    let showClinicalText: Bool
    let onToggleJargon: (String) -> Void
    let onShowClinicalText: (Bool) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(event.title).font(.headline)
                Spacer()
                if let status = event.fhirStatus {
                    Text(statusLabel(status))
                        .font(.caption.weight(.bold))
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(Capsule().stroke(Color.secondary))
                }
            }
            if !event.dateIso.isEmpty {
                Text("Gepland: \(DateDisplay.formatIso(event.dateIso))")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }

            Picker("Tekstmodus", selection: Binding(
                get: { showClinicalText },
                set: { onShowClinicalText($0) }
            )) {
                Text("Begrijpelijk").tag(false)
                Text("Dokterstekst").tag(true)
            }
            .pickerStyle(.segmented)

            if showClinicalText {
                Text("Originele tekst uit uw zorgplan")
                    .font(.caption2)
                    .foregroundStyle(.secondary)
                Text(event.description).font(.body)
            } else {
                if let instruction = event.patientInstruction, !instruction.isEmpty {
                    Text(instruction).font(.body)
                }

                let checklist = distinctActionItems(
                    instruction: event.patientInstruction,
                    actionItems: event.actionItems
                )
                if !checklist.isEmpty {
                    Text("Actiepunten:").font(.subheadline.weight(.semibold))
                    ForEach(checklist, id: \.self) { item in
                        Label(item, systemImage: "checkmark.circle")
                            .font(.caption)
                    }
                }

                if !event.jargon.isEmpty {
                    Text("Moeilijke woorden").font(.subheadline.weight(.semibold))
                    Text("Tik op een term voor uitleg – zonder naar Decoder te gaan")
                        .font(.caption2)
                        .foregroundStyle(.secondary)
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(event.jargon) { match in
                                let selected = expandedJargonTerm == match.term
                                Button {
                                    onToggleJargon(match.term)
                                } label: {
                                    Text(match.term)
                                        .font(.caption.weight(.semibold))
                                        .padding(.horizontal, 10)
                                        .padding(.vertical, 6)
                                        .background(
                                            Capsule()
                                                .fill(selected ? ClinicalColors.primary : Color(.secondarySystemBackground))
                                        )
                                        .foregroundStyle(selected ? Color.white : Color.primary)
                                }
                                .buttonStyle(.plain)
                                .accessibilityLabel(match.term)
                            }
                        }
                    }
                    if let open = event.jargon.first(where: { $0.term == expandedJargonTerm }) {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(open.term).font(.subheadline.weight(.bold))
                            Text(open.explanation).font(.caption)
                            if !open.actionItem.isEmpty {
                                Text("Actie: \(open.actionItem)")
                                    .font(.caption)
                                    .italic()
                            }
                        }
                        .padding(10)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(RoundedRectangle(cornerRadius: 12).fill(Color(.secondarySystemBackground)))
                    }
                }
            }
        }
        .padding(12)
        .background(
            RoundedRectangle(cornerRadius: 14)
                .stroke(Color.secondary.opacity(0.3), lineWidth: 1)
        )
    }

    private func statusLabel(_ status: String) -> String {
        switch status {
        case "completed": return "Voltooid"
        case "in-progress", "active": return "Actief"
        case "scheduled": return "Gepland"
        default: return status
        }
    }

    /// Drop checklist rows that merely restate the plain-language body.
    private func distinctActionItems(instruction: String?, actionItems: [String]) -> [String] {
        let body = (instruction ?? "").lowercased()
        guard !body.isEmpty else { return actionItems }
        return actionItems.filter { item in
            var normalized = item.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
            if normalized.hasSuffix(".") {
                normalized = String(normalized.dropLast())
            }
            return !normalized.isEmpty && !body.contains(normalized)
        }
    }
}

enum BlueprintMockData {
    static let events: [TimelineEventModel] = [
        TimelineEventModel(
            id: "1",
            title: "Neoadjuvante chemotherapie (TCHP schema)",
            description:
                "Zes cycli TCHP (taxaan + carboplatine + trastuzumab + pertuzumab) als " +
                "neoadjuvante HER2-gerichte therapie bij cT2N0M0 Stadium IIA. Pathologie " +
                "toonde pCR in de axilla. IKNL beveelt neoadjuvante HER2-blokkade aan bij " +
                "HER2-positief invasief ductaal carcinoom ≥cT2.",
            dateIso: "2024-12-20",
            phase: .completed,
            fhirStatus: "completed",
            patientInstruction:
                "Voor de operatie kreeg u chemotherapie (TCHP) om de tumor te verkleinen. " +
                "Noteer koorts >38,5°C of kortademigheid tijdens trastuzumab in het bijwerkingendagboek.",
            nextSteps: "Door naar borstsparende chirurgie (BCS) met schildwachtklierprocedure (SLNB).",
            actionItems: [
                "Bewaar het TCHP-infuusschema en de bijsluiter trastuzumab.",
                "Vraag wat pCR in de axilla betekent voor uw prognose.",
            ],
            jargon: [
                JargonMatch(
                    term: "HER2-positief",
                    explanation:
                        "Uw tumor maakt veel HER2-eiwit aan. Dit eiwit stimuleert de celgroei. " +
                        "\"Positief\" betekent dat de tumor gevoelig is voor HER2-remmers zoals trastuzumab.",
                    actionItem: "Vraag uw oncoloog of trastuzumab (Herceptin) onderdeel is van uw behandelplan."
                ),
                JargonMatch(
                    term: "Stadium IIA",
                    explanation:
                        "Stadium IIA (cT2N0M0) betekent: tumor tussen 2 en 5 cm, " +
                        "geen bewijs van uitzaaiing naar lymfeklieren of andere organen.",
                    actionItem: "Vraag hoe het stadium uw behandelopties beïnvloedt."
                ),
                JargonMatch(
                    term: "pCR (pathologisch complete respons)",
                    explanation:
                        "Geen levende tumorcellen meer in het verwijderde weefsel na neoadjuvante therapie.",
                    actionItem: "Vraag wat pCR voor ú betekent."
                ),
            ]
        ),
        TimelineEventModel(
            id: "2",
            title: "Borstsparende chirurgie + schildwachtklierprocedure",
            description:
                "Borstsparende chirurgie (BCS / lumpectomie) met schildwachtklierprocedure (SLNB). " +
                "Resectiemarges R0; schildwachtklier pN0 SN. Operatie bevestigt Stadium IIA zonder " +
                "lymfekliermetastasen.",
            dateIso: "2025-01-14",
            phase: .completed,
            fhirStatus: "completed",
            patientInstruction:
                "U bent geopereerd: de tumor is uit de borst gehaald en de schildwachtklier is onderzocht. " +
                "Wond droog houden; meld roodheid, zwelling of koorts. Geen zwaar tillen met de linkerarm.",
            nextSteps: "Start adjuvante radiotherapie (hypofractionering) en hervat trastuzumab + pertuzumab.",
            actionItems: [
                "Vraag of de snijvlakken ruim genoeg vrij waren.",
                "Bespreek wanneer u mag starten met schouderoefeningen.",
            ],
            jargon: [
                JargonMatch(
                    term: "Borstsparende chirurgie (BCS)",
                    explanation:
                        "Operatie waarbij alleen de tumor en een randje gezond weefsel worden verwijderd, " +
                        "gevolgd door bestraling van de resterende borst.",
                    actionItem: "Bespreek of BCS of mastectomie voor u geldt."
                ),
                JargonMatch(
                    term: "Schildwachtklierprocedure (SLNB)",
                    explanation:
                        "Eerste lymfeklier waarop de tumor uitstroomt wordt verwijderd en onderzocht.",
                    actionItem: "Vraag of uw schildwachtklier vrij was (pN0 SN)."
                ),
            ]
        ),
        TimelineEventModel(
            id: "3",
            title: "Adjuvante radiotherapie",
            description:
                "Adjuvante radiotherapie na BCS volgens hypofractionering / FAST-Forward: " +
                "40 Gy in 15 fracties. IKNL §6.3: hypofractionering is standaard na " +
                "borstsparende chirurgie bij Stadium IIA.",
            dateIso: "2025-03-01",
            phase: .current,
            fhirStatus: "in-progress",
            patientInstruction:
                "U krijgt bestraling van de linkerborst in kortere sessies (ongeveer 15 keer over drie weken). " +
                "Dit is standaard na een borstsparende operatie. Alleen goedgekeurde crème; vermijd zonlicht. " +
                "Noteer huidreacties dagelijks.",
            nextSteps: "Nacontrole na laatste fractie; verder met trastuzumab en LVEF-controles.",
            actionItems: ["Vraag hoeveel fracties u nog heeft en wanneer vermoeidheid piekt."],
            jargon: [
                JargonMatch(
                    term: "Hypofractionering",
                    explanation:
                        "Bestralingsschema met minder maar iets hogere doses per sessie " +
                        "(bijv. 40 Gy in 15 fracties).",
                    actionItem: "Vraag hoeveel fracties u nog heeft."
                ),
            ]
        ),
        TimelineEventModel(
            id: "4",
            title: "Adjuvant trastuzumab + pertuzumab (1 jaar)",
            description:
                "Achttien cycli trastuzumab (Herceptin) + pertuzumab als adjuvante HER2-blokkade. " +
                "Cardiotoxiciteitsbewaking via echocardiografie: LVEF elke drie maanden.",
            dateIso: "2025-01-20",
            phase: .current,
            fhirStatus: "in-progress",
            patientInstruction:
                "U krijgt nog ongeveer een jaar trastuzumab en pertuzumab (HER2-medicatie) via het infuus. " +
                "Meld kortademigheid of enkeloedeem meteen. Neem echo-uitslagen mee naar elk consult.",
            nextSteps: "Volgende LVEF-meting plannen; bij stabiele LVEF schema voortzetten.",
            actionItems: [
                "Plan echocardiografie elke 3 maanden tijdens trastuzumab.",
                "Vraag wat uw laatste LVEF-waarde was.",
            ],
            jargon: [
                JargonMatch(
                    term: "HER2-positief",
                    explanation:
                        "Tumor gevoelig voor HER2-remmers zoals trastuzumab (Herceptin).",
                    actionItem: "Vraag of trastuzumab onderdeel blijft van uw plan."
                ),
                JargonMatch(
                    term: "LVEF (linker ventrikel ejectiefractie)",
                    explanation:
                        "Meet hoe goed uw hart bloed rondpompt; trastuzumab kan dit beïnvloeden.",
                    actionItem: "Houd echocardiografie-afspraken bij."
                ),
            ]
        ),
        TimelineEventModel(
            id: "5",
            title: "Jaarlijkse mammografie controle-plan",
            description:
                "Nacontroleschema: jaarlijkse mammografie gedurende vijf jaar. " +
                "Zolang trastuzumab loopt blijft echocardiografie voor LVEF onderdeel van follow-up. " +
                "IKNL survivorship-zorg na Stadium IIA.",
            dateIso: "2026-06-01",
            phase: .future,
            fhirStatus: "scheduled",
            patientInstruction:
                "Na de behandeling volgt vijf jaar controle met jaarlijkse mammografie. " +
                "Zet mammografie in uw agenda; meld nieuwe knobbels zonder te wachten op jaarcontrole.",
            nextSteps: "Eerste surveillance-mammografie plannen na afronding van HER2-therapie.",
            actionItems: [
                "Vraag wie uw vaste aanspreekpunt is in de nacontrole.",
                "Noteer de datum van de eerste controle-mammografie.",
            ],
            jargon: [
                JargonMatch(
                    term: "LVEF (linker ventrikel ejectiefractie)",
                    explanation: "Hartpompfunctie die tijdens trastuzumab wordt gevolgd.",
                    actionItem: "Vraag wanneer LVEF-controles stoppen na de kuur."
                ),
            ]
        ),
    ]
}
