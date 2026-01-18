package com.oms.core.domain

import jakarta.persistence.Embeddable

/**
 * Address Value Object
 * Represents physical addresses with recipient info for shipping
 */
@Embeddable
data class Address(
    val recipient: String,
    val phone: String,
    val zipCode: String? = null,
    val address1: String,
    val address2: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String = "KR"
) {
    init {
        require(recipient.isNotBlank()) { "Recipient cannot be blank" }
        require(phone.isNotBlank()) { "Phone cannot be blank" }
        require(address1.isNotBlank()) { "Address1 cannot be blank" }
        require(country.isNotBlank()) { "Country cannot be blank" }
    }

    fun fullAddress(): String {
        val parts = mutableListOf(address1)
        address2?.let { parts.add(it) }
        city?.let { parts.add(it) }
        state?.let { parts.add(it) }
        zipCode?.let { parts.add(it) }
        parts.add(country)
        return parts.joinToString(", ")
    }

    companion object {
        fun korean(
            recipient: String,
            phone: String,
            zipCode: String,
            address1: String,
            address2: String? = null,
            city: String = "서울"
        ): Address = Address(
            recipient = recipient,
            phone = phone,
            zipCode = zipCode,
            address1 = address1,
            address2 = address2,
            city = city,
            country = "KR"
        )
    }
}
