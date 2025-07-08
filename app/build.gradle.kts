// 🌟 Suraksha App: Unified Gradle Build File
// Supports Jetpack Compose, ViewBinding, Biometric Auth, Retrofit, Volley & more.

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.suraksha"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.suraksha"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ✅ Enables vector support for older Android versions
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            // 🔐 Keep minify false unless using ProGuard rules
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        // ⚙️ Use Java 11 compatibility
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        // ⚙️ Kotlin JVM target
        jvmTarget = "11"
    }

    buildFeatures {
        // 🎨 Enable Compose UI toolkit
        compose = true

        // 🧩 Useful for working with XML layouts
        viewBinding = true
    }

    composeOptions {
        // 🎯 Jetpack Compose compiler version
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            // 🧹 Clean up unused license files
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // 🚀 Jetpack Compose UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // 🧱 XML & Material UI Support
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // 🌐 Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.android.volley:volley:1.2.1")

    // 🔐 Biometric Authentication
    implementation("androidx.biometric:biometric:1.1.0")

    // ✅ Unit Testing & UI Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // 🛠️ Debugging Tools
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
