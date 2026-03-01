plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("kapt") version "2.0.0"
    jacoco
}

allprojects {
    group = "com.fandomatch"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

subprojects {
    apply(plugin = "jacoco")

    tasks.withType<Test> {
        finalizedBy("jacocoTestReport")
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

