import org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask

plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("kapt") version "2.0.0"
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.6.0"
    kotlin("plugin.jpa") version "1.9.0"
    id("org.jetbrains.kotlin.plugin.spring") version "1.9.22"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.0.0")
    }
}

val generatedSourcesDir = layout.buildDirectory.dir("generated-sources/src/main/kotlin")

sourceSets {
    main {
        kotlin.srcDir(generatedSourcesDir)
    }
}

openApiGenerate {
    generatorName = "kotlin-spring"
    inputSpec = "$rootDir/services/users/specs/api.yaml"
    outputDir = "$buildDir/generated-sources"
    apiPackage = "com.fandomatch.users.api"
    modelPackage = "com.fandomatch.users.model"
    configOptions = mapOf(
        "interfaceOnly" to "true",
        "useTags" to "true",
        "jakarta" to "true",
        "useBeanValidation" to "false"
    )
}

tasks.named("openApiGenerate") {
    outputs.dir(generatedSourcesDir)
    doLast {
        val apiDir = file("$buildDir/generated-sources/src/main/kotlin/com/fandomatch/users/api")

        file("$apiDir/ApiUtil.kt").delete()
        file("$apiDir/DefaultExceptionHandler.kt").delete()
        file("$apiDir/ApiException.kt").delete()
        file("$apiDir/SpringDocConfiguration.kt").delete()
        file("$apiDir/Exceptions.kt").delete()
    }
}

tasks.withType<KaptGenerateStubsTask>().configureEach {
    dependsOn("openApiGenerate")
}

tasks.named("compileKotlin") {
    dependsOn("openApiGenerate")
}


repositories {
    mavenCentral()
}

dependencies {
    // test
    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("org.assertj:assertj-core:3.25.3")
    testImplementation("com.h2database:h2")


    // spring
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.security:spring-security-test")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-configuration-processor")


    // open api specs
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.41")
    implementation("io.swagger.core.v3:swagger-models:2.2.21")

    // crypto
    implementation("org.springframework.security:spring-security-crypto")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")

    // logging
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")

    // jwt
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.3")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.3")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
