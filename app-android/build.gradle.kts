import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}


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

val ACRA_PROP_NAMES = listOf("uri", "basicAuthLogin", "basicAuthPassword")

// The applist list - see main README
val defaultAppList = System.getenv("DEFAULT_APPLIST") ?: "https://respect.directory/respect-ds/base.json"

// The IP address country lookup server endpoint
val geolocationApiEndpoint = System.getenv("GEOLOCATION_API_ENDPOINT") ?: "https://geoip.ustadmobile.com/"

// https://kotlinlang.org/docs/multiplatform/multiplatform-project-agp-9-migration.html#configure-the-build-script-for-the-android-app

kotlin {
    target {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    dependencies {
        api(projects.libCredentials)
        implementation(projects.libCache)

        implementation(projects.libAppuiCompose)
        implementation(projects.libSharedSe)
        implementation(projects.libXapiIpcServer)
        implementation(projects.libDatalayerRepository)
        implementation(projects.libPrimarykeygen)
        implementation(projects.libHttpIpcServer)
        implementation(projects.libHttpIpcClient)

        implementation(libs.multiplatformsettings)
        implementation(libs.androidx.credentials)
        implementation(libs.androidx.credentials.play.service.auth)
        implementation(compose.preview)
        implementation(compose.components.resources)
        implementation(libs.androidx.activity.compose)
        implementation(libs.androidx.appcompat)

        implementation(project.dependencies.platform(libs.koin.bom))
        implementation(libs.koin.core)
        implementation(libs.koin.android)
        implementation(libs.okhttp)
        implementation(libs.ktor.client.okhttp)
        implementation(libs.ktor.client.core)
        implementation(libs.ktor.client.json)
        implementation(libs.ktor.client.content.negotiation)
        implementation(libs.ktor.serialization.kotlinx.json)
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
        implementation(libs.coil.network.okhttp)
        implementation(libs.coil3.coil.compose)
        implementation(libs.napier)

        implementation(compose.material3)
    }
}


android {
    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        println("Keystore exists: ${keystorePropertiesFile.exists()}")
        //See https://developer.android.com/build/building-cmdline#gradle_signing
        if(keystorePropertiesFile.exists()) {
            create("release") {
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
            }
        }
    }

    namespace = "org.openeel.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.openeel.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 133
        versionName = project.version.toString()

        for(propName in ACRA_PROP_NAMES) {
            buildConfigField(
                type = "String",
                name = "ACRA_${propName.uppercase()}",
                value = "\"${acraProperties.getProperty(propName) ?: ""}\"   "
            )
        }

        buildConfigField(
            type = "String",
            name = "DEFAULT_APP_LIST",
            value = "\"$defaultAppList\""
        )
        buildConfigField(
            type = "String",
            name = "GEOLOCATION_API_ENDPOINT",
            value = "\"$geolocationApiEndpoint\""
        )
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            if(keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }


            proguardFiles(
                // Default file with automatically generated optimization rules.
                getDefaultProguardFile("proguard-android-optimize.txt"),
                project.file("proguard-rules.pro")
            )

        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {

}