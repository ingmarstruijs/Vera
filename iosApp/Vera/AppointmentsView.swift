import SwiftUI

struct AppointmentItem: Identifiable {
    let id: String
    let serviceType: String
    let startIso: String
    let location: String
    let practitioner: String
    let reason: String
    let comment: String
}

struct PocketGuideModel {
    let appointmentId: String
    let title: String
    let date: String
    let serviceType: String
    let location: String
    let reason: String
    let symptoms: String
    let questions: [String]
    let memo: String
    let footer: String
    var extraNotes: String

    func plainText() -> String {
        var lines: [String] = [
            title,
            "Datum: \(date)",
            "Type: \(serviceType)",
            "Locatie: \(location)",
            "",
            "Reden van bezoek",
            reason,
            "",
            "Uw ingesproken symptomen",
            symptoms,
            "",
            "Aanbevolen vragen voor uw arts",
        ]
        questions.forEach { lines.append("• \($0)") }
        lines.append("")
        lines.append("Memo")
        lines.append(memo)
        if !extraNotes.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            lines.append("")
            lines.append("Uw extra notities")
            lines.append(extraNotes)
        }
        lines.append("")
        lines.append(footer)
        return lines.joined(separator: "\n")
    }
}

struct AppointmentsView: View {
    @StateObject private var speech = SpeechCoordinator()
    @State private var selectedId: String?
    @State private var pocketGuide: PocketGuideModel?
    @State private var showFullGuide = false
    @State private var appointments = AppointmentMockData.items

    private let defaultQuestions = [
        "Wat zijn de resultaten van mijn laatste LVEF-meting?",
        "Zijn er bijwerkingen die ik moet melden?",
        "Wanneer verwacht u de volgende behandelstap?",
        "Wat kan ik zelf doen om mijn herstel te bevorderen?",
        "Zijn er richtlijnwijzigingen die invloed hebben op mijn zorgplan?",
    ]

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                Text("Afspraak Navigator")
                    .font(.title2.weight(.bold))
                Text("Uw gepersonaliseerde afsprakengids")
                    .font(.body)
                    .foregroundStyle(.secondary)

                ForEach(appointments) { appt in
                    appointmentCard(appt)
                }

                Divider().padding(.vertical, 4)
                Text("Symptomen registreren").font(.title3.weight(.semibold))

                Button {
                    speech.toggleListening()
                } label: {
                    Label(
                        speech.isListening ? "Stop opname" : "Symptomen inspreken",
                        systemImage: speech.isListening ? "stop.fill" : "mic"
                    )
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                }
                .buttonStyle(.borderedProminent)
                .tint(speech.isListening ? .red : Color(red: 0.11, green: 0.37, blue: 0.13))
                .disabled(!speech.available && !speech.isListening)

                if speech.isListening {
                    Text("Luistert…")
                        .font(.caption)
                        .foregroundStyle(.red)
                }

                if !speech.available {
                    Text("Spraakregistratie vereist microfoon- en spraakrechten.")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }

                if !speech.transcript.isEmpty {
                    VStack(alignment: .leading, spacing: 6) {
                        HStack {
                            Text("Geregistreerde symptomen:").font(.caption.weight(.semibold))
                            Spacer()
                            Button("Wissen") { speech.clearTranscript() }
                        }
                        Text(speech.transcript).font(.caption)
                    }
                    .padding(12)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(RoundedRectangle(cornerRadius: 10).fill(Color(.secondarySystemBackground)))
                }

                if let binding = pocketGuideBinding {
                    Divider().padding(.vertical, 4)
                    HStack {
                        Label("Uw Pocket Gids", systemImage: "book")
                            .font(.title3.weight(.semibold))
                        Spacer()
                        Button {
                            showFullGuide = true
                        } label: {
                            Image(systemName: "arrow.up.left.and.arrow.down.right")
                        }
                        .accessibilityLabel("Volledig scherm")
                        Button {
                            speech.speak(binding.wrappedValue.plainText())
                        } label: {
                            Image(systemName: "speaker.wave.2")
                        }
                        .accessibilityLabel("Voorlezen")
                    }
                    pocketGuideContent(binding)
                        .padding(16)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color.secondary.opacity(0.3), lineWidth: 1)
                        )
                }
            }
            .padding(20)
        }
        .onAppear { speech.prepare() }
        .sheet(isPresented: $showFullGuide) {
            NavigationStack {
                ScrollView {
                    if let binding = pocketGuideBinding {
                        pocketGuideContent(binding)
                            .padding(20)
                    }
                }
                .navigationTitle("Uw Pocket Gids")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .topBarTrailing) {
                        Button("Sluiten") { showFullGuide = false }
                    }
                    ToolbarItem(placement: .topBarLeading) {
                        if let guide = pocketGuide {
                            Button {
                                speech.speak(guide.plainText())
                            } label: {
                                Image(systemName: "speaker.wave.2")
                            }
                            .accessibilityLabel("Voorlezen")
                        }
                    }
                }
            }
        }
    }

    private var pocketGuideBinding: Binding<PocketGuideModel>? {
        guard pocketGuide != nil else { return nil }
        return Binding(
            get: { pocketGuide! },
            set: { pocketGuide = $0 }
        )
    }

    @ViewBuilder
    private func pocketGuideContent(_ guide: Binding<PocketGuideModel>) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(guide.wrappedValue.title)
                .font(.title3.weight(.bold))

            metaRow("Datum", guide.wrappedValue.date)
            metaRow("Type", guide.wrappedValue.serviceType)
            metaRow("Locatie", guide.wrappedValue.location)

            section("Reden van bezoek", guide.wrappedValue.reason)
            section("Uw ingesproken symptomen", guide.wrappedValue.symptoms)

            Text("Aanbevolen vragen voor uw arts")
                .font(.subheadline.weight(.semibold))
            ForEach(guide.wrappedValue.questions, id: \.self) { question in
                Text("• \(question)")
                    .font(.body)
            }

            section("Memo", guide.wrappedValue.memo)

            Text("Uw extra notities")
                .font(.subheadline.weight(.semibold))
            TextField(
                "Schrijf hier wat u nog wilt meenemen naar de afspraak…",
                text: guide.extraNotes,
                axis: .vertical
            )
            .lineLimit(3...8)
            .textFieldStyle(.roundedBorder)

            Text(guide.wrappedValue.footer)
                .font(.caption2)
                .foregroundStyle(.secondary)
        }
    }

    @ViewBuilder
    private func metaRow(_ label: String, _ value: String) -> some View {
        if !value.isEmpty {
            HStack(alignment: .top, spacing: 4) {
                Text("\(label):")
                    .font(.body.weight(.semibold))
                Text(value).font(.body)
            }
        }
    }

    @ViewBuilder
    private func section(_ title: String, _ body: String) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title).font(.subheadline.weight(.semibold))
            Text(body).font(.body)
        }
    }

    @ViewBuilder
    private func appointmentCard(_ appt: AppointmentItem) -> some View {
        let selected = selectedId == appt.id
        VStack(alignment: .leading, spacing: 8) {
            Button {
                selectedId = appt.id
                pocketGuide = nil
            } label: {
                VStack(alignment: .leading, spacing: 6) {
                    Label(appt.serviceType, systemImage: "calendar")
                        .font(.headline)
                    Text(appt.startIso)
                        .font(.subheadline.weight(.bold))
                        .foregroundStyle(Color(red: 0.11, green: 0.37, blue: 0.13))
                    if !appt.location.isEmpty {
                        Text(appt.location).font(.caption).foregroundStyle(.secondary)
                    }
                    if !appt.practitioner.isEmpty {
                        Text(appt.practitioner).font(.caption).foregroundStyle(.secondary)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .buttonStyle(.plain)

            if selected {
                Button {
                    pocketGuide = buildGuide(appt)
                } label: {
                    Label("Genereer Pocket Gids", systemImage: "sparkles")
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 10)
                }
                .buttonStyle(.borderedProminent)
                .tint(Color(red: 0.11, green: 0.37, blue: 0.13))
            }
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .stroke(
                    selected ? Color(red: 0.11, green: 0.37, blue: 0.13) : Color.secondary.opacity(0.3),
                    lineWidth: selected ? 2 : 1
                )
        )
    }

    private func buildGuide(_ appt: AppointmentItem) -> PocketGuideModel {
        let symptoms = speech.transcript.isEmpty
            ? "(geen symptomen geregistreerd – gebruik de spraakfunctie)"
            : speech.transcript
        let preservedNotes = pocketGuide?.appointmentId == appt.id
            ? (pocketGuide?.extraNotes ?? "")
            : ""
        return PocketGuideModel(
            appointmentId: appt.id,
            title: "Pocket Gids voor uw afspraak",
            date: appt.startIso,
            serviceType: appt.serviceType,
            location: appt.location,
            reason: appt.reason.isEmpty ? "—" : appt.reason,
            symptoms: symptoms,
            questions: defaultQuestions,
            memo: appt.comment.isEmpty ? "—" : appt.comment,
            footer: "Gegenereerd door Vera – IKNL Richtlijn Borstkanker 2023",
            extraNotes: preservedNotes
        )
    }
}

enum AppointmentMockData {
    static let items: [AppointmentItem] = [
        AppointmentItem(
            id: "appt-001",
            serviceType: "Poliklinisch consult medisch oncoloog",
            startIso: "2025-04-08T10:30:00+02:00",
            location: "UMC Utrecht – Afdeling Oncologie, Polikliniek 3B",
            practitioner: "Dr. A. de Vries – Medisch oncoloog",
            reason: "Tussentijdse evaluatie radiotherapie + trastuzumab bijwerkingen",
            comment: "Meenemen: bijwerkingendagboek, lijst met huidige medicatie."
        ),
        AppointmentItem(
            id: "appt-002",
            serviceType: "Echocardiografie – LVEF-meting",
            startIso: "2025-04-22T09:00:00+02:00",
            location: "UMC Utrecht – Afdeling Cardiologie",
            practitioner: "Afdeling Cardiologie",
            reason: "LVEF-monitoring trastuzumab: cardiotoxiciteitscontrole q3m",
            comment: ""
        ),
        AppointmentItem(
            id: "appt-003",
            serviceType: "Nacontrole radiotherapie",
            startIso: "2025-04-25T14:00:00+02:00",
            location: "UMC Utrecht – Afdeling Radiotherapie",
            practitioner: "Dr. M. Peters – Radiotherapeut-oncoloog",
            reason: "Afsluiting radiotherapie – beoordeling huidreactie",
            comment: ""
        ),
    ]
}
