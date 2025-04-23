import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
}

fun getApiKey(propertyKey: String): String {
    return gradleLocalProperties(rootDir, providers).getProperty(propertyKey)
}

android {
    namespace = "com.example.jikimi"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.jikimi"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        addManifestPlaceholders(mapOf("NAVERMAP_CLIENT_ID" to getApiKey("NAVERMAP_CLIENT_ID")))
        buildConfigField("String", "OUTDOOR_EVACUATION_API_BASE", getApiKey("OUTDOOR_EVACUATION_API_BASE"))
        buildConfigField("String", "OUTDOOR_EVACUATION_API", getApiKey("OUTDOOR_EVACUATION_API"))
        buildConfigField("String", "OUTDOOR_EVACUATION_SERVICE_KEY", getApiKey("OUTDOOR_EVACUATION_SERVICE_KEY"))
        buildConfigField("String", "INDOOR_EVACUATION_API_BASE", getApiKey("INDOOR_EVACUATION_API_BASE"))
        buildConfigField("String", "INDOOR_EVACUATION_API", getApiKey("INDOOR_EVACUATION_API"))
        buildConfigField("String", "INDOOR_EVACUATION_SERVICE_KEY", getApiKey("INDOOR_EVACUATION_SERVICE_KEY"))
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Naver Map
    implementation(libs.map.sdk)
    implementation(libs.play.services.location)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Retrofit + Gson
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // OkHttp
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // ViewModel
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)

    // MultiDex
    implementation(libs.androidx.multidex)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // TikXML
    implementation("com.tickaroo.tikxml:annotation:0.8.13")
    implementation("com.tickaroo.tikxml:core:0.8.13")
    implementation("com.tickaroo.tikxml:retrofit-converter:0.8.13")
    ksp("com.tickaroo.tikxml:processor:0.8.13")

    // Coil
    implementation(libs.coil)

    // Paging
    implementation("androidx.paging:paging-runtime-ktx:3.3.6")

    // circleimageview
    implementation("de.hdodenhof:circleimageview:3.1.0")

    //dotsindicator
    implementation("com.tbuonomo:dotsindicator:5.1.0")

    // Splashscreen
    implementation ("androidx.core:core-splashscreen:1.0.1")

    // firebase
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.firebase:firebase-firestore")
//     implementation("com.google.firebase:firebase-storage")
    implementation("com.firebaseui:firebase-ui-storage:8.0.2")
}
