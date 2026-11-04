import SwiftUI

/// Auth gate mirroring shared AuthViewModel flow (new vault → biometric unlock).
/// Until the Shared XCFramework is linked in Xcode, this uses a local coordinator
/// with the same steps; wire to `AuthViewModel` when the framework is embedded.
struct AuthGateView: View {
    @ObservedObject var coordinator: AuthCoordinator
    var onUnlocked: () -> Void

    var body: some View {
        Group {
            switch coordinator.step {
            case .checking:
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            case .newVault:
                NewVaultView(isBusy: coordinator.isBusy, onCreate: coordinator.createVault)
            case .unlock:
                UnlockView(
                    isBusy: coordinator.isBusy,
                    attemptsLeft: coordinator.attemptsLeft,
                    errorMessage: coordinator.errorMessage,
                    onUnlock: coordinator.unlockBiometric
                )
            case .unlocked:
                Color.clear.onAppear(perform: onUnlocked)
            }
        }
        .padding(24)
        .alert("Kluis gewist", isPresented: $coordinator.showSelfDestructDialog) {
            Button("OK") { coordinator.acknowledgeSelfDestruct() }
        } message: {
            Text("Kluis definitief gewist ter bescherming van uw privacy.")
        }
        .task {
            await coordinator.start(autoUnlock: true)
        }
    }
}

private struct NewVaultView: View {
    let isBusy: Bool
    let onCreate: () -> Void

    var body: some View {
        VStack(spacing: 24) {
            Image(systemName: "lock")
                .font(.system(size: 64))
                .foregroundStyle(Color(red: 0.11, green: 0.37, blue: 0.13))
            Text("Kluis aanmaken")
                .font(.title2.weight(.bold))
                .multilineTextAlignment(.center)
                .accessibilityAddTraits(.isHeader)
            Text(
                "Uw medische gegevens worden versleuteld opgeslagen met AES-256. " +
                "Stel een PIN of biometrie in als extra beveiligingslaag."
            )
            .font(.body)
            .multilineTextAlignment(.center)
            .foregroundStyle(.secondary)
            .fixedSize(horizontal: false, vertical: true)
            Button(action: onCreate) {
                Label(
                    isBusy ? "Bezig…" : "Beveiligde kluis aanmaken",
                    systemImage: "shield"
                )
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
            }
            .buttonStyle(.borderedProminent)
            .tint(Color(red: 0.18, green: 0.36, blue: 0.31))
            .disabled(isBusy)
            .accessibilityLabel(isBusy ? "Bezig" : "Beveiligde kluis aanmaken")
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .dynamicTypeSize(...DynamicTypeSize.accessibility3)
    }
}

private struct UnlockView: View {
    let isBusy: Bool
    let attemptsLeft: Int
    let errorMessage: String?
    let onUnlock: () -> Void

    var body: some View {
        VStack(spacing: 24) {
            Image(systemName: "touchid")
                .font(.system(size: 72))
                .foregroundStyle(Color(red: 0.18, green: 0.36, blue: 0.31).opacity(isBusy ? 0.6 : 1))
                .accessibilityHidden(true)
            Text("Ontgrendel Vera")
                .font(.title2.weight(.bold))
                .accessibilityAddTraits(.isHeader)
            Text("Gebruik uw biometrische gegevens om uw Vera kluis te openen")
                .font(.body)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
                .fixedSize(horizontal: false, vertical: true)
            if let errorMessage {
                HStack(alignment: .top, spacing: 8) {
                    Image(systemName: attemptsLeft <= 1 ? "exclamationmark.triangle" : "info.circle")
                    Text(errorMessage)
                        .font(.footnote)
                }
                .padding(12)
                .background(
                    RoundedRectangle(cornerRadius: 12)
                        .fill(attemptsLeft <= 1 ? Color.red.opacity(0.08) : Color.orange.opacity(0.12))
                )
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(attemptsLeft <= 1 ? Color.red : Color.orange, lineWidth: 1)
                )
            }
            Button(action: onUnlock) {
                Label(isBusy ? "Bezig…" : "Ontgrendelen", systemImage: "touchid")
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
            }
            .buttonStyle(.borderedProminent)
            .tint(Color(red: 0.18, green: 0.36, blue: 0.31))
            .disabled(isBusy || attemptsLeft <= 0)
            .accessibilityLabel(isBusy ? "Bezig" : "Ontgrendelen")
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .dynamicTypeSize(...DynamicTypeSize.accessibility3)
    }
}
