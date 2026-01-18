plugins {
    kotlin("jvm")
    kotlin("plugin.jpa")
}

dependencies {
    api(project(":core:core-domain"))
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")
}
