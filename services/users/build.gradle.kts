plugins {
    kotlin("jvm") version "2.0.20"
    id("org.openapi.generator") version "7.17.0"
}

group = "com.fandomatch"
version = "1.0-SNAPSHOT"

sourceSets {
    main {
        kotlin {
            srcDir("$buildDir/generated-sources/src/main/kotlin")
        }
    }
}


openApiGenerate {
    generatorName = "kotlin-spring"
    inputSpec = "$rootDir/services/users/src/main/specs/api.yaml"
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
    implementation("org.springframework.boot:spring-boot-starter-websocket:3.3.3")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.3.3")
    implementation("org.springframework.boot:spring-boot-starter-security:3.3.3")
    implementation("org.springframework.boot:spring-boot-starter-test:3.3.3")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:3.3.3")
    implementation("org.springframework.boot:spring-boot-starter-web:3.3.3")
    testImplementation("org.springframework.security:spring-security-test:6.3.3")
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