# CI / Obtainium signing

## Why this keystore exists

GitHub Actions runners are ephemeral. If release builds fall back to the Android
**debug** keystore, each run generates a **new certificate fingerprint**. Obtainium
(and Android PackageManager) then treat every APK as a different app and refuse
in-place updates.

`obtainium.keystore` is a **stable preview signing key** committed to the repo so
every `0.1.<N>` APK shares one fingerprint.

| Item | Value |
| --- | --- |
| File | `ci/obtainium.keystore` |
| Alias | `vera_obtainium` |
| Store / key password | `vera-obtainium` |
| SHA-256 | `21:1A:66:75:E1:92:2F:75:DF:83:2A:78:FF:DD:4C:04:9B:D7:00:68:E9:4F:DE:31:3B:39:21:64:F2:67:0A:98` |

## Precedence

1. `keystore.properties` + `ANDROID_KEYSTORE_*` secrets (project / production key)
2. This Obtainium preview key
3. Debug (local only; CI should never publish that)

## Security

Anyone with the repo can resign preview APKs with this key. That is intentional for
early Obtainium testing. **Do not** reuse this key for Play Store or patient-facing
production builds — set `ANDROID_KEYSTORE_*` secrets for those.
