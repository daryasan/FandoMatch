plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("kapt") version "2.0.0"
}

group = "com.fandomatch"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
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