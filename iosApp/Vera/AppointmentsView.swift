import SwiftUI

struct AppointmentItem: Identifiable {
    let id: String
    let serviceType: String
    let startIso: String
    let location: String
    let practitioner: String
    let phone: String
    let reason: String
    let comment: String

    var startDisplay: String { DateDisplay.formatIso(startIso) }
}

struct SymptomNoteItem: Identifiable {
    let id: String
    var text: String
}

struct CareContactItem: Identifiable {
    let id: String
    let name: String
    let role: String
    let organization: String
    let phone: String
}

struct PocketGuideModel: Identifiable {
    var id: String { appointmentId }
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
    var isSaved: Bool = false

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

enum DateDisplay {
    static func formatIso(_ iso: String) -> String {
        let trimmed = iso.trimmingCharacters(in: .whitespacesAndNewlines)
        guard trimmed.count >= 16 else { return trimmed }
        let months = ["jan", "feb", "mrt", "apr", "mei", "jun", "jul", "aug", "sep", "okt", "nov", "dec"]
        let year = String(trimmed.prefix(4))
        guard let month = Int(trimmed.dropFirst(5).prefix(2)),
              let day = Int(trimmed.dropFirst(8).prefix(2)),
              month >= 1, month <= 12 else { return trimmed }
        let hour = String(trimmed.dropFirst(11).prefix(2))
        let minute = String(trimmed.dropFirst(14).prefix(2))
        return "\(day) \(months[month - 1]) \(year) · \(hour):\(minute)"
    }
}

struct AppointmentsView: View {
    @StateObject private var speech = SpeechCoordinator()
    @State private var selectedId: String?
    @State private var pocketGuide: PocketGuideModel?
    @State private var showFullGuide = false
    @State private var guideJustSaved = false
    @State private var savedGuides: [PocketGuideModel] = []
    @State private var appointments = AppointmentMockData.items
    @State private var symptomNotes: [SymptomNoteItem] = []
    @State private var reRecordId: String?
    @State private var contacts = AppointmentMockData.contacts

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
                ForEach(appointments) { appt in
                    appointmentCard(appt)
                }

                if !savedGuides.isEmpty {
                    Divider().padding(.vertical, 4)
                    Text("Opgeslagen pocket gidsen").font(.title3.weight(.semibold))
                    ForEach(savedGuides) { guide in
                        Button {
                            pocketGuide = guide
                            showFullGuide = true
                            guideJustSaved = false
                        } label: {
                            HStack {
                                Label(guide.serviceType, systemImage: "book")
                                Spacer()
                                Text(guide.date).font(.caption).foregroundStyle(.secondary)
                            }
                            .padding(12)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(Color.secondary.opacity(0.3), lineWidth: 1)
                            )
                        }
                        .buttonStyle(.plain)
                    }
                }

                Divider().padding(.vertical, 4)
                Text("Symptomen registreren").font(.title3.weight(.semibold))

                Button {
                    toggleVoice()
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
                    if !speech.transcript.isEmpty {
                        Text(speech.transcript).font(.caption).foregroundStyle(.secondary)
                    }
                }

                if !speech.available {
                    Text("Spraakregistratie vereist microfoon- en spraakrechten.")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }

                if !symptomNotes.isEmpty {
                    HStack {
                        Text("Geregistreerde symptomen:").font(.caption.weight(.semibold))
                        Spacer()
                        Button("Voorlezen") {
                            speech.speak(symptomNotes.map(\.text).joined(separator: "\n"))
                        }
                        Button("Wissen") { symptomNotes.removeAll() }
                    }
                    ForEach(symptomNotes) { note in
                        HStack(alignment: .top) {
                            Text(note.text).font(.caption)
                            Spacer()
                            Button {
                                speech.speak(note.text)
                            } label: {
                                Image(systemName: "speaker.wave.2")
                            }
                            .accessibilityLabel("Voorlezen")
                            Button {
                                reRecordId = note.id
                                if !speech.isListening { speech.toggleListening() }
                            } label: {
                                Image(systemName: "arrow.clockwise")
                            }
                            .accessibilityLabel("Opnieuw inspelen")
                            Button(role: .destructive) {
                                symptomNotes.removeAll { $0.id == note.id }
                            } label: {
                                Image(systemName: "trash")
                            }
                            .accessibilityLabel("Verwijderen")
                        }
                        .padding(10)
                        .background(RoundedRectangle(cornerRadius: 10).fill(Color(.secondarySystemBackground)))
                    }
                }

                Divider().padding(.vertical, 4)
                Text("Wie bel ik?").font(.title3.weight(.semibold))
                Text("Praktijk, afdeling en arts – telefoonnummers bij de hand")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                ForEach(contacts) { contact in
                    contactCard(contact)
                }

            }
            .padding(20)
        }
        .onAppear { speech.prepare() }
        .onChange(of: speech.isListening) { _, listening in
            if !listening {
                commitTranscript()
            }
        }
        .sheet(isPresented: $showFullGuide) {
            NavigationStack {
                VStack(spacing: 0) {
                    ScrollView {
                        if let binding = pocketGuideBinding {
                            pocketGuideContent(binding)
                                .padding(20)
                        }
                    }
                    Divider()
                    HStack {
                        if guideJustSaved || (pocketGuide?.isSaved == true) {
                            Text("Opgeslagen")
                                .foregroundStyle(Color(red: 0.11, green: 0.37, blue: 0.13))
                        }
                        Spacer()
                        Button("Opslaan") { saveCurrentGuide() }
                            .buttonStyle(.borderedProminent)
                            .tint(Color(red: 0.11, green: 0.37, blue: 0.13))
                    }
                    .padding(16)
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

    private func saveCurrentGuide() {
        guard var guide = pocketGuide else { return }
        guide.isSaved = true
        pocketGuide = guide
        if let index = savedGuides.firstIndex(where: { $0.appointmentId == guide.appointmentId }) {
            savedGuides[index] = guide
        } else {
            savedGuides.append(guide)
        }
        guideJustSaved = true
    }

    private var pocketGuideBinding: Binding<PocketGuideModel>? {
        guard pocketGuide != nil else { return nil }
        return Binding(
            get: { pocketGuide! },
            set: { pocketGuide = $0 }
        )
    }

    private func toggleVoice() {
        if speech.isListening {
            speech.toggleListening()
        } else {
            reRecordId = nil
            speech.clearTranscript()
            speech.toggleListening()
        }
    }

    private func commitTranscript() {
        let text = speech.transcript.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !text.isEmpty else {
            reRecordId = nil
            return
        }
        if let replaceId = reRecordId,
           let index = symptomNotes.firstIndex(where: { $0.id == replaceId }) {
            symptomNotes[index].text = text
        } else {
            symptomNotes.append(SymptomNoteItem(id: UUID().uuidString, text: text))
        }
        reRecordId = nil
        speech.clearTranscript()
    }

    @ViewBuilder
    private func contactCard(_ contact: CareContactItem) -> some View {
        HStack(alignment: .top) {
            VStack(alignment: .leading, spacing: 2) {
                Text(contact.name).font(.headline)
                Text(contact.role).font(.caption).foregroundStyle(.secondary)
                Text(contact.organization).font(.caption2).foregroundStyle(.secondary)
                Text(contact.phone)
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(Color(red: 0.11, green: 0.37, blue: 0.13))
            }
            Spacer()
            if let url = URL(string: "tel:\(contact.phone.filter { $0.isNumber || $0 == "+" })") {
                Link(destination: url) {
                    Image(systemName: "phone")
                }
                .accessibilityLabel("Bellen")
            }
        }
        .padding(14)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color.secondary.opacity(0.3), lineWidth: 1)
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
                    Text(appt.startDisplay)
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
                if savedGuides.contains(where: { $0.appointmentId == appt.id }) {
                    Button {
                        if let saved = savedGuides.first(where: { $0.appointmentId == appt.id }) {
                            pocketGuide = saved
                            showFullGuide = true
                            guideJustSaved = false
                        }
                    } label: {
                        Label("Open pocket gids", systemImage: "book")
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 10)
                    }
                    .buttonStyle(.bordered)
                }
                Button {
                    pocketGuide = buildGuide(appt)
                    showFullGuide = true
                    guideJustSaved = false
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
        let symptoms = symptomNotes.isEmpty
            ? "(geen symptomen geregistreerd – gebruik de spraakfunctie)"
            : symptomNotes.map(\.text).joined(separator: "\n")
        let preservedNotes = savedGuides.first(where: { $0.appointmentId == appt.id })?.extraNotes
            ?? (pocketGuide?.appointmentId == appt.id ? (pocketGuide?.extraNotes ?? "") : "")
        return PocketGuideModel(
            appointmentId: appt.id,
            title: "Pocket Gids voor uw afspraak",
            date: appt.startDisplay,
            serviceType: appt.serviceType,
            location: appt.location,
            reason: appt.reason.isEmpty ? "—" : appt.reason,
            symptoms: symptoms,
            questions: defaultQuestions,
            memo: appt.comment.isEmpty ? "—" : appt.comment,
            footer: "Gegenereerd door Vera – IKNL Richtlijn Borstkanker 2023",
            extraNotes: preservedNotes,
            isSaved: false
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
            phone: "088 755 6303",
            reason: "Tussentijdse evaluatie radiotherapie + trastuzumab bijwerkingen",
            comment: "Meenemen: bijwerkingendagboek, lijst met huidige medicatie."
        ),
        AppointmentItem(
            id: "appt-002",
            serviceType: "Echocardiografie – LVEF-meting",
            startIso: "2025-04-22T09:00:00+02:00",
            location: "UMC Utrecht – Afdeling Cardiologie",
            practitioner: "Afdeling Cardiologie",
            phone: "088 755 5555",
            reason: "LVEF-monitoring trastuzumab: cardiotoxiciteitscontrole q3m",
            comment: ""
        ),
        AppointmentItem(
            id: "appt-003",
            serviceType: "Nacontrole radiotherapie",
            startIso: "2025-04-25T14:00:00+02:00",
            location: "UMC Utrecht – Afdeling Radiotherapie",
            practitioner: "Dr. M. Peters – Radiotherapeut-oncoloog",
            phone: "088 755 8800",
            reason: "Afsluiting radiotherapie – beoordeling huidreactie",
            comment: ""
        ),
    ]

    static let contacts: [CareContactItem] = [
        CareContactItem(
            id: "contact-gp",
            name: "Huisartsenpraktijk Utrecht Noord",
            role: "Huisarts",
            organization: "Eerste lijn",
            phone: "030 123 4567"
        ),
        CareContactItem(
            id: "contact-oncology-desk",
            name: "Polikliniek Oncologie",
            role: "Afdelingstelefoon",
            organization: "UMC Utrecht – Afdeling Oncologie",
            phone: "088 755 6300"
        ),
    ] + items.map {
        CareContactItem(
            id: "appt-contact-\($0.id)",
            name: $0.practitioner.isEmpty ? $0.serviceType : $0.practitioner,
            role: $0.serviceType,
            organization: $0.location,
            phone: $0.phone
        )
    }
}
