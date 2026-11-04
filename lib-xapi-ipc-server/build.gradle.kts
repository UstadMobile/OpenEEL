import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    id("maven-publish")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget("17")

        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        optIn.add("kotlinx.serialization.ExperimentalSerializationApi")
    }
}


android {
    namespace = "org.openeel.app.xapi.ipc.server"
    compileSdk {
        version = release(libs.versions.android.compileSdk.get().toInt())
    }

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()

        testInstrumentationRunner = "org.openeel.xapi.ipc.server.InstrumentationTestRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    api(projects.libXapiIpcShared)
    api(projects.libIpcMessagebridge)

    implementation(projects.libUtil)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)

    implementation(libs.urlencoder)


    androidTestImplementation(libs.androidx.rules)
    testImplementation(libs.junit)
    androidTestImplementation(projects.libXapiIpcClient)
    androidTestImplementation(projects.libDatalayerDb)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(projects.libTestResources)
    androidTestImplementation(projects.libXapiTest)
    androidTestImplementation(libs.cache4k)
    androidTestImplementation(kotlin("test"))
}