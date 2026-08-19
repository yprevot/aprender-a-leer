import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Firma de subida a Google Play.
// Las credenciales viven en keystore.properties (fuera del control de versiones).
// Si el fichero no existe, el build de release se firma con la clave de debug
// y sigue compilando: así el proyecto se puede clonar y compilar sin secretos.
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) keystorePropsFile.inputStream().use { load(it) }
}
val hayFirmaDeSubida = keystoreProps.getProperty("storeFile") != null

android {
    namespace = "com.aprenderaleer"
    // Se compila contra el SDK más reciente (lo exigen las AndroidX de 2026);
    // el comportamiento en runtime lo fija targetSdk.
    compileSdk = 37

    defaultConfig {
        applicationId = "com.aprenderaleer"
        minSdk = 24
        targetSdk = 36          // Exigido por Google Play desde el 31/08/2026
        versionCode = 1
        versionName = "1.0"
        // El juego es 100% offline y NO declara ni solicita ningún permiso.
    }

    androidResources {
        localeFilters += "es"
    }

    signingConfigs {
        if (hayFirmaDeSubida) {
            create("upload") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = if (hayFirmaDeSubida) {
                signingConfigs.getByName("upload")
            } else {
                signingConfigs.getByName("debug")
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
    }

    bundle {
        // La app solo tiene recursos en español (localeFilters): no tiene
        // sentido partir el AAB por idioma, y evita que Play sirva un split
        // sin cadenas si el dispositivo está en otro idioma.
        language { enableSplit = false }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        // La app se publica en Play: un error de lint debe romper el build.
        warningsAsErrors = false
        abortOnError = true
        checkReleaseBuilds = true
    }
}

dependencies {
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)

    debugImplementation(libs.compose.ui.tooling)

    testImplementation(libs.junit)
}
