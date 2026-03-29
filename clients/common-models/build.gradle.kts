import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("org.openapi.generator")
}

val generatedDir = layout.buildDirectory.dir("generated/src/main/kotlin")

sourceSets {
    main {
        kotlin.srcDir(generatedDir)
    }
}

dependencies {
    implementation("org.springframework:spring-web:6.2.0")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.41")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
}


tasks.named<GenerateTask>("openApiGenerate") {
    generatorName.set("kotlin")
    inputSpec.set("$projectDir/specs/definitions.yaml")
    outputDir.set(generatedDir.get().asFile.path)
    modelPackage.set("com.fandomatch.core.model")

    globalProperties.set(
        mapOf(
            "apiTests" to "false",
            "modelTests" to "false"
        )
    )

    configOptions.set(
        mapOf(
            "library" to "jvm-spring-restclient",
            "useSpringBoot3" to "true",
            "serializationLibrary" to "jackson",
            "useOneOfInterfaces" to "true",
            "interfaceOnly" to "false",
            "serializableModel" to "true",
            "useAbstractClassForOneOf" to "true",
        )
    )
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn("openApiGenerate")
}

kotlin {
    jvmToolchain(21)
}