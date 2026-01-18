package com.oms.api.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenAPI / Swagger configuration
 */
@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("OMS API")
                    .description("Global Order Management System API")
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("OMS Team")
                            .email("oms@example.com")
                    )
                    .license(
                        License()
                            .name("Private")
                    )
            )
    }
}
