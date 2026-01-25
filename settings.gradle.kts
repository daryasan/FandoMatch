plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "FandoMatch"
include("services:users")
include("services:gateway")
findProject(":services:users")?.name = "users"
findProject(":services:gateway")?.name = "gateway"
include("services:core")
findProject(":services:core")?.name = "core"
