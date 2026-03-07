package com.oms.api

import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Base class for integration tests with Testcontainers support.
 *
 * This class provides:
 * - PostgreSQL container for relational data
 * - MongoDB container for document storage
 * - Redis container for caching
 * - Spring Boot test context
 * - Active test profile
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
abstract class IntegrationTestBase : FunSpec() {
    override fun extensions() = listOf(SpringExtension)

    companion object {
        /**
         * PostgreSQL container for integration tests
         */
        val postgresContainer: PostgreSQLContainer<*> =
            PostgreSQLContainer("postgres:16-alpine")
                .withDatabaseName("oms_test")
                .withUsername("test")
                .withPassword("test")
                .apply {
                    start()
                }

        /**
         * MongoDB container for integration tests
         */
        val mongoContainer: MongoDBContainer =
            MongoDBContainer("mongo:7.0")
                .apply {
                    start()
                }

        /**
         * Redis container for integration tests
         */
        val redisContainer: GenericContainer<*> =
            GenericContainer("redis:7-alpine")
                .withExposedPorts(6379)
                .apply {
                    start()
                }

        init {
            // Set system properties for Spring to pick up container URLs
            System.setProperty("spring.datasource.url", postgresContainer.jdbcUrl)
            System.setProperty("spring.datasource.username", postgresContainer.username)
            System.setProperty("spring.datasource.password", postgresContainer.password)
            System.setProperty("spring.data.mongodb.uri", mongoContainer.replicaSetUrl)
            System.setProperty("spring.data.redis.host", redisContainer.host)
            System.setProperty("spring.data.redis.port", redisContainer.firstMappedPort.toString())
        }
    }
}
