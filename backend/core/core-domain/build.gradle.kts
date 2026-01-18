plugins {
    kotlin("jvm")
}

dependencies {
    // Jakarta Persistence API (interface only)
    compileOnly("jakarta.persistence:jakarta.persistence-api")

    // Validation
    implementation("jakarta.validation:jakarta.validation-api")
}
