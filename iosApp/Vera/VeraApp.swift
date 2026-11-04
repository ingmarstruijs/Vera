import SwiftUI

@main
struct VeraApp: App {
    @StateObject private var auth = AuthCoordinator()
    @State private var unlocked = false

    var body: some Scene {
        WindowGroup {
            Group {
                if unlocked || auth.step == .unlocked {
                    RootTabView()
                } else {
                    AuthGateView(coordinator: auth) {
                        unlocked = true
                    }
                }
            }
        }
    }
}
