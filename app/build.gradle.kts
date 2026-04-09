plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
    id("com.google.devtools.ksp") version "2.2.10-2.0.2"
}
android {
    namespace = "com.cookshare"
    compileSdk {
        version = release(version = 36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.cookshare"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile(name = "proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Lifecycle
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.runtime)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)

    // Image loading
    implementation(libs.picasso)
    implementation(libs.circleimageview)

    // Coroutines
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.play.services)

    // SwipeRefresh
    implementation(libs.swiperefreshlayout)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}