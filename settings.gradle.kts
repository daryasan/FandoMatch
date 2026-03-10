pluginManagement {
    plugins {
        // Kotlin
        kotlin("jvm") version "1.9.24"
        kotlin("kapt") version "1.9.24"
        kotlin("plugin.spring") version "1.9.24"
        kotlin("plugin.jpa") version "1.9.24"

        // Spring Boot
        id("org.springframework.boot") version "3.5.0"

        // Spring Dependency Management
        id("io.spring.dependency-management") version "1.1.7"
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"

        // OpenAPI Generator
        id("org.openapi.generator") version "7.6.0"

        // Jooq Codegen
        id("nu.studer.jooq") version "9.0"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "fandomatch"

include(
    "clients:users-api",
    "clients:core-api",
    "services:users",
    "services:core",
    "services:gateway"
)
