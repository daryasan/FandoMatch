pluginManagement {
    plugins {
        // Kotlin
        kotlin("jvm") version "2.0.21"
        kotlin("kapt") version "2.0.21"
        kotlin("plugin.spring") version "2.0.21"
        kotlin("plugin.jpa") version "2.0.21"

        // Spring Boot
        id("org.springframework.boot") version "3.5.0"

        // Spring Dependency Management
        id("io.spring.dependency-management") version "1.1.7"
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"

        // OpenAPI Generator
        id("org.openapi.generator") version "7.6.0"
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

rootProject.name = "fandomatch"

include(
    "clients:users-api",
    "clients:core-api",
    "clients:messaging-api",
    "clients:common-models",
    "services:users",
    "services:core",
    "services:gateway",
    "services:messaging",
    "libs:media-lib",
    "libs:notifications-lib"
)