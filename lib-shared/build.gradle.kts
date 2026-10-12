
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

compose.resources {
    publicResClass = true
    packageOfResClass = "org.openeel.shared.generated.resources"
}


kotlin {
    compilerOptions {
        jvmToolchain(libs.versions.jvm.toolchain.get().toInt())

        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }

    android {
        //As per https://youtrack.jetbrains.com/projects/CMP/issues/CMP-8232/org.jetbrains.compose.resources.MissingResourceException-Missing-resource-with-path-composeResources
        androidResources {
            enable = true
        }

        compileSdk = libs.versions.android.compileSdk.get().toInt()
        namespace = "${rootProject.group}.shared"
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            api(projects.libDatalayer)
            api(projects.libCredentials)
            api(projects.libUtil)
            api(projects.libDatalayerDb)
            api(projects.libDatalayerHttpClient)
            api(projects.libXapiCore)

            implementation(projects.libCache)
            implementation(projects.libXxhash)
            implementation(projects.libPrimarykeygen)

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.savedstate)
            implementation(libs.navigation.compose)
            implementation(compose.components.resources)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.argparse4j)
            api(libs.uri.kmp)
            implementation(libs.kotlinx.date.time)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.client.content.negotiation)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)

            implementation(libs.multiplatformsettings)
            implementation(libs.napier)
            implementation(libs.qrose)
            implementation(libs.urlencoder)
            implementation(libs.cache4k)

        }

        androidMain.dependencies {
            api(projects.libXapiNanohttpd)
            implementation(libs.androidx.preference)
            implementation(libs.androidx.preference.ktx)
            implementation(libs.acra.core)
            implementation(libs.libphonenumber.android)
            implementation(libs.androidx.biometric.ktx)
            implementation(libs.installreferrer)

            implementation(libs.androidx.browser)
            implementation(projects.libXapiCore)
            implementation(projects.libXapiIpcShared)
            implementation(libs.jsoup)
        }

        jvmMain.dependencies {
            implementation(projects.libDatalayerDb)
            implementation(libs.androidx.room.runtime)
            implementation(libs.json.schema.validator)
            implementation(libs.jsoup)
            implementation(libs.okhttp)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.webauthn4j.core)
            implementation(libs.libphonenumber.google)
        }

        jvmTest.dependencies {
            implementation(projects.libTest)
            implementation(projects.libSharedSe)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.netty)
            implementation(libs.ktor.server.content.negotiation)
            implementation(libs.ktor.server.conditional.headers)
            implementation(libs.ktor.client.core)
            implementation(libs.koin.test)
            implementation(libs.mockito.kotlin)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

