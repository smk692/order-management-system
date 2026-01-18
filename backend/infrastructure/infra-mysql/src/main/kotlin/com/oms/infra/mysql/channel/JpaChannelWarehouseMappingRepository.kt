package com.oms.infra.mysql.channel

import com.oms.channel.domain.ChannelWarehouseMapping
import com.oms.channel.repository.ChannelWarehouseMappingRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * Spring Data JPA repository for ChannelWarehouseMapping
 */
interface ChannelWarehouseMappingJpaRepositoryInterface : JpaRepository<ChannelWarehouseMapping, String> {
    fun findByChannelId(channelId: String): List<ChannelWarehouseMapping>
    fun findByWarehouseId(warehouseId: String): List<ChannelWarehouseMapping>

    @Modifying
    @Transactional
    @Query("DELETE FROM ChannelWarehouseMapping m WHERE m.channelId = :channelId AND m.warehouseId = :warehouseId")
    fun deleteByChannelIdAndWarehouseId(
        @Param("channelId") channelId: String,
        @Param("warehouseId") warehouseId: String
    )
}

/**
 * Implementation of ChannelWarehouseMappingRepository using Spring Data JPA
 */
@Repository
class JpaChannelWarehouseMappingRepository(
    private val jpaRepository: ChannelWarehouseMappingJpaRepositoryInterface
) : ChannelWarehouseMappingRepository {

    override fun save(mapping: ChannelWarehouseMapping): ChannelWarehouseMapping {
        return jpaRepository.save(mapping)
    }

    override fun findByChannelId(channelId: String): List<ChannelWarehouseMapping> {
        return jpaRepository.findByChannelId(channelId)
    }

    override fun findByWarehouseId(warehouseId: String): List<ChannelWarehouseMapping> {
        return jpaRepository.findByWarehouseId(warehouseId)
    }

    override fun deleteByChannelIdAndWarehouseId(channelId: String, warehouseId: String) {
        jpaRepository.deleteByChannelIdAndWarehouseId(channelId, warehouseId)
    }
}
