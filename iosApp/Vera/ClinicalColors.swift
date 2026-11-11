import SwiftUI
import UIKit

/// Mirrors `DesignTokens` in `:core` (ARGB clinical palette) for dual-native parity.
/// Adaptive colors follow system dark mode while keeping Vera's clinical green brand.
enum ClinicalColors {
    /// DesignTokens.PRIMARY_GREEN / PRIMARY_GREEN_DARK
    static let primary = adaptive(
        light: (0.184, 0.365, 0.314),
        dark: (0.608, 0.769, 0.710)
    )
    /// DesignTokens.PRIMARY_CONTAINER / PRIMARY_CONTAINER_DARK
    static let primaryContainer = adaptive(
        light: (0.886, 0.933, 0.914),
        dark: (0.118, 0.247, 0.212)
    )
    /// DesignTokens.SURFACE_LIGHT / SURFACE_DARK
    static let surface = adaptive(
        light: (0.953, 0.965, 0.961),
        dark: (0.071, 0.098, 0.090)
    )
    /// DesignTokens.TEXT_DARK / TEXT_ON_DARK
    static let text = adaptive(
        light: (0.102, 0.141, 0.129),
        dark: (0.894, 0.925, 0.914)
    )
    /// DesignTokens.TEXT_SECONDARY / TEXT_SECONDARY_DARK
    static let textSecondary = adaptive(
        light: (0.290, 0.353, 0.333),
        dark: (0.718, 0.769, 0.749)
    )
    /// DesignTokens.SENTINEL_GREEN / SENTINEL_GREEN_DARK
    static let sentinel = adaptive(
        light: (0.165, 0.420, 0.306),
        dark: (0.498, 0.722, 0.604)
    )
    /// DesignTokens.SENTINEL_BG / SENTINEL_BG_DARK
    static let sentinelBackground = adaptive(
        light: (0.902, 0.949, 0.925),
        dark: (0.082, 0.157, 0.125)
    )
    /// DesignTokens.STATUS_COMPLETED / STATUS_COMPLETED_DARK
    static let statusCompleted = adaptive(
        light: (0.118, 0.302, 0.482),
        dark: (0.545, 0.706, 0.871)
    )
    /// DesignTokens.STATUS_IN_PROGRESS / STATUS_IN_PROGRESS_DARK
    static let statusInProgress = primary
    /// DesignTokens.STATUS_SCHEDULED / STATUS_SCHEDULED_DARK
    static let statusScheduled = adaptive(
        light: (0.722, 0.431, 0.0),
        dark: (0.878, 0.690, 0.416)
    )
    /// DesignTokens.ERROR_RED / ERROR_RED_DARK
    static let error = adaptive(
        light: (0.702, 0.149, 0.118),
        dark: (0.949, 0.722, 0.710)
    )
    /// DesignTokens.AMBER / AMBER_DARK
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

    private static func adaptive(
        light: (CGFloat, CGFloat, CGFloat),
        dark: (CGFloat, CGFloat, CGFloat)
    ) -> Color {
        Color(
            uiColor: UIColor { traits in
                let rgb = traits.userInterfaceStyle == .dark ? dark : light
                return UIColor(red: rgb.0, green: rgb.1, blue: rgb.2, alpha: 1)
            }
        )
    }
}
