
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

kotlin {
    compilerOptions {
        jvmToolchain(libs.versions.jvm.toolchain.get().toInt())

        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            api(projects.libDatalayer)
            api(projects.libDatalayerDb)
            api(projects.libShared)
            api(projects.libDataloadstateKtorServer)
            api(libs.ktor.server.core)
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
