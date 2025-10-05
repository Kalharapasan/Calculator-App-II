plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.calculatorappii"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.calculatorappii"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Use 'isMinifyEnabled' in .kts files
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // Use the versions from your libs.versions.toml file
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    // The following dependency was in your file but not in the version catalog
    implementation("androidx.cardview:cardview:1.0.0")

    // Example test dependencies from your catalog (if you need them)
    // testImplementation(libs.junit)
    // androidTestImplementation(libs.ext.junit)
    // androidTestImplementation(libs.espresso.core)
}
