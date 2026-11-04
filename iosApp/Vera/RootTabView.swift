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
                DecoderView()
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
                AppointmentsView()
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

#Preview {
    RootTabView()
}
