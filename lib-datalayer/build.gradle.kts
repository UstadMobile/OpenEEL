import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
        namespace = "${rootProject.group}.datalayer"
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            api(projects.libDataloadstate)
            api(projects.libIhttpCore)
            api(projects.libXxhash)
            api(projects.libCredentials)
            api(projects.libOpdsModel)
            api(projects.libXapiCore)
            api(projects.libSerializers)
            api(projects.libUtil)
            implementation(libs.kotlinx.serialization.json)
            api(libs.uri.kmp)
            api(libs.kotlinx.date.time)
            api(libs.ktor.client.core)
            api(libs.androidx.paging.common)
            implementation(libs.atomicfu)
            implementation(libs.napier)
            implementation(libs.urlencoder)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }

        jvmMain.dependencies {

        }

        androidMain.dependencies {
            implementation(libs.androidx.work.runtime.ktx)
            implementation(libs.koin.android)
        }

        jvmTest.dependencies {
            implementation(projects.libDatalayerDb)
            implementation(projects.libTestResources)
            implementation(projects.libXapiTest)
            implementation(libs.junit)
            implementation(libs.mockito.kotlin)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
