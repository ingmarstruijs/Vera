// Shared by :androidApp.
// Override from CI: -PveraVersionCode=N -PveraVersionName=X.Y.Z
//
// Convention (see docs/RELEASING.md):
//   preview (main / workflow_dispatch): versionName = 0.1.<N>, versionCode = N (CI run)
//     GitHub tag must also be 0.1.<N> (Obtainium uses tag_name as version).
//   milestone tag v0.1.0-android.N:     versionName from tag, versionCode = CI run
//   production tag vX.Y.Z:              versionName = X.Y.Z, versionCode = CI run
// Local defaults keep installs working without CI props.
//
// Preview uses 0.1.<N> (not 0.1.0-dev.<N>) so Obtainium version detection
// compares cleanly without stripping a SemVer pre-release suffix.

extra["veraVersionCode"] = (findProperty("veraVersionCode") as String?)?.toIntOrNull() ?: 1
extra["veraVersionName"] = (findProperty("veraVersionName") as String?) ?: "0.1.0"
