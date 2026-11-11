import Foundation
import SwiftUI

struct SymptomNoteItem: Identifiable {
    let id: String
    var text: String
    var recordedAtLabel: String
}

@MainActor
final class SymptomLogModel: ObservableObject {
    @Published var notes: [SymptomNoteItem] = []

    var notesText: String {
        notes.reversed().map(\.text).joined(separator: "\n")
    }

    func append(_ text: String) {
        let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        notes.insert(
            SymptomNoteItem(id: UUID().uuidString, text: trimmed, recordedAtLabel: "Zojuist"),
            at: 0
        )
    }

    func replace(id: String, text: String) {
        let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty,
              let index = notes.firstIndex(where: { $0.id == id }) else { return }
        notes[index].text = trimmed
    }

    func delete(id: String) {
        notes.removeAll { $0.id == id }
    }

    func clear() {
        notes.removeAll()
    }
}
