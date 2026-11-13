
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

kotlin {
    compilerOptions {
        jvmToolchain(libs.versions.jvm.toolchain.get().toInt())
    }

    android {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        namespace = "${rootProject.group}.libcache"
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    jvm {

    }

    sourceSets {
        commonMain.dependencies  {
            implementation(kotlin("stdlib-common"))
            api(projects.libIhttpCore)
            implementation(projects.libOpdsModel)
            implementation(projects.libXapiCore)
            implementation(projects.libIhttpIostreams)
            implementation(projects.libXxhash)
            implementation(projects.libUtil)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.atomicfu)
            implementation(libs.kotlinx.io.core)
            implementation(libs.ktor.client.core)
            implementation(libs.napier)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.xmlutil.serialization)

            implementation(libs.nanohttpd)
            implementation(libs.okhttp)
            implementation(projects.libIhttpOkhttp)
            implementation(libs.androidx.room.runtime)
            implementation(libs.kotlinx.date.time)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.cache4k)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(kotlin("test-common"))
        }

        jvmMain.dependencies {
            implementation(libs.okhttp)
        }

        jvmTest.dependencies {
            implementation(libs.mockwebserver)
            implementation(libs.mockito.kotlin)
            implementation(libs.turbine)
            implementation(libs.androidx.sqlite.bundled)
            implementation(projects.libIhttpNanohttpd)

            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.netty)
            implementation(libs.ktor.server.content.negotiation)
            implementation(libs.ktor.server.conditional.headers)
            implementation(libs.ktor.server.auto.head.response)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.serialization.kotlinx.json)
        }

        androidMain.dependencies {
            implementation(libs.androidx.room.ktx)
            implementation(libs.androidx.lifecycle.common.java8)
            implementation(libs.androidx.work.runtime)
            implementation(libs.koin.android)
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspJvm", libs.androidx.room.compiler)
    add("kspAndroid", libs.androidx.room.compiler)
}



