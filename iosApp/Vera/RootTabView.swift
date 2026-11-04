import SwiftUI

/// Shell: Timeline | Decoder | Appointments (no Sync) + privacy sentinel.
struct RootTabView: View {
    var body: some View {
        TabView {
            NavigationStack {
                BlueprintView()
                    .navigationTitle("Vera")
                    .toolbar {
                        ToolbarItem(placement: .topBarTrailing) {
                            PrivacySentinelDot()
                        }
                    }
            }
            .tabItem {
                Label("Tijdlijn", systemImage: "calendar")
            }

            NavigationStack {
                PlaceholderScreen(
                    title: "Decoder",
                    message: "Medische vaktaal-decoder volgt in een latere fase."
                )
                .navigationTitle("Vera")
                .toolbar {
                    ToolbarItem(placement: .topBarTrailing) {
                        PrivacySentinelDot()
                    }
                }
            }
            .tabItem {
                Label("Decoder", systemImage: "text.magnifyingglass")
            }

            NavigationStack {
                PlaceholderScreen(
                    title: "Afspraken",
                    message: "Afspraaknavigator volgt in een latere fase."
                )
                .navigationTitle("Vera")
                .toolbar {
                    ToolbarItem(placement: .topBarTrailing) {
                        PrivacySentinelDot()
                    }
                }
            }
            .tabItem {
                Label("Afspraken", systemImage: "clock")
            }
        }
        .tint(Color(red: 0.11, green: 0.37, blue: 0.13))
    }
}

private struct PrivacySentinelDot: View {
    var body: some View {
        Circle()
            .fill(Color(red: 0.11, green: 0.48, blue: 0.24))
            .frame(width: 12, height: 12)
            .accessibilityLabel("Gegevens lokaal beveiligd. Geen medische gegevens hebben uw apparaat verlaten.")
            .frame(width: 44, height: 44)
    }
}

private struct PlaceholderScreen: View {
    let title: String
    let message: String

    var body: some View {
        VStack(spacing: 12) {
            Text(title).font(.title2.weight(.semibold))
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
