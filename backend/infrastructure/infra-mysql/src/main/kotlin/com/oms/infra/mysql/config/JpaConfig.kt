package com.oms.infra.mysql.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * JPA Configuration for MySQL
 */
@Configuration
@EnableJpaRepositories(basePackages = ["com.oms.infra.mysql.repository"])
@EntityScan(basePackages = [
    "com.oms.identity.domain",
    "com.oms.catalog.domain",
    "com.oms.order.domain"
])
class JpaConfig
