package com.oms.order.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

/**
 * Customer value object
 * Represents customer information for an order
 */
@Embeddable
data class Customer(
    @Column(name = "customer_name")
    val name: String,

    @Column(name = "customer_phone")
    val phone: String,

    @Column(name = "customer_email")
    val email: String? = null
) {
    init {
        require(name.isNotBlank()) { "Customer name cannot be blank" }
        require(phone.isNotBlank()) { "Customer phone cannot be blank" }
        email?.let {
            require(it.contains("@")) { "Invalid email format" }
        }
    }

    override fun toString(): String = "$name ($phone)"
}
