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

struct AppointmentsView: View {
    @StateObject private var speech = SpeechCoordinator()
    @State private var selectedId: String?
    @State private var pocketGuide: String?
    @State private var appointments = AppointmentMockData.items

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

                if let pocketGuide {
                    Divider().padding(.vertical, 4)
                    HStack {
                        Label("Uw Pocket Gids", systemImage: "book")
                            .font(.title3.weight(.semibold))
                        Spacer()
                        Button {
                            speech.speak(pocketGuide)
                        } label: {
                            Image(systemName: "speaker.wave.2")
                        }
                        .accessibilityLabel("Voorlezen")
                    }
                    Text(pocketGuide)
                        .font(.caption)
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

    private func buildGuide(_ appt: AppointmentItem) -> String {
        let symptoms = speech.transcript.isEmpty
            ? "(geen symptomen geregistreerd – gebruik de spraakfunctie)"
            : speech.transcript
        return """
        # Pocket Gids voor uw afspraak
        **Datum:** \(appt.startIso)
        **Type:** \(appt.serviceType)
        **Locatie:** \(appt.location)

        ## Reden van bezoek
        \(appt.reason.isEmpty ? "—" : appt.reason)

        ## Uw ingesproken symptomen
        \(symptoms)

        ## Aanbevolen vragen voor uw arts
        * Wat zijn de resultaten van mijn laatste LVEF-meting?
        * Zijn er bijwerkingen die ik moet melden?
        * Wanneer verwacht u de volgende behandelstap?

        ## Memo
        \(appt.comment.isEmpty ? "—" : appt.comment)

        ---
        *Gegenereerd door Vera – IKNL Richtlijn Borstkanker 2023*
        """
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
