rootProject.name = "oms-backend"

// Core modules
include(":core:core-domain")
include(":core:core-infra")

// Domain modules
include(":domain:domain-identity")
include(":domain:domain-catalog")
include(":domain:domain-channel")
include(":domain:domain-order")
include(":domain:domain-inventory")
include(":domain:domain-claim")
include(":domain:domain-settlement")
include(":domain:domain-automation")
include(":domain:domain-strategy")

// Infrastructure modules
include(":infrastructure:infra-mysql")
include(":infrastructure:infra-mongo")
include(":infrastructure:infra-redis")
include(":infrastructure:infra-external")

// Application & API modules
include(":application")
include(":api")

pluginManagement {
    val kotlinVersion: String by settings
    val springBootVersion: String by settings
    val springDependencyManagementVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
        kotlin("plugin.jpa") version kotlinVersion
        kotlin("kapt") version kotlinVersion
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version springDependencyManagementVersion
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
