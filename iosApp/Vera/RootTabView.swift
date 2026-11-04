import SwiftUI

/// Placeholder shell: Timeline | Decoder | Appointments (no Sync).
struct RootTabView: View {
    var body: some View {
        TabView {
            PlaceholderScreen(
                title: "Timeline",
                message: "Blueprint timeline placeholder. Feature port starts in later phases."
            )
            .tabItem {
                Label("Timeline", systemImage: "calendar")
            }

            PlaceholderScreen(
                title: "Decoder",
                message: "Plain-language decoder placeholder. No jargon/ML wiring yet."
            )
            .tabItem {
                Label("Decoder", systemImage: "text.magnifyingglass")
            }

            PlaceholderScreen(
                title: "Appointments",
                message: "Appointment navigator placeholder. No sync or P2P in v1."
            )
            .tabItem {
                Label("Appointments", systemImage: "clock")
            }
        }
    }
}

private struct PlaceholderScreen: View {
    let title: String
    let message: String

    var body: some View {
        VStack(spacing: 12) {
            Text("Vera")
                .font(.largeTitle.weight(.semibold))
            Text(title)
                .font(.title2)
            Text(message)
                .font(.body)
                .multilineTextAlignment(.center)
                .foregroundStyle(.secondary)
                .padding(.horizontal)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

#Preview {
    RootTabView()
}
