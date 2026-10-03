plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.benbrowser"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.benbrowser"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

// Force stable AndroidX versions to prevent Haze 2.0.1's unconstrained transitives from pulling incompatible alpha artifacts
configurations.all {
    resolutionStrategy {
        force("androidx.core:core:1.15.0")
        force("androidx.core:core-ktx:1.15.0")
        force("androidx.activity:activity:1.9.3")
        force("androidx.activity:activity-ktx:1.9.3")
        force("androidx.activity:activity-compose:1.9.3")
    }
}

dependencies {
    implementation("androidx.core:core:1.15.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity:1.9.3")
    implementation("androidx.activity:activity-compose:1.9.3")

    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")

    // Modern AndroidX WebKit for WebViewFeature & algorithmic darkening
    implementation("androidx.webkit:webkit:1.12.1")

    // Haze 2.0.1 for hardware-accelerated blur / glass effects with clean dependency bounds
    implementation("dev.chrisbanes.haze:haze:2.0.1") {
        exclude(group = "androidx.activity")
        exclude(group = "androidx.core")
    }
    implementation("dev.chrisbanes.haze:haze-blur:2.0.1") {
        exclude(group = "androidx.activity")
        exclude(group = "androidx.core")
    }
}
