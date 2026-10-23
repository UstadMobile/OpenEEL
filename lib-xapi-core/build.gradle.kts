
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
    id("maven-publish")
}

kotlin {
    compilerOptions {
        jvmToolchain(libs.versions.jvm.toolchain.get().toInt())

        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        optIn.add("kotlinx.serialization.ExperimentalSerializationApi")
    }

    android {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        namespace = "${rootProject.group}.lib.xapi.model"
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    jvm()


    sourceSets {
        commonMain.dependencies {
            api(projects.libSerializers)
            api(projects.libDataloadstate)

            api(projects.libUtil)
            api(libs.kotlinx.serialization.json)
            api(libs.uri.kmp)
            api(libs.kotlinx.date.time)
            api(libs.ktor.client.core)
            api(libs.xmlutil.serialization)
            implementation(libs.napier)
        }

        jvmMain.dependencies {

        }

        jvmTest.dependencies {

        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

    }
}
