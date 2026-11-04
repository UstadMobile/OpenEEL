import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

import java.util.Properties
import java.io.FileInputStream

//As per: https://developer.android.com/studio/publish/app-signing.html#kts
// Create a variable called keystorePropertiesFile, and initialize it to your
// keystore.properties file, in the rootProject folder.
val keystorePropertiesFile = System.getenv("KEYSTORE")?.let {
    File(it)
} ?: rootProject.file("keystore.properties")

// Initialize a new Properties() object called keystoreProperties.
val keystoreProperties = Properties()

// Load your keystore.properties file into the keystoreProperties object.
keystoreProperties.takeIf { keystorePropertiesFile.exists() }
    ?.load(FileInputStream(keystorePropertiesFile))

val acraProperties = Properties()
val acraPropertiesFile = System.getenv("ACRA")?.let {
    File(it)
} ?: rootProject.file("acra.properties")

acraProperties.takeIf { acraPropertiesFile.exists() }
    ?.load(FileInputStream(acraPropertiesFile))


val acraPropNames = listOf("uri", "basicAuthLogin", "basicAuthPassword")

acraPropNames.forEach { propName ->
    System.getenv("ACRA_${propName.uppercase()}")?.also {
        acraProperties.setProperty(propName, it)
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

compose.resources {
    publicResClass = true
    packageOfResClass = "org.openeel.app.generated.resources"
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }

    android {
        namespace = "org.openeel.appcompose"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }

        androidResources {
            enable = true
        }
    }

    jvm()

    sourceSets {
        androidMain.dependencies {
            api(projects.libCredentials)
            implementation(projects.libSharedSe)
            implementation(projects.libXapiIpcServer)
            //Uncomment to test running web based publications through HttpIpc
            // implementation(projects.libHttpIpcClient)

            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.service.auth)
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
            implementation(libs.koin.android)
            implementation(libs.okhttp)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.compose.material3.window.size.clazz)
            implementation(projects.libDatalayerDb)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.androidx.webkit)
            implementation(libs.material)
            implementation(libs.androidx.appcompat)
            implementation(libs.coil3.coil.svg)
            implementation(libs.acra.http)
            implementation(libs.acra.core)
            implementation(libs.libphonenumber.android)
            implementation(libs.accompanist.permissions)
        }

        commonMain.dependencies {
            implementation(projects.libShared)
            api(projects.libDatalayer)
            api(projects.libXxhash)
            implementation(projects.libDatalayerRepository)
            implementation(projects.libDatalayerHttpClient)
            implementation(projects.libPrimarykeygen)
            implementation(projects.libCache)

            implementation(libs.napier)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.foundation)
            implementation(compose.runtime)
            implementation(libs.multiplatformsettings)
            implementation(compose.materialIconsExtended)
            implementation(libs.lifecycle.runtime.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.date.time)
            implementation(libs.coil3.coil.compose)
            implementation(libs.coil.network.okhttp)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.date.time)
            implementation(libs.koalaplot)
            implementation(libs.kotlinx.io.core)
            implementation(libs.androidx.paging.compose)
            implementation(libs.reorderable)
            implementation(libs.kscan)
            implementation(libs.qrose)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(projects.libSharedSe)
        }
    }
}


compose.desktop {
    application {
        mainClass = "org.openeel.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.openeel.app"
            packageVersion = "1.0.0"
        }
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}