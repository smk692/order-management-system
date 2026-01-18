package com.oms.infra.mysql.repository

import com.oms.order.domain.Order
import com.oms.order.domain.OrderStatus
import com.oms.order.repository.OrderRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant

/**
 * Spring Data JPA repository for Order
 */
interface OrderJpaRepositoryInterface : JpaRepository<Order, String> {

    fun findByCompanyId(companyId: String): List<Order>

    fun findByCompanyIdAndStatus(companyId: String, status: OrderStatus): List<Order>

    fun findByCompanyIdAndChannelId(companyId: String, channelId: String): List<Order>

    fun findByExternalOrderId(externalOrderId: String): Order?

    @Query("SELECT o FROM Order o WHERE o.companyId = :companyId AND o.orderDate BETWEEN :startDate AND :endDate")
    fun findByCompanyIdAndDateRange(
        @Param("companyId") companyId: String,
        @Param("startDate") startDate: Instant,
        @Param("endDate") endDate: Instant
    ): List<Order>

    @Query("SELECT o FROM Order o WHERE o.companyId = :companyId AND o.status = :status AND o.orderDate BETWEEN :startDate AND :endDate")
    fun findByCompanyIdAndStatusAndDateRange(
        @Param("companyId") companyId: String,
        @Param("status") status: OrderStatus,
        @Param("startDate") startDate: Instant,
        @Param("endDate") endDate: Instant
    ): List<Order>

    fun countByCompanyId(companyId: String): Long

    fun countByCompanyIdAndStatus(companyId: String, status: OrderStatus): Long

    @Query("SELECT o FROM Order o WHERE o.companyId = :companyId ORDER BY o.orderDate DESC")
    fun findByCompanyIdWithPaging(@Param("companyId") companyId: String, pageable: org.springframework.data.domain.Pageable): List<Order>
}

/**
 * Implementation of OrderRepository using Spring Data JPA
 */
@Repository
class OrderJpaRepository(
    private val jpaRepository: OrderJpaRepositoryInterface
) : OrderRepository {

    override fun save(order: Order): Order {
        return jpaRepository.save(order)
    }

    override fun findById(id: String): Order? {
        return jpaRepository.findById(id).orElse(null)
    }

    override fun findByCompanyId(companyId: String): List<Order> {
        return jpaRepository.findByCompanyId(companyId)
    }

    override fun findByCompanyIdAndStatus(companyId: String, status: OrderStatus): List<Order> {
        return jpaRepository.findByCompanyIdAndStatus(companyId, status)
    }

    override fun findByCompanyIdAndChannelId(companyId: String, channelId: String): List<Order> {
        return jpaRepository.findByCompanyIdAndChannelId(companyId, channelId)
    }

    override fun findByExternalOrderId(externalOrderId: String): Order? {
        return jpaRepository.findByExternalOrderId(externalOrderId)
    }

    override fun findByCompanyIdAndDateRange(
        companyId: String,
        startDate: Instant,
        endDate: Instant
    ): List<Order> {
        return jpaRepository.findByCompanyIdAndDateRange(companyId, startDate, endDate)
    }

    override fun findByCompanyIdAndStatusAndDateRange(
        companyId: String,
        status: OrderStatus,
        startDate: Instant,
        endDate: Instant
    ): List<Order> {
        return jpaRepository.findByCompanyIdAndStatusAndDateRange(companyId, status, startDate, endDate)
    }

    override fun findByCompanyIdWithPaging(companyId: String, offset: Int, limit: Int): List<Order> {
        val pageable = PageRequest.of(offset / limit, limit)
        return jpaRepository.findByCompanyIdWithPaging(companyId, pageable)
    }

    override fun countByCompanyId(companyId: String): Long {
        return jpaRepository.countByCompanyId(companyId)
    }

    override fun countByCompanyIdAndStatus(companyId: String, status: OrderStatus): Long {
        return jpaRepository.countByCompanyIdAndStatus(companyId, status)
    }

    override fun delete(order: Order) {
        jpaRepository.delete(order)
    }
}
