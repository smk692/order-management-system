package com.oms.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.core.task.support.TaskExecutorAdapter
import org.springframework.scheduling.annotation.EnableAsync
import java.util.concurrent.Executors

/**
 * Virtual Threads configuration for Spring Boot 3.2+
 *
 * This configuration enables JDK 21 Virtual Threads for async operations,
 * providing lightweight concurrency for high-throughput applications.
 *
 * Virtual threads are suitable for I/O-bound operations and can scale to
 * millions of concurrent tasks without the overhead of traditional platform threads.
 */
@Configuration
@EnableAsync
class VirtualThreadsConfig {

    /**
     * Configures the default async task executor to use virtual threads.
     *
     * This executor will be used for:
     * - @Async annotated methods
     * - CompletableFuture async operations
     * - Spring's async processing
     *
     * @return AsyncTaskExecutor backed by virtual threads
     */
    @Bean(name = ["taskExecutor", "applicationTaskExecutor"])
    fun asyncTaskExecutor(): AsyncTaskExecutor {
        return TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor())
    }
}
