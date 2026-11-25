import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)
}

apply(from = rootProject.file("versioning.gradle.kts"))

val veraVersionCode: Int by extra
val veraVersionName: String by extra

val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("keystore.properties")
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

val releaseStoreFilePath = keystoreProperties.getProperty("storeFile").orEmpty()
val releaseStoreFile =
    if (releaseStoreFilePath.isNotEmpty()) rootProject.file(releaseStoreFilePath) else null
val hasReleaseKeystore =
    releaseStoreFile != null &&
        releaseStoreFile.isFile &&
        !keystoreProperties.getProperty("keyAlias").isNullOrBlank() &&
        !keystoreProperties.getProperty("storePassword").isNullOrBlank() &&
        !keystoreProperties.getProperty("keyPassword").isNullOrBlank()

android {
    namespace = "net.moonbaseone.vera"
    compileSdk = 35

    defaultConfig {
        applicationId = "net.moonbaseone.vera"
        minSdk = 26
        targetSdk = 35
        versionCode = veraVersionCode
        versionName = veraVersionName
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                storeFile = releaseStoreFile
                storePassword = keystoreProperties.getProperty("storePassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig =
                if (hasReleaseKeystore) {
                    signingConfigs.getByName("release")
                } else {
                    signingConfigs.getByName("debug")
                }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        abortOnError = true
        warningsAsErrors = false
        checkReleaseBuilds = false
        disable += setOf("GradleDependency", "AndroidGradlePluginVersion")
        enable += setOf(
            "ContentDescription",
            "LabelFor",
            "ClickableViewAccessibility",
        )
        checkDependencies = false
    }
}

dependencies {
    implementation(project(":ui"))
    implementation(project(":shared"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.koin.android)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.biometric)
}
