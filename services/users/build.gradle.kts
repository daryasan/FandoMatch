plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("kapt") version "2.0.0"
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.17.0"
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

sourceSets {
    main {
        kotlin {
            srcDir("$buildDir/generated-sources/src/main/kotlin")
        }
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
        "useTags" to "true"
    )
}

tasks.named("compileKotlin") {
    dependsOn("openApiGenerate")
}

tasks.named("openApiGenerate") {
    outputs.dir(layout.buildDirectory.dir("generated-sources"))
    doFirst {
        val generatedDir = file("$buildDir/generated-sources")
        if (generatedDir.exists()) {
            generatedDir.deleteRecursively()
        }
    }
}


repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    // spring
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-test")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.security:spring-security-test")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // open api specs
    implementation("io.swagger.core.v3:swagger-annotations:2.2.41")
    implementation("javax.validation:validation-api:2.0.1.Final")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("org.openapitools:jackson-databind-nullable:0.2.8")

}


tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}