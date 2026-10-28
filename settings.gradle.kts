rootProject.name = "Respect"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

include(":app-android")
include(":lib-appui-compose")
include(":app-server")
include(":lib-shared")
include(":lib-shared-se")
include(":app-cli")
include(":lib-datalayer")
include(":lib-datalayer-http-client")
include(":lib-datalayer-repository")
include(":lib-datalayer-db")
include(":lib-util")
include(":lib-xxhash")
include(":lib-primarykeygen")
include(":lib-credentials")

include(":lib-ihttp-core")
include(":lib-ihttp-iostreams")
include(":lib-ihttp-okhttp")
include(":lib-ihttp-nanohttpd")
include(":lib-cache")
include(":lib-serializers")
include(":lib-opds-model")
include(":lib-test")
include(":lib-test-resources")
include(":lib-xapi-core")
include(":lib-dataloadstate")
include(":lib-xapi-nanohttpd")
include(":lib-xapi-ipc-shared")
include(":lib-xapi-ipc-server")
include(":lib-xapi-ipc-client")
include(":lib-ipc-messagebridge")

include(":lib-http-ipc-shared")
include(":lib-http-ipc-client")
include(":lib-http-ipc-server")
include(":lib-xapi-test")
include(":lib-datalayer-http-server")
include(":lib-dataloadstate-ktor-server")

include(":lib-directory")
