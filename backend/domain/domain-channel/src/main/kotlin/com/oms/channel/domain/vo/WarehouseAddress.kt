package com.oms.channel.domain.vo

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class WarehouseAddress(
    @Column(name = "zip_code", length = 10)
    val zipCode: String,

    @Column(name = "address1", nullable = false, length = 200)
    val address1: String,

    @Column(name = "address2", length = 200)
    val address2: String? = null,

    @Column(name = "city", length = 100)
    val city: String,

    @Column(name = "country", nullable = false, length = 50)
    val country: String
) {
    init {
        require(zipCode.isNotBlank()) { "Zip code cannot be blank" }
        require(address1.isNotBlank()) { "Address1 cannot be blank" }
        require(city.isNotBlank()) { "City cannot be blank" }
        require(country.isNotBlank()) { "Country cannot be blank" }
    }
}
