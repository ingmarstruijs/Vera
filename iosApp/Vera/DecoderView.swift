import SwiftUI

struct DecoderView: View {
    @State private var query = ""
    @State private var selectedLanguage = "nl"
    @State private var expandedTerm: String?
    @State private var languageExpanded = false

    private let languages = [
        ("nl", "Nederlands"),
        ("en", "Engels"),
        ("de", "Duits"),
        ("fr", "Frans"),
    ]

    private var filtered: [JargonItem] {
        if query.isEmpty { return DecoderGlossary.items }
        let q = query.lowercased()
        return DecoderGlossary.items.filter {
            $0.term.lowercased().contains(q) ||
                $0.explanation.lowercased().contains(q) ||
                $0.category.lowercased().contains(q)
        }
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                Text("Slim Systeem Decoder")
                    .font(.title2.weight(.bold))
                Text("Medische vaktaal vertaald naar begrijpelijk Nederlands")
                    .font(.body)
                    .foregroundStyle(.secondary)

                Text("Doeltaal")
                    .font(.subheadline.weight(.semibold))
                Picker("Doeltaal", selection: $selectedLanguage) {
                    ForEach(languages, id: \.0) { code, label in
                        Text(label).tag(code)
                    }
                }
                .pickerStyle(.menu)

                privacyNote

                if selectedLanguage != "nl" {
                    Text(
                        "Op-apparaat vertaling (ML Kit) is op iOS nog niet gekoppeld. " +
                        "De Nederlandse uitleg blijft beschikbaar; Android ondersteunt " +
                        "download + lokale vertaling."
                    )
                    .font(.caption)
                    .padding(12)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(RoundedRectangle(cornerRadius: 10).fill(Color.orange.opacity(0.12)))
                }

                TextField("Zoek medische term…", text: $query)
                    .textFieldStyle(.roundedBorder)
                    .accessibilityLabel("Zoek op medische term")

                if filtered.isEmpty {
                    Text("Geen termen gevonden voor deze zoekopdracht.")
                } else {
                    ForEach(filtered) { entry in
                        jargonCard(entry)
                    }
                }
            }
            .padding(20)
        }
    }

    private var privacyNote: some View {
        HStack(alignment: .top, spacing: 8) {
            Image(systemName: "shield")
                .foregroundStyle(Color(red: 0.11, green: 0.48, blue: 0.24))
            Text(
                "Vertaling vindt volledig op uw apparaat plaats. " +
                "Geen tekst wordt naar externe servers verstuurd."
            )
            .font(.caption)
            .foregroundStyle(Color(red: 0.11, green: 0.37, blue: 0.13))
        }
        .padding(12)
        .background(
            RoundedRectangle(cornerRadius: 10)
                .fill(Color(red: 0.91, green: 0.96, blue: 0.93))
                .overlay(
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(Color(red: 0.11, green: 0.48, blue: 0.24), lineWidth: 1)
                )
        )
    }

    @ViewBuilder
    private func jargonCard(_ entry: JargonItem) -> some View {
        let expanded = expandedTerm == entry.term
        VStack(alignment: .leading, spacing: 10) {
            Button {
                expandedTerm = expanded ? nil : entry.term
            } label: {
                HStack {
                    Text(entry.term)
                        .font(.headline)
                        .foregroundStyle(.primary)
                    Spacer()
                    if !entry.category.isEmpty {
                        Text(entry.category)
                            .font(.caption)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .background(Capsule().fill(Color.secondary.opacity(0.15)))
                    }
                    Image(systemName: expanded ? "chevron.up" : "chevron.down")
                }
            }
            .buttonStyle(.plain)

            if expanded {
                Text(entry.explanation).font(.body)
                HStack(alignment: .top, spacing: 8) {
                    Image(systemName: "checkmark.circle")
                        .foregroundStyle(Color(red: 0.11, green: 0.37, blue: 0.13))
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Actiepunt")
                            .font(.caption.weight(.semibold))
                            .foregroundStyle(Color(red: 0.11, green: 0.37, blue: 0.13))
                        Text(entry.actionItem).font(.caption)
                    }
                }
                .padding(12)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(
                    RoundedRectangle(cornerRadius: 10)
                        .fill(Color(red: 0.84, green: 0.95, blue: 0.86))
                )
            }
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color.secondary.opacity(0.3), lineWidth: 1)
        )
    }
}

struct JargonItem: Identifiable {
    var id: String { term }
    let term: String
    let explanation: String
    let actionItem: String
    let category: String
}

enum DecoderGlossary {
    static let items: [JargonItem] = [
        JargonItem(
            term: "HER2-positief",
            explanation: "Uw tumor maakt veel HER2-eiwit aan. Dit eiwit stimuleert de celgroei.",
            actionItem: "Vraag uw oncoloog of trastuzumab (Herceptin) onderdeel is van uw behandelplan.",
            category: "biomarker"
        ),
        JargonItem(
            term: "ER-negatief",
            explanation: "Uw tumor heeft geen oestrogeenreceptoren. Hormoontherapie is meestal niet effectief.",
            actionItem: "Bevestig met uw arts dat hormonale behandeling niet geïndiceerd is.",
            category: "biomarker"
        ),
        JargonItem(
            term: "Ki-67",
            explanation: "Ki-67 meet hoe snel tumorcellen zich delen. Hoog (≥25%) betekent snellere groei.",
            actionItem: "Vraag wat uw Ki-67-percentage betekent voor uw behandelkeuze.",
            category: "pathologie"
        ),
        JargonItem(
            term: "Hypofractionering",
            explanation: "Bestralingsschema met minder maar iets hogere doses per sessie.",
            actionItem: "Vraag hoeveel bestralingsafspraken u totaal heeft.",
            category: "radiotherapie"
        ),
        JargonItem(
            term: "LVEF (linker ventrikel ejectiefractie)",
            explanation: "Meet hoe goed uw hart bloed rondpompt; relevant bij trastuzumab.",
            actionItem: "Houd echocardiografie-afspraken bij tijdens trastuzumab-behandeling.",
            category: "cardiologie"
        ),
    ]
}
