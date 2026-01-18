plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    // Core
    api(project(":core:core-domain"))
    implementation(project(":core:core-infra"))

    // Domain modules (implemented)
    api(project(":domain:domain-identity"))
    api(project(":domain:domain-catalog"))
    api(project(":domain:domain-order"))
    api(project(":domain:domain-channel"))
    api(project(":domain:domain-inventory"))
    api(project(":domain:domain-claim"))
    api(project(":domain:domain-settlement"))
    api(project(":domain:domain-automation"))
    api(project(":domain:domain-strategy"))

    // Spring
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework:spring-tx")

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")
}
