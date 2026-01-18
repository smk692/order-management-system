package com.oms.infra.mysql.channel

import com.oms.channel.domain.Channel
import com.oms.channel.domain.vo.ChannelStatus
import com.oms.channel.repository.ChannelRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Spring Data JPA repository for Channel
 */
interface ChannelJpaRepositoryInterface : JpaRepository<Channel, String> {
    fun findByCompanyId(companyId: String): List<Channel>
    fun findByCompanyIdAndStatus(companyId: String, status: ChannelStatus): List<Channel>
    fun existsByNameAndCompanyId(name: String, companyId: String): Boolean
}

/**
 * Implementation of ChannelRepository using Spring Data JPA
 */
@Repository
class JpaChannelRepository(
    private val jpaRepository: ChannelJpaRepositoryInterface
) : ChannelRepository {

    override fun save(channel: Channel): Channel {
        return jpaRepository.save(channel)
    }

    override fun findById(id: String): Channel? {
        return jpaRepository.findById(id).orElse(null)
    }

    override fun findByCompanyId(companyId: UUID): List<Channel> {
        return jpaRepository.findByCompanyId(companyId.toString())
    }

    override fun findByCompanyIdAndStatus(companyId: UUID, status: ChannelStatus): List<Channel> {
        return jpaRepository.findByCompanyIdAndStatus(companyId.toString(), status)
    }

    override fun existsByNameAndCompanyId(name: String, companyId: UUID): Boolean {
        return jpaRepository.existsByNameAndCompanyId(name, companyId.toString())
    }
}
