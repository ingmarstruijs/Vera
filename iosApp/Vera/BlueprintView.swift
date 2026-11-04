import SwiftUI

struct TimelineEventModel: Identifiable, Equatable {
    let id: String
    let title: String
    let description: String
    let dateIso: String
    let phase: Phase
    let fhirStatus: String?
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

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                Text("Uw Tijdlijn")
                    .font(.title2.weight(.bold))
                Text("Uw diagnose afgezet tegen IKNL-richtlijnen")
                    .font(.body)
                    .foregroundStyle(.secondary)
                Label("Bron: IKNL Richtlijnendatabase", systemImage: "checkmark.seal")
                    .font(.caption)
                    .foregroundStyle(Color(red: 0.11, green: 0.37, blue: 0.13))

                HStack(spacing: 16) {
                    LegendDot(color: Color(red: 0.05, green: 0.28, blue: 0.63), label: "Afgerond")
                    LegendDot(color: Color(red: 0.11, green: 0.37, blue: 0.13), label: "Huidig")
                    LegendDot(color: Color(red: 0.88, green: 0.48, blue: 0), label: "Gepland")
                }
                .padding(.top, 4)

                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 0) {
                        ForEach(Array(events.enumerated()), id: \.element.id) { index, event in
                            TimelineNodeView(
                                event: event,
                                expanded: expandedId == event.id
                            ) {
                                expandedId = expandedId == event.id ? nil : event.id
                            }
                            if index < events.count - 1 {
                                Rectangle()
                                    .fill(Color.secondary.opacity(0.3))
                                    .frame(width: 24, height: 2)
                            }
                        }
                    }
                    .padding(.vertical, 8)
                }

                if let expanded = events.first(where: { $0.id == expandedId }) {
                    EventDetailCard(event: expanded)
                }
            }
            .padding(20)
        }
        .onAppear {
            if expandedId == nil {
                expandedId = events.first(where: { $0.phase == .current })?.id
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
        case .completed: return Color(red: 0.05, green: 0.28, blue: 0.63)
        case .current: return Color(red: 0.11, green: 0.37, blue: 0.13)
        case .future: return Color(red: 0.88, green: 0.48, blue: 0)
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
            VStack(alignment: .leading, spacing: 6) {
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
            .padding(10)
            .frame(width: 140, height: 100, alignment: .topLeading)
            .background(
                RoundedRectangle(cornerRadius: 14)
                    .fill(expanded ? phaseColor : phaseColor.opacity(0.12))
            )
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(phaseColor, lineWidth: expanded ? 2 : 1.5)
            )
        }
        .buttonStyle(.plain)
        .accessibilityLabel("\(event.title). Status: \(phaseLabel).")
    }
}

private struct EventDetailCard: View {
    let event: TimelineEventModel

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                Text(event.title).font(.headline)
                Spacer()
                if let status = event.fhirStatus {
                    Text(statusLabel(status))
                        .font(.caption.weight(.bold))
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Capsule().stroke(Color.secondary))
                }
            }
            if !event.dateIso.isEmpty {
                Text("Gepland: \(event.dateIso)")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            Divider()
            Text(event.description).font(.body)

            if !event.jargon.isEmpty {
                Text("Begrijpelijke uitleg").font(.subheadline.weight(.semibold))
                ForEach(event.jargon) { match in
                    VStack(alignment: .leading, spacing: 6) {
                        Text(match.term).font(.subheadline.weight(.bold))
                        Text(match.explanation).font(.caption)
                        if !match.actionItem.isEmpty {
                            Text("Actie: \(match.actionItem)")
                                .font(.caption)
                                .italic()
                        }
                    }
                    .padding(12)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(RoundedRectangle(cornerRadius: 12).fill(Color(.secondarySystemBackground)))
                }
            }

            if !event.actionItems.isEmpty {
                Text("Actiepunten:").font(.subheadline.weight(.semibold))
                ForEach(event.actionItems, id: \.self) { item in
                    Label(item, systemImage: "checkmark.circle")
                        .font(.caption)
                }
            }
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 16)
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
}

enum BlueprintMockData {
    static let events: [TimelineEventModel] = [
        TimelineEventModel(
            id: "1",
            title: "Neoadjuvante chemotherapie (TCHP schema)",
            description: "IKNL: Neoadjuvante HER2-gerichte therapie aanbevolen bij cT2 HER2-positieve tumoren.",
            dateIso: "2024-12-20",
            phase: .completed,
            fhirStatus: "completed",
            actionItems: [],
            jargon: [
                JargonMatch(
                    term: "HER2-positief",
                    explanation: "Uw tumor maakt veel HER2-eiwit aan. Dit eiwit stimuleert de celgroei.",
                    actionItem: "Vraag uw oncoloog of trastuzumab onderdeel is van uw behandelplan."
                ),
            ]
        ),
        TimelineEventModel(
            id: "2",
            title: "Borstsparende chirurgie + schildwachtklierprocedure",
            description: "Operatie afgerond met vrije resectiemarges.",
            dateIso: "2025-01-14",
            phase: .completed,
            fhirStatus: "completed",
            actionItems: [],
            jargon: []
        ),
        TimelineEventModel(
            id: "3",
            title: "Adjuvante radiotherapie",
            description: "IKNL §6.3: Hypofractionering (40 Gy / 15 fracties) na BCS.",
            dateIso: "2025-03-01",
            phase: .current,
            fhirStatus: "in-progress",
            actionItems: ["Noteer huidreacties in uw bijwerkingendagboek."],
            jargon: []
        ),
        TimelineEventModel(
            id: "4",
            title: "Adjuvant trastuzumab + pertuzumab (1 jaar)",
            description: "IKNL: 18 cycli trastuzumab + pertuzumab. LVEF-monitoring q3m.",
            dateIso: "2025-01-20",
            phase: .current,
            fhirStatus: "in-progress",
            actionItems: ["Plan echocardiografie elke 3 maanden."],
            jargon: []
        ),
        TimelineEventModel(
            id: "5",
            title: "Jaarlijkse mammografie controle-plan",
            description: "IKNL: Jaarlijkse mammografie gedurende 5 jaar na behandeling.",
            dateIso: "2026-06-01",
            phase: .future,
            fhirStatus: "scheduled",
            actionItems: [],
            jargon: []
        ),
    ]
}
