package com.oms.api

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

/**
 * Abstract base class for integration tests.
 *
 * This class provides:
 * - Spring Boot test context with RANDOM_PORT
 * - Testcontainers setup for PostgreSQL, MongoDB, and Redis
 * - Automatic container lifecycle management
 * - Dynamic property configuration from containers
 *
 * Usage:
 * ```kotlin
 * @SpringBootTest
 * class OrderApiIntegrationTest : AbstractIntegrationTest() {
 *     @Autowired
 *     lateinit var restTemplate: TestRestTemplate
 *
 *     @Test
 *     fun `should create order via REST API`() {
 *         val response = restTemplate.postForEntity("/api/v1/orders", request, OrderResponse::class.java)
 *         response.statusCode shouldBe HttpStatus.CREATED
 *     }
 * }
 * ```
 *
 * Container Details:
 * - PostgreSQL 16 (main database)
 * - MongoDB 7.0 (document storage)
 * - Redis 7.2 (caching and session storage)
 *
 * Performance Notes:
 * - Containers are shared across all tests in the suite (singleton pattern)
 * - First test startup: ~10-15 seconds
 * - Subsequent tests: <1 second (containers reused)
 * - Containers automatically stop when test suite completes
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class AbstractIntegrationTest {
    companion object {
        /**
         * PostgreSQL container for relational data storage
         */
        @Container
        @JvmStatic
        val postgresContainer: PostgreSQLContainer<*> =
            PostgreSQLContainer(
                DockerImageName.parse("postgres:16-alpine"),
            )
                .withDatabaseName("oms_test")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true) // Reuse container across test runs (requires Testcontainers config)

        /**
         * MongoDB container for document storage and event sourcing
         */
        @Container
        @JvmStatic
        val mongoContainer: MongoDBContainer =
            MongoDBContainer(
                DockerImageName.parse("mongo:7.0"),
            )
                .withReuse(true)

        /**
         * Redis container for caching and distributed session storage
         */
        @Container
        @JvmStatic
        val redisContainer: GenericContainer<*> =
            GenericContainer(
                DockerImageName.parse("redis:7.2-alpine"),
            )
                .withExposedPorts(6379)
                .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*\\n", 1))
                .withReuse(true)

        /**
         * Configure Spring Boot application properties dynamically from container URLs
         */
        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            // PostgreSQL configuration
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)

            // MongoDB configuration
            registry.add("spring.data.mongodb.uri", mongoContainer::getReplicaSetUrl)

            // Redis configuration
            registry.add("spring.data.redis.host", redisContainer::getHost)
            registry.add("spring.data.redis.port") { redisContainer.getMappedPort(6379) }
        }
    }
}
