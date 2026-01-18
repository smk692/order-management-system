plugins {
    kotlin("jvm")
    kotlin("plugin.jpa")
}

dependencies {
    // Core domain
    api(project(":core:core-domain"))

    // JPA (interface only)
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")
}
