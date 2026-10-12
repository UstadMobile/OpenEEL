plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
}

kotlin {

    android {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        namespace = "org.openeel.lib.cache"

        compilerOptions {

        }

        kotlin {
            jvmToolchain(17)
        }
    }

    jvm{

    }

    sourceSets {
        commonMain.dependencies {
            api(projects.libIhttpCore)
            implementation(libs.nanohttpd)
            api(libs.kotlinx.io.core)
        }
    }
}

