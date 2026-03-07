import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") apply false
    kotlin("plugin.spring") apply false
    kotlin("plugin.jpa") apply false
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management") apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.5"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("org.jetbrains.kotlinx.kover") version "0.7.5"
}

allprojects {
    group = property("group") as String
    version = property("version") as String

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "org.jetbrains.kotlinx.kover")

    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}")
        }
    }

    dependencies {
        // Kotlin
        "implementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        "implementation"("org.jetbrains.kotlin:kotlin-reflect")
        "implementation"("com.fasterxml.jackson.module:jackson-module-kotlin")

        // Test
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testImplementation"("io.mockk:mockk:1.13.9")
        "testImplementation"("io.kotest:kotest-runner-junit5:5.8.0")
        "testImplementation"("io.kotest:kotest-assertions-core:5.8.0")
        "testImplementation"("io.kotest.extensions:kotest-extensions-spring:1.1.3")
        "testImplementation"("com.tngtech.archunit:archunit-junit5:1.2.1")
        "testImplementation"("org.testcontainers:testcontainers:1.19.4")
        "testImplementation"("org.testcontainers:junit-jupiter:1.19.4")
        "testImplementation"("org.testcontainers:postgresql:1.19.4")
        "testImplementation"("org.testcontainers:mongodb:1.19.4")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "21"
        }
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    // Detekt configuration
    detekt {
        buildUponDefaultConfig = true
        allRules = false
        config.setFrom(files("${rootProject.projectDir}/detekt-config.yml"))
    }

    // ktlint configuration
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.1.1")
        enableExperimentalRules.set(false)
        android.set(false)
        outputToConsole.set(true)
        ignoreFailures.set(false)
        filter {
            exclude("**/generated/**")
            include("**/kotlin/**")
        }
    }

    // Kover configuration - Temporarily disabled due to API changes
    // configure<kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension> {
    //     reports {
    //         filters {
    //             excludes {
    //                 classes("*Test", "*Tests")
    //             }
    //         }
    //
    //         verify {
    //             onCheck = true
    //             rule {
    //                 minBound(80)
    //             }
    //         }
    //
    //         total {
    //             html {
    //                 onCheck = true
    //             }
    //             xml {
    //                 onCheck = true
    //             }
    //         }
    //     }
    // }

    // Quality check task
    tasks.register("qualityCheck") {
        group = "verification"
        description = "Runs all code quality checks"
        dependsOn("detekt", "ktlintCheck")
    }

    // Make build depend on quality check
    tasks.named("check") {
        dependsOn("qualityCheck")
    }
}

// Domain modules configuration
configure(subprojects.filter { it.path.startsWith(":domain:") }) {
    dependencies {
        "implementation"(project(":core:core-domain"))
    }
}

// Infrastructure modules configuration
configure(subprojects.filter { it.path.startsWith(":infrastructure:") }) {
    dependencies {
        "implementation"(project(":core:core-domain"))
        "implementation"(project(":core:core-infra"))
    }
}
