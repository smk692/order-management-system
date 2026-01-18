package com.oms.infra.mongo.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

/**
 * MongoDB Configuration
 */
@Configuration
@EnableMongoRepositories(basePackages = ["com.oms.infra.mongo.repository"])
class MongoConfig
