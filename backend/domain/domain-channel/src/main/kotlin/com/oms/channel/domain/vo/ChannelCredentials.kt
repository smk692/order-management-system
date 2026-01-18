package com.oms.channel.domain.vo

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class ChannelCredentials(
    @Column(name = "api_key", nullable = false, length = 255)
    val apiKey: String,

    @Column(name = "secret_key", nullable = false, length = 255)
    val secretKey: String,

    @Column(name = "additional_config", columnDefinition = "TEXT")
    val additionalConfig: String? = null
) {
    init {
        require(apiKey.isNotBlank()) { "API key cannot be blank" }
        require(secretKey.isNotBlank()) { "Secret key cannot be blank" }
    }
}
