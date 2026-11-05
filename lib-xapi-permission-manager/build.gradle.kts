
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
        namespace = "${rootProject.group}.lib.xapi.security"
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    jvm()


    sourceSets {
        commonMain.dependencies {
            api(projects.libXapiCore)

            api(libs.kotlinx.serialization.json)
            api(libs.xmlutil.serialization)
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
