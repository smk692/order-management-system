package com.oms.channel.domain.vo

import jakarta.persistence.Embeddable

@Embeddable
@JvmInline
value class ChannelId(val value: String) {
    init {
        require(value.isNotBlank()) { "Channel ID cannot be blank" }
    }

    override fun toString(): String = value
}
