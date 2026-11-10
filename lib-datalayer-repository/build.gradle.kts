
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
        namespace = "${rootProject.group}.datalayer.repository"
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            api(projects.libDatalayer)
            implementation(projects.libUtil)
            implementation(libs.kotlinx.serialization.json)
            api(libs.uri.kmp)
            api(libs.kotlinx.date.time)
            api(libs.ktor.client.core)
            implementation(libs.napier)
            implementation(libs.atomicfu)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }

        androidMain.dependencies {
            implementation(libs.androidx.work.runtime)
            implementation(libs.koin.android)
        }

        jvmMain.dependencies {

        }

        jvmTest.dependencies {
            implementation(projects.libTest)
            implementation(kotlin("test"))
            implementation(projects.appServer)
            implementation(projects.libDatalayerHttpServer)
            implementation(projects.libPrimarykeygen)
            implementation(projects.libXxhash)
            implementation(projects.libTestResources)
            implementation(projects.libXapiTest)

            implementation(libs.turbine)
            implementation(projects.libDatalayerHttpClient)
            implementation(projects.libDatalayerDb)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)

            implementation(libs.okhttp)
            implementation(libs.ktor.client.okhttp)


            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.client.content.negotiation)

            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.netty)
            implementation(libs.ktor.server.content.negotiation)
            implementation(libs.ktor.server.conditional.headers)
            implementation(libs.ktor.server.call.logging)
            implementation(libs.logback)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.ktor)
            implementation(libs.mockito.kotlin)
        }
    }
}
