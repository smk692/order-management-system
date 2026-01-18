package com.oms.infra.mysql.inventory

import com.oms.inventory.domain.Stock
import com.oms.inventory.domain.vo.StockStatus
import com.oms.inventory.repository.StockRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Spring Data JPA repository interface for Stock
 */
interface StockJpaRepositoryInterface : JpaRepository<Stock, String> {
    fun findByProductIdAndWarehouseId(productId: String, warehouseId: String): Stock?
    fun findByCompanyId(companyId: String): List<Stock>
    fun findByCompanyIdAndStatus(companyId: String, status: StockStatus): List<Stock>

    @Query("SELECT s FROM Stock s WHERE s.companyId = :companyId AND s.status = 'LOW'")
    fun findLowStockByCompanyId(@Param("companyId") companyId: String): List<Stock>
}

/**
 * Implementation of StockRepository using Spring Data JPA
 */
@Repository
class JpaStockRepository(
    private val jpaRepository: StockJpaRepositoryInterface
) : StockRepository {

    override fun save(stock: Stock): Stock {
        return jpaRepository.save(stock)
    }

    override fun findById(id: UUID): Stock? {
        return jpaRepository.findById(id.toString()).orElse(null)
    }

    override fun findByProductIdAndWarehouseId(productId: String, warehouseId: String): Stock? {
        return jpaRepository.findByProductIdAndWarehouseId(productId, warehouseId)
    }

    override fun findByCompanyId(companyId: UUID): List<Stock> {
        return jpaRepository.findByCompanyId(companyId.toString())
    }

    override fun findByCompanyIdAndStatus(companyId: UUID, status: StockStatus): List<Stock> {
        return jpaRepository.findByCompanyIdAndStatus(companyId.toString(), status)
    }

    override fun findLowStockByCompanyId(companyId: UUID): List<Stock> {
        return jpaRepository.findLowStockByCompanyId(companyId.toString())
    }

    override fun delete(stock: Stock) {
        jpaRepository.delete(stock)
    }
}
