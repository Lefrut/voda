import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.navigation.safeargs)
    id("kotlin-parcelize")
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    buildToolsVersion = "36.1.0"
    namespace = "com.m.vodovoz"
    compileSdk = 36
    ndkVersion = "29.0.14206865"


    bundle {
        language {
            @Suppress("UnstableApiUsage")
            enableSplit = false
        }

        density {
            @Suppress("UnstableApiUsage")
            enableSplit = false
        }
    }
    packaging {
        jniLibs { useLegacyPackaging = true }
    }

    defaultConfig {
        applicationId = "com.m.vodovoz"
        minSdk = 26
        targetSdk = 35
        versionCode = 2280
        //todo
        versionName = "2.2.8"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
        }

        data class AppKey(
            val buildConfigName: String,
            val propertyName: String,
        ) {
            constructor(name: String) : this(name, name)
        }


        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())

        listOf(
            AppKey("YOUTUBE_API_KEY"),
            AppKey("MAPKIT_API_KEY"),
            AppKey("GEOCODER"),
            AppKey("YANDEX_METRICA_KEY")
        ).forEach { (buildConfigName, propertyName) ->
            buildConfigField(
                type = "String",
                name = buildConfigName,
                value = properties.getProperty(propertyName)
            )
        }
    }


    signingConfigs {
        //todo
        create("release") {
            storeFile =
                file("/Users/kirill/StudioProjects/Vodovoz/lalalala.jks")
            keyAlias = "zinou"
            storePassword = "zinou123"
            keyPassword = "zinou123"
        }
    }

    buildTypes {
        //todo
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
            isShrinkResources = false
        }
    }

    lint {
        checkReleaseBuilds = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=$rootDir/composeMetrics",
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=$rootDir/composeReports"
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
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui.compose.material3)
    implementation(libs.media3.ui)

    //Tests
    testImplementation(libs.turbine)
    testImplementation(libs.junit4)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.core.testing)

    //Utils
    implementation(libs.libphonenumber)
    implementation(libs.coroutines.core)
    implementation(libs.pdf.viewer)

    //Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    //Lifecycle
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    //Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.viewmodel.navigation3)

    //Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.database.ktx)

    //Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp.logging.interceptor)

    //Moshi
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)

    //MapKit
    implementation(libs.yandex.mapkit)

    //Paging
    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.paging.compose)

    //Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.fragment)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    //Logs
    implementation(libs.timber)

    //Lottie
    implementation(libs.lottie.compose)

    //Work Manager
    implementation(libs.androidx.work.runtime)

    //Yandex Metrica
    implementation(libs.appmetrica)

    // Biometric
    implementation(libs.androidx.biometric.ktx)


    //debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.datastore.preferences)

    //Compose
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.foundation)


    //Cam
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.mlkit.barcode.scanning)

    //Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
    implementation(libs.coil.video)
    implementation(libs.coil.network.okhttp)

    //Decorations
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.compose.pager.indicator)
    implementation(libs.calendar.compose)
    implementation(libs.compose.shimmer)
    implementation(libs.haze)
    implementation(libs.compose.ratingbar)

    implementation(libs.play.app.update)
    implementation(libs.play.app.update.ktx)

    implementation(libs.android.oembed.video)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
}
