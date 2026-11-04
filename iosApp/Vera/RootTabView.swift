import SwiftUI

/// Shell IA: Care plan → Decoder → Appointments (unlock happens before this view).
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
                Label("Zorgplan", systemImage: "list.clipboard")
            }
            .accessibilityLabel("Zorgplan")

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
        // Dynamic Type: rely on semantic fonts; tint matches DesignTokens.PRIMARY_GREEN.
        .tint(ClinicalColors.primary)
        .dynamicTypeSize(...DynamicTypeSize.accessibility3)
    }
}

private struct PrivacySentinelDot: View {
    var body: some View {
        Circle()
            .fill(ClinicalColors.sentinel)
            .frame(width: 12, height: 12)
            .accessibilityLabel(
                "Gegevens lokaal beveiligd. Geen medische gegevens hebben uw apparaat verlaten."
            )
            .accessibilityAddTraits(.isStaticText)
            .frame(minWidth: 44, minHeight: 44)
            .contentShape(Rectangle())
    }
}

#Preview {
    RootTabView()
}
