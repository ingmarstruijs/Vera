import SwiftUI

/// Dedicated "Wie bel ik?" contacts screen (dial actions preserved).
struct ContactsView: View {
    @State private var contacts = AppointmentMockData.contacts

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                Text("Praktijk, afdeling en arts – telefoonnummers bij de hand")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)

                ForEach(contacts) { contact in
                    contactCard(contact)
                }
            }
            .padding(20)
        }
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
                    .foregroundStyle(ClinicalColors.primary)
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
}

#Preview {
    ContactsView()
}
