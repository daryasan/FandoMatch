plugins {
    kotlin("jvm") version "2.0.20"
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.6.0"
}

group = "com.fandomatch"
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
    generatorName = "kotlin"
    inputSpec = "$rootDir/services/core/specs/api.yaml"
    outputDir = "$buildDir/generated-sources"
    apiPackage = "com.fandomatch.core.api"
    modelPackage = "com.fandomatch.core.model"
    configOptions = mapOf(
        "library" to "jvm-spring-restclient",
        "useSpringBoot3" to "true",
        "serializationLibrary" to "jackson"
    )
}

tasks.named("openApiGenerate") {
    outputs.dir(generatedSourcesDir)
    doLast {
        val apiDir = file("$buildDir/generated-sources/src/main/kotlin/com/fandomatch/core/api")

        file("$apiDir/ApiUtil.kt").delete()
        file("$apiDir/DefaultExceptionHandler.kt").delete()
        file("$apiDir/ApiException.kt").delete()
        file("$apiDir/SpringDocConfiguration.kt").delete()
        file("$apiDir/Exceptions.kt").delete()
    }
}
dependencies {
//    compileOnly(project(mapOf("path" to ":services:users", "configuration" to "openApi")))
//    testCompileOnly(project(mapOf("path" to ":services:users", "configuration" to "openApi")))

    implementation(project(":services:users"))

    // test
    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.springframework.security:spring-security-test")


    // spring
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")


    // open api specs
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.41")
    implementation("io.swagger.core.v3:swagger-models:2.2.21")

    // logging
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")

    // db
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

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