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
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Nierika"
include(":app")
include(":core:designsystem")
include(":core:database")
include(":core:mediastore")
include(":core:data")
include(":core:domain")
include(":feature:timeline")
include(":feature:collections")
include(":feature:settings")
include(":feature:editor")
include(":feature:viewer")