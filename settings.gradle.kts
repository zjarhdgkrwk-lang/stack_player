pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Stack"

include(":app")
include(":core")
include(":core:player")
include(":data")
include(":domain")
include(":feature:gate")
include(":feature:library")
include(":feature:player")
include(":feature:search")
include(":feature:tags")
include(":feature:playlists")
include(":feature:settings")
include(":feature:crashlog")
