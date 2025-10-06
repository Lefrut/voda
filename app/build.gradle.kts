plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-parcelize")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.m.vodovoz"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.m.vodovoz"
        minSdk = 21
        targetSdk = 35
        //todo - release
        versionCode = 2011
        //todo - release
        versionName = "2.0.11"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("/home/VODOVOZ/rk_sotin/Projects/VodovozJava/VodovozVersion1.jks")
            keyAlias = "zinou"
            storePassword = "zinou123"
            keyPassword = "zinou123"
        }
    }

    buildTypes {
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }

        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = false
        }
    }

    lint {
        checkReleaseBuilds = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-P", "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=$rootDir/composeMetrics",
            "-P", "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=$rootDir/composeReports"
        )
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    composeCompiler {
        stabilityConfigurationFile.set(
            rootProject.layout.projectDirectory.file("stability_config.conf")
        )
    }

}

dependencies {

    //Tests
    testImplementation("app.cash.turbine:turbine:1.2.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("io.mockk:mockk:1.12.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    //Utils
    implementation("com.googlecode.libphonenumber:libphonenumber:8.12.38")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("io.github.afreakyelf:Pdf-Viewer:2.1.1")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    //Android
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")

    //Lifecycle
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")

    //Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    //Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-database-ktx")

    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    //Moshi
    val moshiVersion = "1.15.0"
    implementation("com.squareup.moshi:moshi:$moshiVersion")
    implementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")

    //MapKit
    implementation("com.yandex.android:maps.mobile:4.0.0-full")

    //Paging
    implementation("androidx.paging:paging-runtime-ktx:3.3.6")
    implementation("androidx.paging:paging-compose:3.3.6")

    //Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-fragment:1.0.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    //Logs
    implementation("com.jakewharton.timber:timber:5.0.1")

    //Lottie
    implementation("com.airbnb.android:lottie-compose:6.6.0")

    //Work Manager
    implementation("androidx.work:work-runtime:2.10.3")

    //Yandex Metrica
    implementation("com.yandex.android:mobmetricalib-ndk-crashes:1.1.0")
    implementation("com.yandex.android:mobmetricalib:5.3.0")

    // Biometric
    implementation("androidx.biometric:biometric-ktx:1.2.0-alpha05")


    //debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("androidx.datastore:datastore-preferences:1.1.7")

    //Compose
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.compose.ui:ui:1.8.2")
    implementation("androidx.compose.ui:ui-tooling-preview:1.8.2")
    implementation("androidx.compose.ui:ui-tooling:1.8.2")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.compose.foundation:foundation:1.8.2")


    //Cam
    implementation("androidx.camera:camera-camera2:1.4.2")
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    implementation("androidx.camera:camera-view:1.4.2")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    //Coil
    implementation("io.coil-kt.coil3:coil-compose:3.0.4")
    implementation("io.coil-kt.coil3:coil-svg:3.0.4")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")

    //Decorations
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.27.0")
    implementation("mx.platacard:compose-pager-indicator:0.0.8")
    implementation("com.kizitonwose.calendar:compose:2.6.2")
    implementation("com.valentinilk.shimmer:compose-shimmer:1.0.5")
    implementation("dev.chrisbanes.haze:haze:1.6.10")
    implementation("com.github.a914-gowtham:compose-ratingbar:1.3.12")

    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")

    implementation("com.github.TalbotGooday:Android-Oembed-Video:0.2.8")

    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.navigation:navigation-compose:2.8.9")
}