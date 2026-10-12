import com.android.build.gradle.internal.tasks.factory.dependsOn

val defaultAppList = System.getenv("DEFAULT_APPLIST") ?: "https://respect.directory/respect-ds/base.json"

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
    application
    alias(libs.plugins.buildconfigPlugin)
}



application {
    mainClass.set("org.openeel.server.ServerAppMainKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

kotlin {
    compilerOptions {
        compilerOptions {
            jvmToolchain(libs.versions.jvm.toolchain.get().toInt())
        }

        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }
}

buildConfig {
    packageName("org.openeel.server")
    className("OpenEelServerBuildConfig")

    buildConfigField<String>("DEFAULT_APPLIST", defaultAppList)
}

dependencies {
    implementation(projects.libShared)
    implementation(projects.libDatalayer)
    implementation(projects.libDatalayerDb)
    implementation(projects.libDataloadstateKtorServer)
    implementation(projects.libDatalayerHttpServer)
    implementation(projects.libXxhash)
    implementation(projects.libPrimarykeygen)

    implementation(projects.libIhttpCore)
    implementation(projects.libSharedSe)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.sqlite.bundled)

    implementation(libs.logback)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.auth)
    implementation(libs.argparse4j)
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.json)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.status.pages)

    implementation(libs.sqlite.jdbc)
    implementation(libs.napier)
    implementation(libs.webauthn4j.core)

    implementation(libs.ktor.server.html.builder)

    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.ktor.server.test.host)

}

tasks.withType<Tar>() {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Zip>() {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
