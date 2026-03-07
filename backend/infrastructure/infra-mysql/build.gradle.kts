plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
}

dependencies {
    // Core domain
    api(project(":core:core-domain"))

    // Domain modules (implemented)
    implementation(project(":domain:domain-identity"))
    implementation(project(":domain:domain-catalog"))
    implementation(project(":domain:domain-order"))
    implementation(project(":domain:domain-channel"))
    implementation(project(":domain:domain-inventory"))
    implementation(project(":domain:domain-claim"))
    implementation(project(":domain:domain-settlement"))
    implementation(project(":domain:domain-strategy"))
    implementation(project(":domain:domain-automation"))

    // Spring Data JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // PostgreSQL
    runtimeOnly("org.postgresql:postgresql")

    // Flyway
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
}
