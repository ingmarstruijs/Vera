# Vera documentation

English docs for the Kotlin Multiplatform migration.

| Path | Status |
|------|--------|
| [migration/kmp-plan.md](migration/kmp-plan.md) | In-repo mirror of the migration plan (v1 decisions) |
| `architecture/`, `setup/`, `security/`, `testing.md` | Coming in later docs phases |

The Flutter tree (`lib/`, `pubspec.yaml`, Flutter `android/` / `ios/` / `web/`) remains until cutover (P10). Prefer the Gradle modules below for new work:

- `shared/` — Kotlin Multiplatform business logic
- `androidApp/` — Jetpack Compose UI
- `iosApp/` — SwiftUI UI
