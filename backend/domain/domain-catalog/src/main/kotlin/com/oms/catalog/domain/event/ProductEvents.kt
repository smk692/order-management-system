package com.oms.catalog.domain.event

import com.oms.core.event.DomainEvent
import com.oms.catalog.domain.ProductStatus

/**
 * Event fired when a new product is created
 */
class ProductCreatedEvent(
    val productId: String,
    val companyId: String,
    val sku: String,
    val name: String
) : DomainEvent() {
    override val aggregateId: String = productId
    override val aggregateType: String = "Product"
}

/**
 * Event fired when product status changes
 */
class ProductStatusChangedEvent(
    val productId: String,
    val companyId: String,
    val previousStatus: ProductStatus,
    val newStatus: ProductStatus
) : DomainEvent() {
    override val aggregateId: String = productId
    override val aggregateType: String = "Product"
}

/**
 * Event fired when a barcode is added to a product
 */
class BarcodeAddedEvent(
    val productId: String,
    val companyId: String,
    val barcode: String,
    val isMain: Boolean
) : DomainEvent() {
    override val aggregateId: String = productId
    override val aggregateType: String = "Product"
}

/**
 * Event fired when product information is updated
 */
class ProductUpdatedEvent(
    val productId: String,
    val companyId: String,
    val updatedFields: Set<String>
) : DomainEvent() {
    override val aggregateId: String = productId
    override val aggregateType: String = "Product"
}
