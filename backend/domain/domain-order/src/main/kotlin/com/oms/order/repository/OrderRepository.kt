package com.oms.order.repository

import com.oms.order.domain.Order
import com.oms.order.domain.OrderStatus
import java.time.Instant

/**
 * Repository interface for Order aggregate
 * Implementation will be in infrastructure module
 */
interface OrderRepository {

    fun save(order: Order): Order

    fun findById(id: String): Order?

    fun findByCompanyId(companyId: String): List<Order>

    fun findByCompanyIdAndStatus(companyId: String, status: OrderStatus): List<Order>

    fun findByCompanyIdAndChannelId(companyId: String, channelId: String): List<Order>

    fun findByExternalOrderId(externalOrderId: String): Order?

    fun findByCompanyIdAndDateRange(
        companyId: String,
        startDate: Instant,
        endDate: Instant
    ): List<Order>

    fun findByCompanyIdAndStatusAndDateRange(
        companyId: String,
        status: OrderStatus,
        startDate: Instant,
        endDate: Instant
    ): List<Order>

    fun findByCompanyIdWithPaging(companyId: String, offset: Int, limit: Int): List<Order>

    fun countByCompanyId(companyId: String): Long

    fun countByCompanyIdAndStatus(companyId: String, status: OrderStatus): Long

    fun delete(order: Order)
}
