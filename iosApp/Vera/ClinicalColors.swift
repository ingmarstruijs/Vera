import SwiftUI

/// Mirrors `DesignTokens` in `:core` (ARGB clinical palette) for dual-native parity.
enum ClinicalColors {
    /// DesignTokens.PRIMARY_GREEN 0xFF2F5D50
    static let primary = Color(red: 0.184, green: 0.365, blue: 0.314)
    /// DesignTokens.PRIMARY_CONTAINER 0xFFE2EEE9
    static let primaryContainer = Color(red: 0.886, green: 0.933, blue: 0.914)
    /// DesignTokens.SURFACE_LIGHT 0xFFF3F6F5
    static let surface = Color(red: 0.953, green: 0.965, blue: 0.961)
    /// DesignTokens.TEXT_DARK 0xFF1A2421
    static let text = Color(red: 0.102, green: 0.141, blue: 0.129)
    /// DesignTokens.TEXT_SECONDARY 0xFF4A5A55
    static let textSecondary = Color(red: 0.290, green: 0.353, blue: 0.333)
    /// DesignTokens.SENTINEL_GREEN 0xFF2A6B4E
    static let sentinel = Color(red: 0.165, green: 0.420, blue: 0.306)
    /// DesignTokens.STATUS_COMPLETED 0xFF1E4D7B
    static let statusCompleted = Color(red: 0.118, green: 0.302, blue: 0.482)
    /// DesignTokens.STATUS_IN_PROGRESS 0xFF2F5D50
    static let statusInProgress = primary
    /// DesignTokens.STATUS_SCHEDULED 0xFFB86E00
    static let statusScheduled = Color(red: 0.722, green: 0.431, blue: 0.0)
    /// DesignTokens.ERROR_RED 0xFFB3261E
    static let error = Color(red: 0.702, green: 0.149, blue: 0.118)
    /// DesignTokens.AMBER 0xFFB86E00
    static let amber = statusScheduled

    static func status(_ fhirStatus: String?) -> Color {
        switch fhirStatus {
        case "active", "in-progress": return statusInProgress
        case "completed": return statusCompleted
        case "scheduled", "booked": return statusScheduled
        case "cancelled", "entered-in-error": return error
        default: return textSecondary
        }
    }
}
