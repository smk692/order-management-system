package com.oms.infra.mysql.channel

import com.oms.channel.domain.Warehouse
import com.oms.channel.repository.WarehouseRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Spring Data JPA repository for Warehouse
 */
interface WarehouseJpaRepositoryInterface : JpaRepository<Warehouse, String> {
    fun findByCompanyId(companyId: String): List<Warehouse>
    fun findByCompanyIdAndRegion(companyId: String, region: String): List<Warehouse>
}

/**
 * Implementation of WarehouseRepository using Spring Data JPA
 */
@Repository
class JpaWarehouseRepository(
    private val jpaRepository: WarehouseJpaRepositoryInterface
) : WarehouseRepository {

    override fun save(warehouse: Warehouse): Warehouse {
        return jpaRepository.save(warehouse)
    }

    override fun findById(id: String): Warehouse? {
        return jpaRepository.findById(id).orElse(null)
    }

    override fun findByCompanyId(companyId: UUID): List<Warehouse> {
        return jpaRepository.findByCompanyId(companyId.toString())
    }

    override fun findByCompanyIdAndRegion(companyId: UUID, region: String): List<Warehouse> {
        return jpaRepository.findByCompanyIdAndRegion(companyId.toString(), region)
    }
}
