
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

kotlin {
    compilerOptions {
        jvmToolchain(libs.versions.jvm.toolchain.get().toInt())

        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }

    android {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        namespace = "${rootProject.group}.libuserdirectory"
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            api(projects.libDataloadstate)
            api(projects.libIhttpCore)
            api(projects.libSerializers)
            implementation(libs.kotlinx.serialization.json)
            api(libs.kotlinx.date.time)
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