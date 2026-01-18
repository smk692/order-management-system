plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
}

dependencies {
    // Core
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-infra"))

    // Application
    implementation(project(":application"))

    // Infrastructure (implemented)
    implementation(project(":infrastructure:infra-mysql"))
    implementation(project(":infrastructure:infra-mongo"))

    // Spring Web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Spring Security
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Actuator
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // OpenAPI / Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // Jackson Kotlin module
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Dev tools
    developmentOnly("org.springframework.boot:spring-boot-devtools")
}

tasks.bootJar {
    archiveFileName.set("oms-api.jar")
}
