package com.oms.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST API for system diagnostics and verification
 */
@RestController
@RequestMapping("/api/v1/diagnostics")
@Tag(name = "Diagnostics", description = "System diagnostics and health check APIs")
class DiagnosticsController {
    @GetMapping("/thread-info")
    @Operation(summary = "Get current thread information to verify Virtual Threads")
    fun getThreadInfo(): ResponseEntity<Map<String, Any>> {
        val currentThread = Thread.currentThread()
        return ResponseEntity.ok(
            mapOf(
                "threadName" to currentThread.name,
                "isVirtual" to currentThread.isVirtual,
                "threadId" to currentThread.threadId(),
            ),
        )
    }

    @GetMapping("/health")
    @Operation(summary = "Health check endpoint")
    fun health(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(
            mapOf("status" to "UP"),
        )
    }
}
