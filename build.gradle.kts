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
    dependsOn(subprojects.map { it.tasks.named("test") })

    executionData.setFrom(
        subprojects.map { it.file("build/jacoco/test.exec") }
    )

    sourceDirectories.setFrom(
        subprojects.map { it.file("src/main/kotlin") }
    )

    classDirectories.setFrom(
        subprojects.map { it.file("build/classes/kotlin/main") }
    )

    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}