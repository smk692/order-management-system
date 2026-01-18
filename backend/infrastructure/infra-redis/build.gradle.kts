plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    // Spring Data Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
}
