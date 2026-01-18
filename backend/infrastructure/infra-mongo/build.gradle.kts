plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    // Core domain
    api(project(":core:core-domain"))

    // Spring Data MongoDB
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    // Jackson for JSON serialization
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}
