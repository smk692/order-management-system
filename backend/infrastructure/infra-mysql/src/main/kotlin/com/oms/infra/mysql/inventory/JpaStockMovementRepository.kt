package com.oms.infra.mysql.inventory

import com.oms.inventory.domain.StockMovement
import com.oms.inventory.repository.StockMovementRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Spring Data JPA repository interface for StockMovement
 */
interface StockMovementJpaRepositoryInterface : JpaRepository<StockMovement, String> {
    fun findByStockId(stockId: UUID): List<StockMovement>
    fun findByReferenceId(referenceId: String): List<StockMovement>
}

/**
 * Implementation of StockMovementRepository using Spring Data JPA
 */
@Repository
class JpaStockMovementRepository(
    private val jpaRepository: StockMovementJpaRepositoryInterface
) : StockMovementRepository {

    override fun save(movement: StockMovement): StockMovement {
        return jpaRepository.save(movement)
    }

    override fun findByStockId(stockId: UUID): List<StockMovement> {
        return jpaRepository.findByStockId(stockId)
    }

    override fun findByReferenceId(referenceId: String): List<StockMovement> {
        return jpaRepository.findByReferenceId(referenceId)
    }
}
