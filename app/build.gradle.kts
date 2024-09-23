import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)    //hilt
//    id("com.google.gms.google-services")
    id ("kotlin-parcelize")
}

//val properties = Properties().apply {
//    load(FileInputStream(rootProject.file("local.properties")))
//}

fun getApiKey(propertyKey: String): String {
    return gradleLocalProperties(rootDir,providers).getProperty(propertyKey)
}


android {
    namespace = "com.example.jikimi"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.jikimi"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

//        addManifestPlaceholders(mapOf("NAVERMAP_CLIENT_ID" to properties.getProperty("NAVERMAP_CLIENT_ID")))
//        buildConfigField("String", "OUTDOOR_EVACUATION_API_BASE", properties.getProperty("OUTDOOR_EVACUATION_API_BASE"))
//        buildConfigField("String", "OUTDOOR_EVACUATION_API", properties.getProperty("OUTDOOR_EVACUATION_API"))
//        buildConfigField("String", "OUTDOOR_EVACUATION_SERVICE_KEY", properties.getProperty("OUTDOOR_EVACUATION_SERVICE_KEY"))

        addManifestPlaceholders(mapOf("NAVERMAP_CLIENT_ID" to getApiKey("NAVERMAP_CLIENT_ID")))
        buildConfigField("String", "OUTDOOR_EVACUATION_API_BASE", getApiKey("OUTDOOR_EVACUATION_API_BASE"))
        buildConfigField("String", "OUTDOOR_EVACUATION_API", getApiKey("OUTDOOR_EVACUATION_API"))
        buildConfigField("String", "OUTDOOR_EVACUATION_SERVICE_KEY", getApiKey("OUTDOOR_EVACUATION_SERVICE_KEY"))
    }

    packaging {
        resources.excludes += "META-INF/DEPENDENCIES"
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation (libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // bottomNavigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Naver Map
    implementation(libs.map.sdk)
    // Naver Map 현재위치
    implementation(libs.play.services.location)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // retrofit2
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // okhttp
    implementation (libs.okhttp)
    implementation(libs.logging.interceptor)

    // viewModels
    implementation (libs.androidx.activity.ktx)
    implementation (libs.androidx.fragment.ktx)

    implementation (libs.androidx.core)
}