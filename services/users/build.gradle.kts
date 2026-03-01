
import org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask


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

val openApiGenerator by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val openApi by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false

    attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_API))
    attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named("jar"))
}


val openApiRuntime by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false

    attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named("jar"))
}


val generatedSourcesDir = layout.buildDirectory.dir("generated-sources/src/main/kotlin")

sourceSets {
    main {
        kotlin.srcDir(generatedSourcesDir)
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
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")


    // spring
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.security:spring-security-test")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-actuator")


    // open api specs
    openApiGenerator("org.openapitools:openapi-generator-cli:7.6.0")
    openApiGenerator("com.fasterxml.jackson.core:jackson-core:2.17.2")
    openApiGenerator("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    openApiGenerator("com.fasterxml.jackson.core:jackson-annotations:2.17.2")
    openApiGenerator("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.41")
    implementation("io.swagger.core.v3:swagger-models:2.2.21")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")

    // crypto
    implementation("org.springframework.security:spring-security-crypto")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")

    // logging
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")

    // jwt
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.3")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.3")

    // db
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

tasks.named<GenerateTask>("openApiGenerate") {
    generatorName.set("kotlin")
    inputSpec.set("$rootDir/services/users/specs/api.yaml")
    outputDir.set("$buildDir/generated-sources")
    apiPackage.set("com.fandomatch.users.api")
    modelPackage.set("com.fandomatch.users.model")
    configOptions.set(
        mapOf(
            "library" to "jvm-spring-restclient",
            "useSpringBoot3" to "true",
            "serializationLibrary" to "jackson"
        )
    )

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

tasks.named("openApiGenerate") {
    val cfg = project.configurations.getByName("openApiGenerator")
    doFirst {
        try {
            val method =
                this::class.java.methods.firstOrNull { it.name == "setGeneratorClasspath" && it.parameterTypes.size == 1 }
            if (method != null) {
                method.invoke(this, cfg)
            } else {
                val field = this::class.java.declaredFields.firstOrNull { it.name == "generatorClasspath" }
                if (field != null) {
                    field.isAccessible = true
                    field.set(this, cfg)
                }
            }
        } catch (t: Throwable) {
            throw GradleException("Failed to set generatorClasspath for openApiGenerate: ${t.message}", t)
        }
    }
}



tasks.register<Jar>("openApiJar") {
    archiveClassifier.set("openapi")
    dependsOn(tasks.named("openApiGenerate"))
    dependsOn(tasks.named("classes"))
    from(fileTree("${buildDir}/classes/kotlin/main") { include("**/*.class") })
}

openApi.outgoing.artifact(tasks.named("openApiJar"))
openApiRuntime.outgoing.artifact(tasks.named("openApiJar"))

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
