plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("io.spring.dependency-management")
    id("jacoco")
}

allprojects {
    group = "com.fandomatch"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

subprojects {
    apply(plugin = "jacoco")

    tasks.withType<Test> {
        useJUnitPlatform()
        finalizedBy("jacocoTestReport")
    }

    // Настройка toolchain для всех Kotlin-задач
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "21"
    }

    jacoco {
        toolVersion = "0.8.10"
    }

    tasks.matching { it.name == "jacocoTestReport" }.configureEach {
        this as JacocoReport
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}

tasks.register<JacocoReport>("jacocoRootReport") {
    val testableProjects = subprojects.filter { it.tasks.findByName("test") != null }
    dependsOn(testableProjects.map { it.tasks.named("test") })

    executionData.setFrom(
        testableProjects.map { it.fileTree("build/jacoco") { include("*.exec") } }
    )

    sourceDirectories.setFrom(
        testableProjects.map { it.fileTree("src/main/kotlin") }
    )

    classDirectories.setFrom(
        testableProjects.map { it.fileTree("build/classes/kotlin/main") }
    )

    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}