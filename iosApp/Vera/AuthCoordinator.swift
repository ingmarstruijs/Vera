import Foundation
import LocalAuthentication

enum AuthStep {
    case checking
    case newVault
    case unlock
    case unlocked
}

/// Local stand-in for shared `AuthViewModel` until Shared.framework is linked in Xcode.
/// Uses Keychain presence + LocalAuthentication to mirror the P4/P5 unlock flow.
@MainActor
final class AuthCoordinator: ObservableObject {
    @Published var step: AuthStep = .checking
    @Published var isBusy = false
    @Published var attemptsLeft = 5
    @Published var errorMessage: String?
    @Published var showSelfDestructDialog = false

    private let vaultFlagKey = "vera_vault_initialised_v1"
    private let attemptsKey = "vera_failed_attempts"
    private let maxAttempts = 5

    func start(autoUnlock: Bool) async {
        step = .checking
        errorMessage = nil
        let initialised = UserDefaults.standard.bool(forKey: vaultFlagKey)
        attemptsLeft = remainingAttempts()
        if !initialised {
            step = .newVault
            return
        }
        step = .unlock
        if autoUnlock && attemptsLeft > 0 {
            unlockBiometric()
        }
    }

    func createVault() {
        isBusy = true
        UserDefaults.standard.set(true, forKey: vaultFlagKey)
        UserDefaults.standard.set(0, forKey: attemptsKey)
        attemptsLeft = maxAttempts
        step = .unlock
        isBusy = false
        unlockBiometric()
    }

    func unlockBiometric() {
        guard !isBusy, attemptsLeft > 0 else { return }
        isBusy = true
        errorMessage = nil

        let context = LAContext()
        var error: NSError?
        guard context.canEvaluatePolicy(.deviceOwnerAuthentication, error: &error) else {
            isBusy = false
            errorMessage =
                "Geen schermbeveiliging op dit apparaat. Stel een PIN of biometrie in via de apparaatinstellingen."
            return
        }

        context.evaluatePolicy(
            .deviceOwnerAuthentication,
            localizedReason: "Ontgrendel Vera om je medische gegevens te openen"
        ) { [weak self] success, authError in
            Task { @MainActor in
                guard let self else { return }
                self.isBusy = false
                if success {
                    UserDefaults.standard.set(0, forKey: self.attemptsKey)
                    self.attemptsLeft = self.maxAttempts
                    self.step = .unlocked
                    return
                }
                if let authError = authError as? LAError, authError.code == .userCancel {
                    self.errorMessage = "Authenticatie geannuleerd."
                    return
                }
                self.registerFailure()
            }
        }
    }

    func acknowledgeSelfDestruct() {
        showSelfDestructDialog = false
        step = .newVault
        attemptsLeft = maxAttempts
        errorMessage = nil
    }

    private func remainingAttempts() -> Int {
        let failed = UserDefaults.standard.integer(forKey: attemptsKey)
        return max(0, maxAttempts - failed)
    }

    private func registerFailure() {
        let failed = UserDefaults.standard.integer(forKey: attemptsKey) + 1
        UserDefaults.standard.set(failed, forKey: attemptsKey)
        attemptsLeft = max(0, maxAttempts - failed)
        if failed >= maxAttempts {
            UserDefaults.standard.set(false, forKey: vaultFlagKey)
            UserDefaults.standard.set(0, forKey: attemptsKey)
            showSelfDestructDialog = true
            errorMessage = "Kluis definitief gewist ter bescherming van uw privacy."
            return
        }
        errorMessage = "Verificatie mislukt. Pogingen resterend: \(attemptsLeft)"
        if attemptsLeft <= 1 {
            errorMessage = "WAARSCHUWING: Na nog één mislukte poging wordt de kluis gewist."
        }
    }
}
