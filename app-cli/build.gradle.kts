plugins {
    id("java-library")
    id("application")
    alias(libs.plugins.kotlinJvm)
}

application {
    mainClass = "org.openeel.clitools.OpenEelCLI"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.argparse4j)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.date.time)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.json)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.okhttp)
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)

    implementation(projects.libShared)

    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.ktor.server.core)
    testImplementation(libs.ktor.server.netty)
    testImplementation(libs.ktor.server.content.negotiation)
    testImplementation(projects.libTest)
    testImplementation(libs.ktor.server.test.host)
}


tasks.withType<Tar>() {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Zip>() {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}
