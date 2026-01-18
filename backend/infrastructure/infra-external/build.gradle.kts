plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    // WebClient for external API calls
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Resilience4j for circuit breaker
    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.2.0")
}
