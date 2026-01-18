package com.oms.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main application class for OMS API
 */
@SpringBootApplication(scanBasePackages = ["com.oms"])
class OmsApplication

fun main(args: Array<String>) {
    runApplication<OmsApplication>(*args)
}
