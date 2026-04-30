plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    alias(libs.plugins.baselineprofile) // Kotlin processor
    // för att Room och Dagger ska kunna köra sina kompilatorer
}

android {
    namespace = "com.example.test_design"
    compileSdk = 36

    packaging {
        resources {
            excludes += "/META-INF/INDEX.LIST"

            excludes += "/META-INF/io.netty.versions.properties"
            excludes += "/META-INF/okio.kotlin_module"
        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/jniLibs")
        }
    }

    defaultConfig {
        applicationId = "com.example.test_design"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                cppFlags("-std=c++20")
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt") // Använd '=' här
            version = "3.22.1"
        }
    }

    buildTypes {
        debug {

            // set true if you want to emulate, false if you use the terminal
            buildConfigField("boolean", "USE_EMULATED_TERMINAL", "false")
            // set true if you want to use local terminal (on-device terminal), false if you want POS and terminal to be on seperate devices
            buildConfigField("boolean", "USE_LOCAL_TERMINAL", "false")
            // if USE_LOCAL_TERMINAL = true, define off-device terminal IP-address here
            buildConfigField("String", "OFF_DEVICE_TERMINAL_IP", "\"192.168.121.112\"")
        }
        release {
            buildConfigField("boolean", "USE_EMULATED_TERMINAL", "false")
            buildConfigField("boolean", "USE_LOCAL_TERMINAL", "false")
            buildConfigField("String", "OFF_DEVICE_TERMINAL_IP", "\"192.168.121.112\"")
            isMinifyEnabled = true

            isShrinkResources = true

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
        buildConfig = true
    }
    compileSdk = 36
    buildToolsVersion = "36.1.0"
    ndkVersion = "26.1.10909125"
}

androidComponents {
    beforeVariants(selector().all()) { variantBuilder ->
        variantBuilder.enable = variantBuilder.buildType in setOf("debug", "release", "benchmarkRelease")
    }
}

dependencies {
    //Payment API
    implementation(project(":api"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.profileinstaller)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    baselineProfile(project(":baselineprofile"))
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("androidx.navigation:navigation-compose:2.7.0")
    implementation("com.jakewharton.threetenabp:threetenabp:1.4.4")

    implementation("androidx.compose.material:material-icons-extended")

    implementation("com.google.dagger:dagger:2.51.1")
    ksp("com.google.dagger:dagger-compiler:2.51.1")

    //Room core, det är en utbyggnad av SQLite som är en
    // prestandavänlig offline databas som finns inbyggd i android.
    // Vi behöver alltså inte installera SQLite, bara Room.

    val room_version = "2.8.4" // Ändra i efterhand om uppdateringar kommer

    implementation("androidx.room:room-runtime:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation ("androidx.room:room-ktx:$room_version")

    implementation("com.google.zxing:core:3.5.4")

    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-gif:2.5.0")

    //JankStats
    implementation("androidx.metrics:metrics-performance:1.0.0")

    implementation("io.ktor:ktor-server-core:2.3.12")
    implementation("io.ktor:ktor-server-netty:2.3.12")

    implementation("com.airbnb.android:lottie-compose:6.4.0")
}
