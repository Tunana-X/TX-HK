plugins {
    alias(libs.plugins.android.application)
}

android {

    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true;
    }
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.vision.common)
    implementation(libs.swiperefreshlayout)
    implementation(libs.play.services.mlkit.text.recognition.chinese)
    implementation(libs.swiperefreshlayout)
    implementation("androidx.navigation:navigation-fragment-ktx:2.6.0")  // 或最新版本
    implementation("androidx.navigation:navigation-ui-ktx:2.6.0")      // 或最新版本
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("com.google.mlkit:text-recognition-chinese:16.0.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}