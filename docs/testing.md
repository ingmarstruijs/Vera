# Testing

Automated tests live in KMP `:core` / `:shared` (`commonTest`). UI is Compose in `:ui` (lint via `:androidApp`).

## Run locally

```bash
./gradlew :core:testDebugUnitTest :shared:testDebugUnitTest
./gradlew :core:detekt :shared:detekt :ui:detekt :androidApp:detekt
./gradlew :androidApp:lintDebug
./gradlew :ui:assembleDebug :androidApp:assembleDebug
```

CI: [`.github/workflows/kmp-smoke.yml`](../.github/workflows/kmp-smoke.yml) on `pull_request` / `push` → `main` (+ `workflow_dispatch`). Signing not required — [setup/signing.md](setup/signing.md). Changelog: agent rule (no bot) — [contributing.md](contributing.md).

## Coverage

| Area | Location |
|------|----------|
| Domain / i18n / nav | `core/src/commonTest` |
| Features / ViewModels | `shared/src/commonTest` |
| Detekt | `config/detekt/` |
| Android a11y-minded lint | `:androidApp:lintDebug` |

## iOS

No macOS CI job yet. CMP iOS host is pending — [setup/ios.md](setup/ios.md).
